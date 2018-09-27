package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;

/**
 * This recomposition strategy merges the decomposition by randomly choosing activities with conflicts.
 * Then all open alignments are aligned in the next iteration.
 * 
 * @author Wai Lam Jonathan Lee
 *
 */
public class RandomConflictRecomposeStrategy implements RecomposeStrategy {
	
	public static final String NAME = "Random conflicts";
	
	private Random rand = new Random();

	public Set<XEventClass> recompose(RecomposeInformation info) {
		Map<XEventClass, Double> logConflicts = info.getLogConflicts();
		
		Set<XEventClass> scapegoatActivities = new HashSet<>();
		// need to be at least merging on one activity
		int nofActivitiesToRecompose = rand.nextInt(logConflicts.size()) + 1;
		
		List<XEventClass> candidates = new ArrayList<>(logConflicts.keySet());
		
		for (int i = 0; i < nofActivitiesToRecompose; i++) {
			// choose random activity to recompose on
			int index = rand.nextInt(candidates.size());
			XEventClass activity = candidates.get(index);
			scapegoatActivities.add(activity);
			info.getContext().getProgress().inc();
		}
		
		scapegoatActivities.addAll(info.getUnsplittables());
		
		return scapegoatActivities;
	}

	public String getName() {
		return NAME;
	}
	
}
