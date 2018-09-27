package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class LazyRecomposeStrategy implements RecomposeStrategy {

	public static final String NAME = "Lazy";
	public static final double WEIGHT_FREQUENCY = 0.5;
	public static final double WEIGHT_SEVERITY = 0.5;
	
	private ConflictUtils conflictUtils = new ConflictUtils();
	
	public Set<XEventClass> recompose(RecomposeInformation info) {
		Set<XEventClass> toRecompose = new HashSet<>();
		
		Set<SyncReplayResult> openAlignments = info.getOpenAlignments();
		
		Map<Set<XEventClass>, Double> traceConflictFreqSets = new HashMap<>();
		Map<Set<XEventClass>, Double> traceConflictSeveritySets = new HashMap<>();
		
		double maxFrequency = 0.0;
		int maxSize = 0;
		double maxSeverity = 0.0;
		
		for(SyncReplayResult traceAlignment: openAlignments) {
			Map<XEventClass, Double> traceConflictMap = conflictUtils.getConflicts(traceAlignment);
			Set<XEventClass> traceConflictSet = new HashSet<>(traceConflictMap.keySet());
			
			// update the conflict set frequency
			double updatedFrequency = 1 + traceConflictFreqSets.getOrDefault(traceConflictSet, 0.0);
			traceConflictFreqSets.put(traceConflictSet, updatedFrequency);

			double score = 0.0;
			for (double value: traceConflictMap.values())
				score += value;
			
			double updatedConflictSeverity = score + traceConflictSeveritySets.getOrDefault(traceConflictSet, 0.0);
			traceConflictSeveritySets.put(traceConflictSet, updatedConflictSeverity);
			
//			System.out.println("[" + getClass().getSimpleName() + "] Conflict activity set: " + traceConflictSet + 
//					" has " + score + " conflicts.");
			
			if (traceConflictSet.size() > maxSize)
				maxSize = traceConflictSet.size();
			
			// check if this makes it the highest score
			if (Double.compare(updatedFrequency, maxFrequency) > 0)
				maxFrequency = updatedFrequency;
			
			if (Double.compare(updatedConflictSeverity, maxSeverity) > 0)
				maxSeverity = updatedConflictSeverity;
		}
		
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict set size: " + maxSize);
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict frequency: " + maxFrequency);
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict severity: " + maxSeverity);
				
		// compute the normalized conflict score
		Map<Set<XEventClass>, Double> traceConflictFreqScores = new HashMap<>();
		for (Set<XEventClass> activities: traceConflictFreqSets.keySet()) {
			double score = traceConflictFreqSets.get(activities) + activities.size();
			score = score / (maxFrequency + maxSize);
			traceConflictFreqScores.put(activities, score);
		}
		
		// compute the normalized conflict severity score
		Map<Set<XEventClass>, Double> traceConflictSeverityScores = new HashMap<>();
		for (Set<XEventClass> activities: traceConflictSeveritySets.keySet()) {
			// the score favors less severity, so that it's lazy
			double score = 1 - traceConflictSeveritySets.get(activities) / maxSeverity;
			traceConflictSeverityScores.put(activities, score);
		}
		
		Map<Set<XEventClass>, Double> scoreMap = new HashMap<>();
		Double maxScore = -1.0;
		for (Set<XEventClass> activities: traceConflictFreqScores.keySet()) {
			double score = WEIGHT_FREQUENCY * traceConflictFreqScores.get(activities) + 
					WEIGHT_SEVERITY * traceConflictSeverityScores.get(activities);
//			System.out.println("[" + getClass().getSimpleName() + "] " + activities + " has score: " + score);
			scoreMap.put(activities, score);
			if (Double.compare(score, maxScore) > 0)
				maxScore = score;
		}
		System.out.println("[" + getClass().getSimpleName() + "] Max score: " + maxScore);
		
		for (Set<XEventClass> activities: scoreMap.keySet()) {
			if (Double.compare(scoreMap.get(activities), maxScore) == 0)
				toRecompose.addAll(activities);
		}
		
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing on activities with max score: " + toRecompose + " with score: " + maxScore);		
		
		// add all the current unsplittables
		toRecompose.addAll(info.getUnsplittables());
		
		return toRecompose;
	}

	public String getName() {
		return NAME;
	}

}
