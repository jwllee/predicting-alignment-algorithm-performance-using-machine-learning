package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import org.processmining.decomposedreplayer.algorithms.recompose.TopKMostFrequentConflictSetRecomposeStrategy;

public class TopTenMostFrequentConflictSetRecomposeStrategy extends TopKMostFrequentConflictSetRecomposeStrategy {

	public static final String NAME = "Top ten most frequent conflicts set";
	public static final int K = 10;
	
	public int getK() {
		return K;
	}

	public String getName() {
		return NAME;
	}

}
