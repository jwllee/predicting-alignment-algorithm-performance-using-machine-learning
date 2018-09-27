package org.processmining.acceptingpetrinetdecomposition.models;

import java.util.List;
import java.util.Map;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.DirectedGraphElement;

public interface AcceptingPetriNetDecompositionFactory {

	/**
	 * Creates an accepting petri net decomposition from an overall net and the decomposed array of subnets
	 * 
	 * 
	 * @param overallNet
	 * 				The overall accepting petri net that is decomposed
	 * @param subnets
	 * 				The array of subnets created by the decomposition
	 * @return An accepting petri net decomposition
	 */
	AcceptingPetriNetDecomposition makeDecomposition(AcceptingPetriNet overallNet, AcceptingPetriNetArray subnets);
	
	/**
	 * Creates an accepting petri net decomposition from an overall net and the decomposed array of subnets, as well
	 * as a list of mapping from overall net elements to the corresponding subnet elements
	 * 
	 * 
	 * @param overallNet
	 * 				The overall accepting petri net that is decomposed
	 * @param subnets
	 * 				The array of subnets created by the decomposition
	 * @param parentChildMaps
	 * 				A list of mapping from overall net elements to subnet elements, the list order should be the same
	 * 				as the net array order
	 * @return An accepting petri net decomposition
	 */
	AcceptingPetriNetDecomposition makeDecomposition(AcceptingPetriNet overallNet, AcceptingPetriNetArray subnets,
			List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps);
	
}
