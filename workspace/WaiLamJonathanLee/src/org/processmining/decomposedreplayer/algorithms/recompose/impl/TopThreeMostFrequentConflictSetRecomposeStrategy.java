package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import org.processmining.decomposedreplayer.algorithms.recompose.TopKMostFrequentConflictSetRecomposeStrategy;

public class TopThreeMostFrequentConflictSetRecomposeStrategy extends TopKMostFrequentConflictSetRecomposeStrategy {

	public static final String NAME = "Top three most frequent conflicts set";
	public static final int K = 3;
	
	public int getK() {
		return K;
	}

	public String getName() {
		return NAME;
	}

}
