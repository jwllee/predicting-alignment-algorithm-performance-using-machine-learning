package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;

/**
 * This recomposition strategy merges the decomposition by the conflicts with the most frequency.
 * Then all open alignments are to be aligned in the next iteration.
 * 
 * @author Wai Lam Jonathan Lee
 *
 */
public class MostFrequentConflictRecomposeStrategy implements RecomposeStrategy {
	
	public static final String NAME = "Most frequent conflicts";

	public Set<XEventClass> recompose(RecomposeInformation info) {
		Map<XEventClass, Double> logConflicts = info.getLogConflicts();
		Set<XEventClass> scapegoatActivities = new HashSet<XEventClass>();
		/*
		 * Create a new set of activities-to-merge based on the conflicts.
		 */
		Double max = 0.0;
		for (XEventClass activity : logConflicts.keySet()) {
			if (Double.compare(logConflicts.get(activity), max) > 0) {
				max = logConflicts.get(activity);
			}
		}
		for (XEventClass activity : logConflicts.keySet()) {
			if (Double.compare(logConflicts.get(activity), max) == 0) {
				scapegoatActivities.add(activity);
				info.getContext().getProgress().inc();
			}
		}
		System.out.println("[" + getClass().getSimpleName() + "] Max conflict activities: " + 
				scapegoatActivities + " with score: " + max);
		
		// add all the current unsplittables
		scapegoatActivities.addAll(info.getUnsplittables());
		
		return scapegoatActivities;
	}

	public String getName() {
		return NAME;
	}
	
}
