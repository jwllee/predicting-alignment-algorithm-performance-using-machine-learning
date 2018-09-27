package org.processmining.decomposedreplayer.algorithms.recompose;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public interface RecomposeStrategy {
	
	/**
	 * Returns a list of activities that are to be set as unsplittable activities for the next
	 * decomposed replay
	 * 
	 * @param info
	 * @return a list of unsplittable activities
	 */
	public Set<XEventClass> recompose(RecomposeInformation info);
	
	public String getName();
	
}
