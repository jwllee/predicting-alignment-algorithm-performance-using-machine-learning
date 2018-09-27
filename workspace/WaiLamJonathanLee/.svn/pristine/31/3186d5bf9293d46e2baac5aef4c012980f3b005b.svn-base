package org.processmining.decomposedreplayer.models.recompose;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public class MergeConflictScoreSet implements Comparable<MergeConflictScoreSet>{

	private Set<XEventClass> conflictActivitySet;
	private double score;
	
	public MergeConflictScoreSet(Set<XEventClass> conflictActivitySet, double score) {
		this.setConflictActivitySet(conflictActivitySet);
	}

	public Set<XEventClass> getConflictActivitySet() {
		return conflictActivitySet;
	}

	public void setConflictActivitySet(Set<XEventClass> conflictActivitySet) {
		this.conflictActivitySet = conflictActivitySet;
	}

	public int compareTo(MergeConflictScoreSet o) {
		if (Double.compare(this.score, o.score) < 0) 		
			return -1;
		else if (Double.compare(this.score, o.score) == 0) 
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
		return this.conflictActivitySet.toString() + ", score: " + score;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MergeConflictScoreSet))
			return false;
		MergeConflictScoreSet other = (MergeConflictScoreSet) o;
		return conflictActivitySet.equals(other.conflictActivitySet);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
