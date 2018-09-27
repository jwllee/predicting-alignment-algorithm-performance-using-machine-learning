package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import org.processmining.decomposedreplayer.algorithms.recompose.TopKMinimalDecompositionRecomposeStrategy;

public class TopFiveMinimalDecompositionRecomposeStrategy extends TopKMinimalDecompositionRecomposeStrategy {

	public static final String NAME = "Top five minimal decomposition";
	
	public static final int K = 10;
	
	public String getName() {
		return NAME;
	}

	public int getK() {
		return K;
	}

}
