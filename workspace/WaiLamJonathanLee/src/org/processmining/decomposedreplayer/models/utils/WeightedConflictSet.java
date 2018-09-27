package org.processmining.decomposedreplayer.models.utils;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public class WeightedConflictSet implements Weighted {

	private final Set<XEventClass> conflictSet;
	
	public WeightedConflictSet(Set<XEventClass> conflictSet) {
		this.conflictSet = conflictSet;
	}
	
	public int getWeight() {
		return conflictSet.size();
	}

	public Set<XEventClass> getConflictSet() {
		return conflictSet;
	}
	
}
