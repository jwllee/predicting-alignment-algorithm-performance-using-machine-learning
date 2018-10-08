package org.processmining.plugins.petrinet.replayresult;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class PNRepResultImpl extends TreeSet<SyncReplayResult> implements PNRepResult {

	private static final long serialVersionUID = -3116866282473570550L;
	private Map<String, Object> info = new HashMap<String, Object>(1);

	/**
	 * Add information
	 * 
	 * @param property
	 * @param valString
	 */
	public void addInfo(String property, String valString) {
		info.put(property, valString);
	}

	/**
	 * @return the info
	 */
	public Map<String, Object> getInfo() {
		return info;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(Map<String, Object> info) {
		this.info = info;
	}

	//	@Override
	//	public boolean equals(Object other) {
	//		if (other instanceof PNRepResult) {
	//			return (((PNRepResult) other).hashCode() == this.hashCode());
	//		}
	//		return false;
	//	}

	/**
	 * Cache important aggregate information about syncRepResult in info
	 * variable
	 * 
	 * @param col
	 */
	public PNRepResultImpl(Collection<SyncReplayResult> col) {
		super(new Comparator<SyncReplayResult>() {
			public int compare(SyncReplayResult o1, SyncReplayResult o2) {
				SortedSet<Integer> s1 = o1.getTraceIndex();
				SortedSet<Integer> s2 = o2.getTraceIndex();
				if (o1.isReliable() && !o2.isReliable()) {
					return -1;
				}
				if (!o1.isReliable() && o2.isReliable()) {
					return 1;
				}
				if (s1.size() != s2.size()) {
					return s2.size() - s1.size();
				}
				if (o1.equals(o2)) {
					return 0;
				}
				if (o1.getStepTypes().size() != o2.getStepTypes().size()) {
					return o2.getStepTypes().size() - o1.getStepTypes().size();
				}
				Iterator<Integer> it1 = s1.iterator();
				Iterator<Integer> it2 = s2.iterator();
				while (it1.hasNext()) {
					Integer ss1 = it1.next();
					Integer ss2 = it2.next();
					if (!ss1.equals(ss2)) {
						return ss1.compareTo(ss2);
					}
				}
				return 0;
			}

		});
		addAll(col);

		// get all stats
		TObjectIntMap<String> key2Idx = new TObjectIntHashMap<String>(10, 0.8f, -1);
		TDoubleArrayList stats = new TDoubleArrayList();
		// in stats: 0 = value, 1 = occurrence

		// calculate average states
		for (SyncReplayResult rr : col) {
			int numTraces = rr.getTraceIndex().size();
			for (Entry<String, Double> entry : rr.getInfo().entrySet()) {
				int idx = key2Idx.get(entry.getKey());
				// take care first item
				if (idx < 0) {
					// first time
					idx = stats.size();
					key2Idx.put(entry.getKey(), idx);
					stats.add(entry.getValue());
					stats.add(1.00);
				} else {
					double oldVal = stats.get(idx);
					double occ = stats.get(idx + 1);
					stats.set(idx, oldVal + ((entry.getValue() - oldVal) / (1 + occ)));
					stats.set(idx + 1, 1 + occ);
				}

				// update the rest items
				for (int i = 1; i < numTraces; i++) {
					double oldVal = stats.get(idx);
					double occ = stats.get(idx + 1);
					stats.set(idx, oldVal + ((entry.getValue() - oldVal) / (1 + occ)));
					stats.set(idx + 1, 1 + occ);
				}
			}
		}

		// for each stats, store the value
		for (String key : key2Idx.keySet()) {
			info.put(key, stats.get(key2Idx.get(key)));
		}

	}
}
