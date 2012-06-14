package fr.inria.jessy.vector;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a concurrent version of {@code VersionVector}. It is needed for
 * storing version vectors associated to each jessy instance in the PSI
 * consistency criterion.
 * <p>
 * {@code VersionVector} cannot be used for vectors associated to each jessy
 * instance since its implementation is not thread safe.
 * 
 * @author Masoud Saeida Ardekani
 * 
 */
public class ConcurrentVersionVector<K> implements Externalizable {

	private ConcurrentHashMap<K, Integer> map;

	public ConcurrentVersionVector() {
		map = new ConcurrentHashMap<K, Integer>();
	}

	public void update(Set<Entry<K, Integer>> vector) {
		if (vector == null)
			return;
		for (Entry<K, Integer> entry : vector) {
			K key = entry.getKey();
			Integer value = entry.getValue();
			try {
				if (map.get(key).compareTo(value) < 0) {
					map.put(key, value);
				}
			} catch (Exception ex) {
				map.put(key, value);
			}

		}

	}

	public Map<K, Integer> getMap() {
		return map;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		map = (ConcurrentHashMap) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(map);
	}
}
