package org.processmining.tests.decomposedreplayer.algorithms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.processmining.decomposedreplayer.algorithms.replay.AbstractRecomposingReplayAlgorithm;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class TestAbstractRecomposingReplayAlgorithm {

	@Test
	public void simpleTest() {
		assertEquals(2, 1 + 1);
	}
	
	@Test
	public void testEachAlgorithmStepGetsCalled() {
		MockRecomposingReplayAlgorithm mock = new MockRecomposingReplayAlgorithm();
		mock.recomposeReplay();
		assertEquals(true, mock.calledInitialDecompose);
		assertEquals(true, mock.calledStopReplay);
		assertEquals(true, mock.calledRecompose);
		assertEquals(true, mock.calledDecomposedReplay);
		assertEquals(true, mock.calledMergeIterationResults);
		assertEquals(true, mock.calledWrapUp);
	}
	
	@Test
	public void testAlgorithmTerminates() {
		MockRecomposingReplayAlgorithm mock = new MockRecomposingReplayAlgorithm();
		mock.recomposeReplay();
		assertEquals(true, mock.calledWrapUp);
	}
	
	@Test
	public void testValidateIterationCorrectness() {
		MockRecomposingReplayAlgorithm mock = new MockRecomposingReplayAlgorithm();
		mock.recomposeReplay();
		assertEquals(mock.extraIterationCounter, mock.getIteration());
	}
	
	@Test
	public void testReplayTerminatesAtCorrectIteration() {
		MockRecomposingReplayAlgorithm mock = new MockRecomposingReplayAlgorithm();
		mock.recomposeReplay();
		assertEquals(mock.iterationToStopReplay, mock.getIteration());
		assertEquals(true, mock.calledStopReplay);
	}
	
	@Test
	public void testInitiateWithZeroIteration() {
		MockRecomposingReplayAlgorithm mock = new MockRecomposingReplayAlgorithm();
		// set stop replay to true to not do any replay
		mock.iterationToStopReplay = 0;
		mock.recomposeReplay();
		assertEquals(0, mock.getIteration());
	}

	// mock class to test logic of recomposing replay algorithm
	class MockRecomposingReplayAlgorithm extends AbstractRecomposingReplayAlgorithm {
		public int extraIterationCounter = 0;
		public int iterationToStopReplay = 10;
		
		public boolean calledInitialDecompose = false;
		public boolean calledStopReplay = false;
		public boolean calledRecompose = false;
		public boolean calledDecomposedReplay = false;
		public boolean calledMergeIterationResults = false;
		public boolean calledWrapUp = false;

		protected void initialDecompose() {
			calledInitialDecompose = true;
		}

		protected boolean stopReplay() {
			calledStopReplay = true;
			return getIteration() >= iterationToStopReplay;
		}

		protected void recompose() {
			calledRecompose = true;
		}

		protected void decomposedReplay() {
			calledDecomposedReplay = true;
			extraIterationCounter++;
		}

		protected void mergeIterationResults() {
			calledMergeIterationResults = true;
		}

		protected void wrapUp() {
			calledWrapUp = true;
		}

		public PNRepResult apply() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
