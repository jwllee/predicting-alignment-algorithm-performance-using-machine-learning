package org.processmining.decomposedreplayer.algorithms.recompose;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.models.recompose.MergeConflictScoreSet;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public abstract class TopKMinimalDecompositionRecomposeStrategy implements RecomposeStrategy {
	
	private int curUnsplittableIndex = 0;
	private ConflictUtils conflictUtils = new ConflictUtils();
	private Set<Set<XEventClass>> triedConflictSets = new HashSet<>();
	private List<Set<XEventClass>> unsplittableList = new ArrayList<>();
	
	private Set<XEventClass> triedActivities = new HashSet<>();
	
	public Set<XEventClass> recompose(RecomposeInformation info) {
		if (unsplittableList.size() == 0)
			unsplittableList.add(info.getInitialDecomposition());
		
		Set<SyncReplayResult> openAlignments = info.getOpenAlignments();
		
		Map<Set<XEventClass>, Integer> seenScore = new HashMap<>();
		Map<Set<XEventClass>, Double> triedScore = new HashMap<>();
		
		int maxTried = 0;
		
		for (SyncReplayResult traceAlignment: openAlignments) {
			Map<XEventClass, Double> traceConflictMap = conflictUtils.getConflicts(traceAlignment);
			Set<XEventClass> traceConflictSet = new HashSet<>(traceConflictMap.keySet());
			
			Set<XEventClass> aux = new HashSet<>(traceConflictSet);
			aux.retainAll(triedActivities);
			
			triedScore.put(traceConflictSet, aux.size() + 0.0);
			
			if (aux.size() > maxTried)
				maxTried = aux.size();
			
			if (triedConflictSets.contains(traceConflictSet)) 
				seenScore.put(traceConflictSet, 1);
			else
				seenScore.put(traceConflictSet, 0);
		}
		
		List<MergeConflictScoreSet> conflictList = new ArrayList<>();
		for (Set<XEventClass> conflictSet: seenScore.keySet()) {
			double score = seenScore.get(conflictSet);
			score += (triedScore.get(conflictSet) / maxTried);
			MergeConflictScoreSet set = new MergeConflictScoreSet(conflictSet, score);
			conflictList.add(set);
		}
		
		// sort incrementally
		conflictList.sort(new Comparator<MergeConflictScoreSet>() {

			public int compare(MergeConflictScoreSet o1, MergeConflictScoreSet o2) {
				return o1.compareTo(o2);
			}
		});
		
		Set<XEventClass> bestTraceConflictSet = new HashSet<>();
		double maxScore = -1;
		int end = getK();
		
		if (end > conflictList.size())
			end = conflictList.size();
		
		Set<XEventClass> seenActs = new HashSet<>(triedActivities);
		
		for (int i = 0; i < end; i++) {
			MergeConflictScoreSet set = conflictList.get(i);
			if (Double.compare(set.getScore(), maxScore) > 0)
				maxScore = set.getScore();
			bestTraceConflictSet.addAll(set.getConflictActivitySet());
			triedConflictSets.add(set.getConflictActivitySet());
			triedActivities.addAll(set.getConflictActivitySet());
		}
		
		seenActs.retainAll(bestTraceConflictSet);
		
		System.out.println("[" + getClass().getSimpleName() + "] " + seenActs.size() + " seen activities: " + seenActs);
		
		System.out.println("[" + getClass().getSimpleName() + "] Minimum seen score: " + maxScore);
		System.out.println("[" + getClass().getSimpleName() + "] Tried conflict sets: " + triedConflictSets);
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing on " + bestTraceConflictSet.size() + " activities: " + bestTraceConflictSet);
		
		if (maxScore > 0) {
			curUnsplittableIndex++;
			System.out.println("[" + getClass().getSimpleName() + "] Updating unsplittable index to: " + curUnsplittableIndex);
		}
		
		System.out.println("[" + getClass().getSimpleName() + "] Current unsplittable index: " + curUnsplittableIndex);
		
		bestTraceConflictSet.addAll(unsplittableList.get(curUnsplittableIndex));
		unsplittableList.add(bestTraceConflictSet);
		return bestTraceConflictSet;
	}

	public abstract int getK();
	
}
