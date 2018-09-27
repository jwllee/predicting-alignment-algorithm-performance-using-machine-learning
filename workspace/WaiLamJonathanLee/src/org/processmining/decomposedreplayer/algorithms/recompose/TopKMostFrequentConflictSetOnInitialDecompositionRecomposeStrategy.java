package org.processmining.decomposedreplayer.algorithms.recompose;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.models.recompose.MergeConflictSet;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public abstract class TopKMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy implements RecomposeStrategy {

	private ConflictUtils conflictUtils = new ConflictUtils();
	private int count = 0;
	
	public Set<XEventClass> recompose(RecomposeInformation info) {
		
		Set<SyncReplayResult> openAlignments = info.getOpenAlignments();
		
		Map<Set<XEventClass>, Integer> traceConflictSetFrequency = new HashMap<>();
		Map<Set<XEventClass>, Double> traceConflictSetScore = new HashMap<>();
		
		int mostFrequent = 0;
		double minScore = Double.MAX_VALUE;
		
		for (SyncReplayResult traceAlignment: openAlignments) {
			Map<XEventClass, Double> traceConflictMap = conflictUtils.getConflicts(traceAlignment);
			Set<XEventClass> traceConflictSet = new HashSet<>(traceConflictMap.keySet());
			
			// update the overall count
			int updated = 1 + traceConflictSetFrequency.getOrDefault(traceConflictSet, 0);
			traceConflictSetFrequency.put(traceConflictSet, updated);

			double score = traceConflictSet.size();
			
			if (!traceConflictSetScore.containsKey(traceConflictSet)) {
				traceConflictSetScore.put(traceConflictSet, score);
			} else {
				if (Double.compare(traceConflictSetScore.get(traceConflictSet), score) < 0)
					// keep the largest value
					traceConflictSetScore.put(traceConflictSet, score);
			}
			
//			System.out.println("[" + getClass().getSimpleName() + "] Conflict activity set: " + 
//					traceConflictSet + " has " + score + " conflicts.");
			
			// check if this makes it the best trace conflict set
			if (updated > mostFrequent) {
				mostFrequent = updated;
			}
			
			if (Double.compare(minScore, score) > 0)
				minScore = score;
		}

		List<MergeConflictSet> mergeConflictSetList = new ArrayList<>(); 
		
		for (Set<XEventClass> set: traceConflictSetFrequency.keySet()) {
			MergeConflictSet conflictSet = new MergeConflictSet(set, traceConflictSetFrequency.get(set));
			mergeConflictSetList.add(conflictSet);
		}
		
		mergeConflictSetList.sort(new Comparator<MergeConflictSet>() {

			public int compare(MergeConflictSet o1, MergeConflictSet o2) {
				// sort in a decrementing manner
				int compareTo = o1.compareTo(o2);
				if (compareTo != 0)
					return -compareTo;
				else {
					// rank higher the set with the smaller activity set
					int s0 = o1.getConflictActivitySet().size();
					int s1 = o2.getConflictActivitySet().size();
					if (s0 < s1)
						return -1;
					else if (s0 == s1)
						return 0;
					else
						return 1;
				}
			}
		});
		
//		System.out.println("[" + getClass().getSimpleName() + "] Merge conflict sets: \n" + mergeConflictSetList.toString());
		
		Set<XEventClass> bestTraceConflictSet = new HashSet<>();
		
		int toAdd = getK();
		if (mergeConflictSetList.size() < getK())
			toAdd = mergeConflictSetList.size();
		
		for (int i = 0; i < toAdd; i++) {
			MergeConflictSet set = mergeConflictSetList.get(i);
			bestTraceConflictSet.addAll(set.getConflictActivitySet());
		}
			
		System.out.println("[" + getClass().getSimpleName() + "] Number of max conflicting sets: " + toAdd);
		
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict activities: " + 
				bestTraceConflictSet + " with frequency: " + mostFrequent);

		// add all the current unsplittables into best trace conflict set
		bestTraceConflictSet.addAll(info.getInitialDecomposition());
		
		return bestTraceConflictSet;
	}

	public abstract int getK();
	
	public abstract int getNofInitialDecomposition();
	
}
