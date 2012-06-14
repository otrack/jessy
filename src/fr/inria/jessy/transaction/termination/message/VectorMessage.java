package fr.inria.jessy.transaction.termination.message;

import java.util.Set;

import net.sourceforge.fractal.multicast.MulticastMessage;
import fr.inria.jessy.ConstantPool;
import fr.inria.jessy.vector.ConcurrentVersionVector;
import fr.inria.jessy.vector.VersionVector;

public class VectorMessage extends MulticastMessage {
	private static final long serialVersionUID = ConstantPool.JESSY_MID;

	// For Fractal
	@Deprecated
	public VectorMessage() {
	}

	public VectorMessage(ConcurrentVersionVector<String> vector, Set<String> dest,
			String gsource, int source) {
		super(vector, dest, gsource, source);
	}

	public ConcurrentVersionVector<String> getConcurrentVersionVector() {
		return (ConcurrentVersionVector) serializable;
	}

}
