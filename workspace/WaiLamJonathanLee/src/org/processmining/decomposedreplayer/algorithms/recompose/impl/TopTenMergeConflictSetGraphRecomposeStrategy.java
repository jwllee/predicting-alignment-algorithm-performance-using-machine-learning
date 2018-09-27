package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import org.processmining.decomposedreplayer.algorithms.recompose.TopKMergeConflictSetGraphRecomposeStrategy;

public class TopTenMergeConflictSetGraphRecomposeStrategy extends TopKMergeConflictSetGraphRecomposeStrategy {

	public static final String NAME = "Top ten merge conflict set graph";
	public static final int K = 10;
	public static final int S = 20;
	
	public String getName() {
		return NAME;
	}

	public int getK() {
		return K;
	}

	public int getS() {
		return S;
	}

}
