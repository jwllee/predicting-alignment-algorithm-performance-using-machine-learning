package org.processmining.decomposedreplayer.algorithms.recompose;

import org.processmining.decomposedreplayer.algorithms.recompose.impl.AllConflictRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.LazyRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.MergeConflictGraphRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.MostFrequentConflictRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.RandomConflictRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.ScoreBasedRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopFiveMergeConflictSetGraphRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopFiveMinimalDecompositionRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopFiveMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopFiveMostFrequentConflictSetRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopOneMostFrequentConflictSetRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopTenMergeConflictSetGraphRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopTenMostFrequentConflictSetRecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.impl.TopThreeMostFrequentConflictSetRecomposeStrategy;

public class RecomposeStrategyFactory {

	public static RecomposeStrategy createRecomposeStrategy(String strategy) {
		
		switch (strategy) {
			case MostFrequentConflictRecomposeStrategy.NAME:
				return new MostFrequentConflictRecomposeStrategy();
				
			case RandomConflictRecomposeStrategy.NAME:
				return new RandomConflictRecomposeStrategy();
				
			case AllConflictRecomposeStrategy.NAME:
				return new AllConflictRecomposeStrategy();
				
			case TopOneMostFrequentConflictSetRecomposeStrategy.NAME:
				return new TopOneMostFrequentConflictSetRecomposeStrategy();
				
			case TopThreeMostFrequentConflictSetRecomposeStrategy.NAME:
				return new TopThreeMostFrequentConflictSetRecomposeStrategy();
				
			case TopFiveMostFrequentConflictSetRecomposeStrategy.NAME:
				return new TopFiveMostFrequentConflictSetRecomposeStrategy();
				
			case TopTenMostFrequentConflictSetRecomposeStrategy.NAME:
				return new TopTenMostFrequentConflictSetRecomposeStrategy();
				
			case TopFiveMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy.NAME:
				return new TopFiveMostFrequentConflictSetOnInitialDecompositionRecomposeStrategy();
				
			case TopFiveMinimalDecompositionRecomposeStrategy.NAME:
				return new TopFiveMinimalDecompositionRecomposeStrategy();
				
			case TopFiveMergeConflictSetGraphRecomposeStrategy.NAME:
				return new TopFiveMergeConflictSetGraphRecomposeStrategy();
				
			case TopTenMergeConflictSetGraphRecomposeStrategy.NAME:
				return new TopTenMergeConflictSetGraphRecomposeStrategy();
				
			case ScoreBasedRecomposeStrategy.NAME:
				return new ScoreBasedRecomposeStrategy();
				
			case LazyRecomposeStrategy.NAME:
				return new LazyRecomposeStrategy();
				
			case MergeConflictGraphRecomposeStrategy.NAME:
				return new MergeConflictGraphRecomposeStrategy();
				
			default:
				throw new IllegalArgumentException("Do not recognize strategy: " + strategy);
		}
		
	}
	
}
