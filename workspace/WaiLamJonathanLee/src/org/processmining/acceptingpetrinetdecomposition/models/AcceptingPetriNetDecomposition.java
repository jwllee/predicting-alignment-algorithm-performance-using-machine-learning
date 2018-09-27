package org.processmining.acceptingpetrinetdecomposition.models;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public interface AcceptingPetriNetDecomposition {
	
	/**
	 * Returns the subnet at the corresponding index
	 * 
	 * 
	 * @param index
	 * 			The given index
	 * @return The subnet at the given index, if valid. null if not valid.
	 */
	AcceptingPetriNet getSubnet(int index);
	
	/**
	 * Returns all the subnets in the decomposition
	 * 
	 * 
	 * @return All the subnets 
	 */
	
	AcceptingPetriNetArray getSubnets();

	/**
	 * Returns the overall net
	 * 
	 * 
	 * @return the overall net
	 */
	AcceptingPetriNet getOverallNet();
	
	/**
	 * Returns a list of mapping from overall net elements to subnet elements
	 * 
	 * 
	 * @return a list of mappings between directed graph elements
	 */
	List<Map<DirectedGraphElement, DirectedGraphElement>> getParentChildMaps();
	
	/**
	 * Get the size of the net decomposition
	 * 
	 * 
	 * @return the size of net decomposition, measured by number of subnets
	 */
	int getSize();
	
	/**
	 * Returns the set of transitions from the overall net that are shared between more than one subnet
	 * 
	 * 
	 * @return a set of transitions
	 */
	Set<Transition> getBorderTransitions();
	
	/**
	 * Returns the set of places from the overall net that are shared between more than one subnet
	 * 
	 * 
	 * @return a set of places
	 */
	Set<Place> getBorderPlaces();
	
	/**
	 * Returns the set of edges from the overall net that are shared between more than one subnet
	 * 
	 * 
	 * @return a set of edges
	 */
	Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> getBorderEdges();
	
	/**
	 * Returns a boolean value on whether if the decomposition is a valid decomposition
	 * A valid decomposition requires that only unique, visible transitions are border 
	 * transitions
	 * 
	 * See: Decomposing Petri nets for process mining: A generic approach for more details on the 
	 * 		definition of a valid decomposition
	 * 
	 * 
	 * @return a boolean value
	 */
	boolean isValid();
}
