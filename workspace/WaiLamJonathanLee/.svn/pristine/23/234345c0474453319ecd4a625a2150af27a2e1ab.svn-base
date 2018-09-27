package org.processmining.tests.acceptingpetrinetdecomposition.models.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.processmining.acceptingpetrinetdecomposition.models.AcceptingPetriNetDecomposition;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class TestAcceptingPetriNetDecompositionImpl {
	
	AcceptingPetriNetDecompositionUtils utils = new AcceptingPetriNetDecompositionUtils();

	@Test
	public void simpleTest() {
		assertEquals(1, 1 + 0);
	}

	@Test
	public void testGetBorderTransitionsForSmallValidDecompositionWithThreeSubnets() {
		AcceptingPetriNetDecomposition decomposition = utils.makeSmallValidDecompositionWithThreeSubnets();
		
		Set<Transition> borderTrans = decomposition.getBorderTransitions();
		
		// all the transitions in the overall net are border transitions
		for (Transition t: decomposition.getOverallNet().getNet().getTransitions()) {
			assertEquals(true, borderTrans.contains(t));
		}
	}
	
	@Test
	public void testGetBorderTransitionsForValidDecompositionWithInternalTransitions() {
		AcceptingPetriNetDecomposition decomposition = utils.makeSmallValidDecompositionWithInternalTransitions();
		
		Set<Transition> borderTrans = decomposition.getBorderTransitions();
		
		// there should be one border transition with label t0;
		assertEquals(1, borderTrans.size());
		for (Transition t: decomposition.getOverallNet().getNet().getTransitions()) {
			if (t.getLabel().equals("t0")) {
				// this is the expected border transition
				assertEquals(true, borderTrans.contains(t));
			}
		}
	}
	
	@Test
	public void testGetBorderPlacesForSmallValidDecompositionWithThreeSubnetsIsEmpty() {
		AcceptingPetriNetDecomposition decomposition = utils.makeSmallValidDecompositionWithThreeSubnets();
		
		Set<Place> borderPlaces = decomposition.getBorderPlaces();
		
		// there should not be any border places
		assertEquals(true, borderPlaces.isEmpty());
	}
	
	@Test
	public void testGetBorderEdgesForSmallValidDecompositionWithThreeSubnetsIsEmpty() {
		AcceptingPetriNetDecomposition decomposition = utils.makeSmallValidDecompositionWithThreeSubnets();
		
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> borderEdges = decomposition.getBorderEdges();
		
		// there should not be any border edges
		assertEquals(true, borderEdges.isEmpty());
	}
	
	@Test
	public void testSmallValidDecompositionWithThreeSubnetsIsValid() {
		AcceptingPetriNetDecomposition decomposition = utils.makeSmallValidDecompositionWithThreeSubnets();
		
		assertEquals(true, decomposition.isValid());
	}
	
	@Test
	public void testSmallInvalidDecompositionWithThreeSubnetsIsNotValid() {
		AcceptingPetriNetDecomposition decomposition = utils.makeInvalidDecompositionWithDuplicatedBorderTransitions();
		
		assertEquals(false, decomposition.isValid());
	}
}
