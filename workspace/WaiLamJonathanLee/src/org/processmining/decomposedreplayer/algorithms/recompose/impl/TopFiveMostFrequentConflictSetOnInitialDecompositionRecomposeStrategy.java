package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import org.processmining.decomposedreplayer.algorithms.recompose.TopKMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy;

public class TopFiveMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy extends TopKMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy {

	public static final String NAME = "Top five most frequent conflicts set on initial decomposition";
	public static final int K = 5;
	public static final int NUM_OF_INITIAL_DECOMPOSITION_ROUNDS = 3;
	
	public int getK() {
		return K;
	}

	public String getName() {
		return NAME;
	}

	public int getNofInitialDecomposition() {
		return NUM_OF_INITIAL_DECOMPOSITION_ROUNDS;
	}

}
