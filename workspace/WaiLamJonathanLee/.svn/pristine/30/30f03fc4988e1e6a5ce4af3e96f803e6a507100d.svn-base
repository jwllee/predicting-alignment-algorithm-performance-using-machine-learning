package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ScoreBasedRecomposeStrategy implements RecomposeStrategy {

	private ConflictUtils conflictUtils = new ConflictUtils();
	
	public static final String NAME = "Score-based";
	public static final double WEIGHT_FREQUENCY = 0.5;
	public static final double WEIGHT_SHARED = 0.5;
	
	public Set<XEventClass> recompose(RecomposeInformation info) {
		Set<XEventClass> toRecompose = new HashSet<>();
		
		Set<SyncReplayResult> openAlignments = info.getOpenAlignments();
		
		Map<Set<XEventClass>, Double> traceConflictFreqSets = new HashMap<>();
		
		double maxFreq = 0.0;
		int maxSize = 0;
		
		for(SyncReplayResult traceAlignment: openAlignments) {
			Map<XEventClass, Double> traceConflictMap = conflictUtils.getConflicts(traceAlignment);
			Set<XEventClass> traceConflictSet = new HashSet<>(traceConflictMap.keySet());
			
			// update the overall conflict score
			double updated = 1 + traceConflictFreqSets.getOrDefault(traceConflictSet, 0.0);
			traceConflictFreqSets.put(traceConflictSet, updated);

			// check if this makes it the highest score
			if (Double.compare(updated, maxFreq) > 0)
				maxFreq = updated;

			if (traceConflictSet.size() > maxSize)
				maxSize = traceConflictSet.size();
		}
		
//		System.out.println("[" + getClass().getSimpleName() + "] Max conflict set size: " + maxSize);
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict frequency: " + maxFreq);

		// compute the normalized conflict score
		Map<Set<XEventClass>, Double> traceConflictFreqScores = new HashMap<>();
		for (Set<XEventClass> activities: traceConflictFreqSets.keySet()) {
			double score = traceConflictFreqSets.get(activities); 
			score = score / maxFreq;
			traceConflictFreqScores.put(activities, score);
		}
		
		ActivityClusterArray clusters = info.getLastActivityClusters();
		Map<XEventClass, Set<Integer>> activitySubnetIndices = new HashMap<>();
		Map<XEventClass, Double> logConflicts = info.getLogConflicts();
		int clustersSize = clusters.getClusters().size();
		for (XEventClass activity: logConflicts.keySet()) {
			Set<Integer> indices = new HashSet<>();
			for (int index = 0; index < clustersSize; index++) {
				Set<XEventClass> cluster = clusters.getCluster(index);
				if (cluster.contains(activity)) {
					indices.add(index);
				}
			}
			activitySubnetIndices.put(activity, indices);
		}
		
		
		Map<Set<XEventClass>, Integer> traceConflictShare = new HashMap<>();
		int maxShared = 0;
		for (Set<XEventClass> conflictSet: traceConflictFreqSets.keySet()) {
			// the average size of recomposed subnets per activity
			int joinedSizes = 0;
			for (XEventClass activity: conflictSet) {
				int joinedSize = 0;
				for (int index: activitySubnetIndices.get(activity)) {
					int clusterSize = clusters.getCluster(index).size();
					joinedSize += clusterSize;
				}
				joinedSizes += joinedSize;
			}
			int sharedScore = joinedSizes / conflictSet.size();
			traceConflictShare.put(conflictSet, sharedScore);
			if (sharedScore > maxShared)
				maxShared = sharedScore;
		}
		
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict frequency: " + maxFreq);
		System.out.println("[" + getClass().getSimpleName() + "] Max shared score (average subnet sizes): " + maxShared);
		
		// normalize shared to get score
		Map<Set<XEventClass>, Double> traceConflictShareScores = new HashMap<>();
		for (Set<XEventClass> activities: traceConflictShare.keySet()) {
			double normalized = 1 - (traceConflictShare.get(activities) / maxShared);
			traceConflictShareScores.put(activities, normalized);
		}
		
		Map<Set<XEventClass>, Double> scoreMap = new HashMap<>();
		Double maxScore = -1.0;
		for (Set<XEventClass> activities: traceConflictFreqScores.keySet()) {
			double score = WEIGHT_FREQUENCY * traceConflictFreqScores.get(activities) + WEIGHT_SHARED * traceConflictShareScores.get(activities);
//			System.out.println("[" + getClass().getSimpleName() + "] " + activities + " has score: " + score);
			scoreMap.put(activities, score);
			if (Double.compare(score, maxScore) > 0)
				maxScore = score;
		}
		System.out.println("[" + getClass().getSimpleName() + "] Max score: " + maxScore);
		
		int cnt = 0;
		for (Set<XEventClass> activities: scoreMap.keySet()) {
			if (Double.compare(scoreMap.get(activities), maxScore) == 0) {
				toRecompose.addAll(activities);
				cnt += 1;
			}
		}

		System.out.println("[" + getClass().getSimpleName() + "] " + cnt + " conflict sets with max score: " + maxScore);		
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing on: " + toRecompose);		
		
		// add all the current unsplittables
		toRecompose.addAll(info.getUnsplittables());
		
		return toRecompose;
	}

	public String getName() {
		return NAME;
	}

}
