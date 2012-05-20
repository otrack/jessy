package fr.inria.jessy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.fractal.FractalManager;
import net.sourceforge.fractal.Learner;
import net.sourceforge.fractal.Stream;
import net.sourceforge.fractal.membership.Group;
import net.sourceforge.fractal.multicast.MulticastStream;
import net.sourceforge.fractal.utils.ExecutorPool;
import net.sourceforge.fractal.utils.PerformanceProbe.ValueRecorder;

import org.apache.log4j.Logger;

import fr.inria.jessy.store.JessyEntity;
import fr.inria.jessy.store.ReadReply;
import fr.inria.jessy.store.ReadRequest;

/**
 * 
 * A remote reader for distributed Jessy. This class takes as input a remote
 * read request via function
 * <p>
 * remoteRead
 * </p>
 * , and returns a Future encapsulating a JessyEntity. It maekes use of the
 * Fractal group ALLNODES to exchange replies , and create a
 * ReliableMulticastStream named RemoteReaderStream.
 * 
 * TODO: put the ExecutorPool inside Jessy (?) TODO: suppress or garbage-collect
 * cancelled requests.
 * 
 * @author Pierre Sutra
 * @author Masoud Saeida Ardekani
 */

// FIXME fix parametrized types.
// TODO CAUTION: this implementation is not fault tolerant

public class RemoteReader implements Learner {

	private static Logger logger = Logger.getLogger(RemoteReader.class);
	
	private static ValueRecorder serverAnsweringTime;
	static{
		serverAnsweringTime = new ValueRecorder("RemoteReader#serverAnsweringTime(us)");
		serverAnsweringTime.setFormat("%a");
		serverAnsweringTime.setFactor(1000);
	}


	private DistributedJessy jessy;
	private MulticastStream remoteReadStream;

	private ExecutorPool pool = ExecutorPool.getInstance();

	private Map<Integer, ReadReply<? extends JessyEntity>> replies;

	/**
	 * States the number of different recipients for each particular read
	 * request. ReadRequestTask will be notified to continue when the
	 * corresponding value in the map becomes zero.
	 * 
	 */
	private Map<Integer, AtomicInteger> readRequestRecipientCounts;

	private Map<Integer, ReadRequest<? extends JessyEntity>> requests;

	public RemoteReader(DistributedJessy j, Group g) {
		jessy = j;
		remoteReadStream = FractalManager.getInstance()
				.getOrCreateMulticastStream(g.name(), g.name());
		remoteReadStream.registerLearner("RemoteReadRequestMessage", this);
		remoteReadStream.registerLearner("RemoteReadReplyMessage", this);
		remoteReadStream.start();

		replies = new ConcurrentHashMap<Integer, ReadReply<? extends JessyEntity>>();
		requests = new ConcurrentHashMap<Integer, ReadRequest<? extends JessyEntity>>();
		readRequestRecipientCounts = new ConcurrentHashMap<Integer, AtomicInteger>();
	}

	@SuppressWarnings("unchecked")
	public <E extends JessyEntity> Future<ReadReply<E>> remoteRead(
			ReadRequest<E> readRequest) {
		requests.put(readRequest.getReadRequestId(), readRequest);
		logger.debug("creating task for "+readRequest);
		Future<ReadReply<E>> reply = pool.submit(new RemoteReadRequestTask(readRequest));
		return reply;
	}

	@SuppressWarnings("unchecked")
	public void learn(Stream s, Serializable v) {

		if (v instanceof RemoteReadRequestMessage) {

			RemoteReadRequestMessage message = (RemoteReadRequestMessage) v;
			logger.debug("request "	+ message.getReadRequest());
			long start = System.nanoTime();
			ReadRequest request = message.getReadRequest();
			logger.debug("asnswering to " + message.source + " for "
					+ request.getReadRequestId());
			ReadReply readReply = jessy.getDataStore().get(request);
			if( !readReply.getEntity().iterator().hasNext() || readReply.getEntity().iterator().next() == null)
				logger.error("request "+request+ " failed ");
			remoteReadStream.unicast(new RemoteReadReplyMessage(readReply),
					message.source);
			serverAnsweringTime.add(System.nanoTime() - start);

		} else {

			ReadReply reply = ((RemoteReadReplyMessage) v).getReadReply();
			logger.debug("reply " + reply.getReadRequestId());

			if (replies.containsKey(reply.getReadRequestId())) {
				replies.get(reply.getReadRequestId()).mergeReply(reply);
			} else {
				replies.put(reply.getReadRequestId(), reply);
			}

			Integer unAnsweredRequests = readRequestRecipientCounts.get(reply
					.getReadRequestId()).decrementAndGet();
			
			if (unAnsweredRequests == 0) {
				readRequestRecipientCounts.remove(reply.getReadRequestId());
				synchronized (requests.get(reply.getReadRequestId())) {
					requests.get(reply.getReadRequestId()).notify();
				}
			}
		}
	}

	class RemoteReadRequestTask<E extends JessyEntity> implements
			Callable<ReadReply<E>> {

		private ReadRequest<E> request;

		private RemoteReadRequestTask(ReadRequest<E> readRequest) {
			this.request = readRequest;
		}

		@SuppressWarnings("unchecked")
		public ReadReply<E> call() throws Exception {
			Set<Group> destGroups = jessy.partitioner.resolve(request);
			readRequestRecipientCounts.put(request.getReadRequestId(),
					new AtomicInteger(destGroups.size()));
			synchronized (requests.get(request.getReadRequestId())) {
				for (Group dest : destGroups) {
					logger.debug("asking group" + dest + " for " + request);
					remoteReadStream.unicast(new RemoteReadRequestMessage<E>(
							request), dest.members().iterator().next());
				}
				requests.get(request.getReadRequestId()).wait();
			}
			ReadReply<E> reply = (ReadReply<E>) replies.get(request
					.getReadRequestId());

			// FIXME modify this pattern for fault tolerance
			requests.remove(request.getReadRequestId()); 
			replies.remove(request.getReadRequestId());
			readRequestRecipientCounts.remove(request.getReadRequestId());
			
			return reply;
		}

	}

	class RemoteReadReplyTask<E extends JessyEntity> implements
			Callable<ReadReply<E>> {

		private RemoteReadRequestMessage<E> message;

		public RemoteReadReplyTask(RemoteReadRequestMessage<E> m) {
			message = m;
		}

		public ReadReply<E> call() throws Exception {
			long start = System.nanoTime();
			ReadRequest<E> request = message.getReadRequest();
			logger.debug("asnswering to " + message.source + " for "
					+ request.getReadRequestId());
			ReadReply<E> readReply = jessy.getDataStore().get(request);
			if( !readReply.getEntity().iterator().hasNext() || readReply.getEntity().iterator().next() == null)
				logger.error("request "+request+ " failed ");
			remoteReadStream.unicast(new RemoteReadReplyMessage<E>(readReply),
					message.source);
			serverAnsweringTime.add(System.nanoTime() - start);
			return null;
		}

	}

}
