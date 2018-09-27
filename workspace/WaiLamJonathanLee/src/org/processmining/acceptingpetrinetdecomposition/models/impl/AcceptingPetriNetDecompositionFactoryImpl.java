package org.processmining.acceptingpetrinetdecomposition.models.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinetdecomposition.models.AcceptingPetriNetDecomposition;
import org.processmining.acceptingpetrinetdecomposition.models.AcceptingPetriNetDecompositionFactory;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class AcceptingPetriNetDecompositionFactoryImpl implements AcceptingPetriNetDecompositionFactory {

	public AcceptingPetriNetDecomposition makeDecomposition(AcceptingPetriNet overallNet,
			AcceptingPetriNetArray subnets) {
		// need to do make parent child mappings from overall net to subnets
		Petrinet net = overallNet.getNet();
		
		List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps = new ArrayList<>();
		Set<Transition> mappedTransAll = new HashSet<>();
		Set<Place> mappedPlacesAll = new HashSet<>();
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> mappedEdgesAll = new HashSet<>();
		for (int i = 0; i < subnets.getSize(); i++) {
			AcceptingPetriNet subApn = subnets.getNet(i);
			Petrinet subnet = subApn.getNet();
			
			Map<DirectedGraphElement, DirectedGraphElement> parentChildMap = new HashMap<>();
			// process arcs in subnet
			Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> seenEdges = new HashSet<>();
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e0: subnet.getEdges()) {
				boolean assigned = false;
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e1: net.getEdges()) {
					if (e0.getSource().getLabel().equals(e1.getSource().getLabel()) &&
							e0.getTarget().getLabel().equals(e1.getTarget().getLabel()) &&
							e0.getSource().getClass().equals(e1.getSource().getClass()) &&
							e0.getTarget().getClass().equals(e1.getTarget().getClass())) {
						// equality is checked by examining the equality of the source and target of the edges
						if (seenEdges.contains(e1)) {
							// need to make sure that this overall net edge has not been seen before
							String msg = "[" + getClass().getSimpleName() + "] Trying to map overall net edge: " + e1.toString() + " to multiple subnet edges!";
							throw new IllegalStateException(msg);
						}
						
						parentChildMap.put(e1, e0);
						assigned = true;
						
						// should not need to map to the seen edge again
						seenEdges.add(e1);
						// add to overall mapped edges
						mappedEdgesAll.add(e1);
					}
				}
				if (!assigned) {
					String msg = "[" + getClass().getSimpleName() + "] Cannot map subnet edge: " + e0.toString() + " to an edge in the overall net!";
					throw new IllegalStateException(msg);
				}
			}
			
			// we were able to map all subnet edges to a corresponding overall net edge
			// use that to map the subnet transitions and places
			// need to keep track of all the overall net transitions and places that have been
			// mapped to a subnet transition and place
			Set<Transition> mappedOverallT = new HashSet<>();
			Set<Place> mappedOverallP = new HashSet<>();
			Map<PetrinetNode, PetrinetNode> parentChildMapT = new HashMap<>();
			Map<PetrinetNode, PetrinetNode> parentChildMapP = new HashMap<>();
			for (DirectedGraphElement key: parentChildMap.keySet()) {
				// for brevity, parent means overallNet and child means subnet
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> parentEdge = 
						(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>) key;
				PetrinetNode parentSrc = parentEdge.getSource();
				PetrinetNode parentTarget = parentEdge.getTarget();
				
				PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> childEdge = 
						(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>) parentChildMap.get(key);
				
				PetrinetNode childSrc = childEdge.getSource();
				PetrinetNode childTarget = childEdge.getTarget();
				
				if (parentSrc instanceof Transition) {
					Transition parentSrcTransition = (Transition) parentSrc;
					Transition childSrcTransition = (Transition) childSrc;
					parentChildMapT.put(parentSrcTransition, childSrcTransition);
					mappedOverallT.add(parentSrcTransition);
					mappedTransAll.add(parentSrcTransition);
				} else {
					Place parentSrcPlace = (Place) parentSrc;
					Place childSrcPlace = (Place) childSrc;
					parentChildMapP.put(parentSrcPlace, childSrcPlace);
					mappedOverallP.add(parentSrcPlace);
					mappedPlacesAll.add(parentSrcPlace);
				}
				
				if (parentTarget instanceof Transition) {
					Transition parentTargetTransition = (Transition) parentTarget;
					Transition childTargetTransition = (Transition) childTarget;
					parentChildMapT.put(parentTargetTransition, childTargetTransition);
					mappedOverallT.add(parentTargetTransition);
					mappedTransAll.add(parentTargetTransition);
				} else {
					Place parentTargetPlace = (Place) parentTarget;
					Place childTargetPlace = (Place) childTarget;
					parentChildMapP.put(parentTargetPlace, childTargetPlace);
					mappedOverallP.add(parentTargetPlace);
					mappedPlacesAll.add(parentTargetPlace);
				}
			}
			// update the parentChildMap with mapped transitions and places
			parentChildMap.putAll(parentChildMapT);
			parentChildMap.putAll(parentChildMapP);
			
			// there might be transitions that are not connected to anything
			for (Transition childT: subnet.getTransitions()) {
				if (!parentChildMap.values().contains(childT)) {
					// this is a floating transition that has not been mapped yet
					boolean assigned = false;
					for (Transition parentT: net.getTransitions()) {
						if (!net.getInEdges(parentT).isEmpty() || 
								!net.getOutEdges(parentT).isEmpty()) 
							// need to be a floating transition not connected to anything
							continue;
						if (mappedOverallT.contains(parentT))
							// avoid mapping the same overall net transition to multiple subnet transitions
							continue;
						if (childT.getLabel().equals(parentT.getLabel())) {
							parentChildMap.put(parentT, childT);
							assigned = true;
							mappedOverallT.add(parentT);
							mappedTransAll.add(parentT);
						}
					}
					if (!assigned) {
						String msg = "[" + getClass().getSimpleName() + "] Cannot map subnet transition: " + childT.toString() + " to a transition in the overall net!";
						throw new IllegalStateException(msg);
					}
				}
			}
			
			// there might be places that are not connected to anything
			for (Place childP: subnet.getPlaces()) {
				if (!parentChildMap.values().contains(childP)) {
					// this is a floating place that has not been mapped yet
					boolean assigned = false;
					for (Place parentP: net.getPlaces()) {
						if (!net.getInEdges(parentP).isEmpty() ||
								!net.getOutEdges(parentP).isEmpty()) 
							continue;
						if (mappedOverallP.contains(parentP)) 
							continue;
						if (childP.getLabel().equals(parentP.getLabel())) {
							parentChildMap.put(parentP, childP);
							assigned = true;
							mappedOverallP.add(parentP);
							mappedPlacesAll.add(parentP);
						}
					}
					if (!assigned) {
						// we have a problem if we cannot map a place from subnet to a place in the overall net
						String msg = "[" + getClass().getSimpleName() + "] Cannot map subnet place: " + childP.toString() + " to a place in the overall net!";
						throw new IllegalStateException(msg);
					}
				}
			}
			
			// add the parentChildMap to the list
			parentChildMaps.add(parentChildMap);
		}
		
		// check that all the transitions, places, and arcs have been mapped to some element in the subnets
		if (!net.getTransitions().containsAll(mappedTransAll)) {
			Set<Transition> trans = new HashSet<>(net.getTransitions());
			trans.removeAll(mappedTransAll);
			String msg = "[" + getClass().getSimpleName() + "] Have not mapped these transitions to subnet elements: " + trans.toString();
			throw new IllegalStateException(msg);
		}
		
		if (!net.getPlaces().containsAll(mappedPlacesAll)) {
			Set<Place> places = new HashSet<>(net.getPlaces());
			places.removeAll(mappedPlacesAll);
			String msg = "[" + getClass().getSimpleName() + "] Have not mapped these places to subnet elements: " + places.toString();
			throw new IllegalStateException(msg);
		}
		
		if (!net.getEdges().containsAll(mappedEdgesAll)) {
			Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = new HashSet<>(net.getEdges());
			edges.removeAll(mappedEdgesAll);
			String msg = "[" + getClass().getSimpleName() + "] Have not mapped these edges to subnet elements: " + edges.toString();
			throw new IllegalStateException(msg);
		}

		return makeDecomposition(overallNet, subnets, parentChildMaps);
	}

	public AcceptingPetriNetDecomposition makeDecomposition(AcceptingPetriNet overallNet,
			AcceptingPetriNetArray subnets, List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps) {
		AcceptingPetriNetDecomposition decomposition = new AcceptingPetriNetDecompositionImpl(overallNet, subnets, parentChildMaps);
		return decomposition;
	}

}
