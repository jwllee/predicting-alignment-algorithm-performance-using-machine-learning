package org.processmining.decomposedreplayer.models.recompose;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.models.utils.Weighted;

public class MergeConflictSet implements Comparable<MergeConflictSet>, Weighted{

	private Set<XEventClass> conflictActivitySet;
	private int frequency;
	
	public MergeConflictSet(Set<XEventClass> conflictActivitySet, int frequency) {
		this.setConflictActivitySet(conflictActivitySet);
		this.setFrequency(frequency);
	}

	public Set<XEventClass> getConflictActivitySet() {
		return conflictActivitySet;
	}

	public void setConflictActivitySet(Set<XEventClass> conflictActivitySet) {
		this.conflictActivitySet = conflictActivitySet;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int compareTo(MergeConflictSet o) {
		if (this.frequency < o.frequency) 		
			return -1;
		else if (this.frequency == o.frequency) 
			return 0;
		else			
			return 1;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for (XEventClass activity: conflictActivitySet)
			hash += activity.getId().hashCode();
		return hash;
	}
	
	@Override
	public String toString() {
		return this.conflictActivitySet.toString() +
				", size: " + conflictActivitySet.size() + ", frequency: " + frequency;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MergeConflictSet))
			return false;
		MergeConflictSet other = (MergeConflictSet) o;
		return conflictActivitySet.equals(other.conflictActivitySet);
	}

	public int getWeight() {
		// using set size as weight
		return this.conflictActivitySet.size();
	}
}
