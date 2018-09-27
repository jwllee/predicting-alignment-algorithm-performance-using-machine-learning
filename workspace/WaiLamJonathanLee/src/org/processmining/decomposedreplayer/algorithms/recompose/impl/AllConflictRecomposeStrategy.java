package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;

/**
 * This recomposition strategy merges the decomposition by all the conflicts.
 * Then all open alignments are aligned in the next iteration.
 * 
 * @author Wai Lam Jonathan Lee
 *
 */
public class AllConflictRecomposeStrategy implements RecomposeStrategy {
	
	public static final String NAME = "All conflicts";
	
	public Set<XEventClass> recompose(RecomposeInformation info) {
		Set<XEventClass> unsplittables = new HashSet<>(info.getLogConflicts().keySet());
		
		unsplittables.addAll(info.getUnsplittables());
		
		return unsplittables;
	}

	public String getName() {
		return NAME;
	}

}
