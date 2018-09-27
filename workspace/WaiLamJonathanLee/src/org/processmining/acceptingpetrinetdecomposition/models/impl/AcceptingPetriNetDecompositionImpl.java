package org.processmining.acceptingpetrinetdecomposition.models.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinetdecomposition.models.AcceptingPetriNetDecomposition;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class AcceptingPetriNetDecompositionImpl implements AcceptingPetriNetDecomposition {
	
	private AcceptingPetriNet overallNet;
	private AcceptingPetriNetArray subnets;
	private List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps;
	
	public AcceptingPetriNetDecompositionImpl(AcceptingPetriNet overallNet, AcceptingPetriNetArray subnets,
			List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps) {
		this.overallNet = overallNet;
		this.subnets = subnets;
		this.parentChildMaps = parentChildMaps;
	}
	
	@Override
	public AcceptingPetriNet getOverallNet() {
		return overallNet;
	}
	
	public void setOverallNet(AcceptingPetriNet overallNet) {
		this.overallNet = overallNet;
	}
	
	@Override
	public AcceptingPetriNet getSubnet(int index) {
		return subnets.getNet(index);
	}
	
	@Override
	public AcceptingPetriNetArray getSubnets() {
		return subnets;
	}
	
	public void setSubnets(AcceptingPetriNetArray subnets) {
		this.subnets = subnets;
	}

	@Override
	public List<Map<DirectedGraphElement, DirectedGraphElement>> getParentChildMaps() {
		return parentChildMaps;
	}

	public void setParentChildMaps(List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps) {
		this.parentChildMaps = parentChildMaps;
	}

	@Override
	public int getSize() {
		return subnets.getSize();
	}
	
	public Set<Transition> getBorderTransitions() {
		// get the sets of overall net transitions that each subnet relates to
		List<Set<Transition>> overallNetTrans = new ArrayList<>();
		
		for (int i = 0; i < subnets.getSize(); i++) {
			
			Map<DirectedGraphElement, DirectedGraphElement> parentChildMap = parentChildMaps.get(i);

			Set<Transition> overallNetTransAtSubnet = new HashSet<>();

			for (DirectedGraphElement element: parentChildMap.keySet()) {	
				if (element instanceof Transition) 
					overallNetTransAtSubnet.add((Transition) element);
			}
			
			overallNetTrans.add(overallNetTransAtSubnet);
		}
		
		// the transitions that are shared amongst more than one subnet are border transitions
		Set<Transition> borderTrans = new HashSet<>();
		
		for (int i = 0; i < overallNetTrans.size(); i++) {
			
			Set<Transition> transAtSubnet0 = overallNetTrans.get(i);
			
			for (int j = 0; j < overallNetTrans.size(); j++) {
				
				// dont check the same sets
				if (i == j) continue;
				
				Set<Transition> transAtSubnet1 = overallNetTrans.get(j);
				Set<Transition> borderTransBetweenTwoSubnets = new HashSet<>(transAtSubnet0);
				borderTransBetweenTwoSubnets.retainAll(transAtSubnet1);
				borderTrans.addAll(borderTransBetweenTwoSubnets);
				
			}
			
		}
		
		// verify that all border transitions are in overall net
		for (Transition trans: borderTrans) {
			if (!overallNet.getNet().getTransitions().contains(trans)) {
				String msg = "[" + getClass().getSimpleName() + "] Overall net does not have transition: " + trans.toString(); 
				throw new IllegalStateException(msg);
			}
		}
		
		return borderTrans;
	}

	public Set<Place> getBorderPlaces() {
		// get the set of places in the overall net that each subnet relates to
		List<Set<Place>> overallNetPlaces = new ArrayList<>();
		
		for (int i = 0; i < subnets.getSize(); i++) {
			
			Map<DirectedGraphElement, DirectedGraphElement> parentChildMap = parentChildMaps.get(i);

			Set<Place> overallNetPlacesAtSubnet = new HashSet<>();
			
			for (DirectedGraphElement element: parentChildMap.keySet()) {	
				if (element instanceof Place)
					overallNetPlacesAtSubnet.add((Place) element);
			}
			
			overallNetPlaces.add(overallNetPlacesAtSubnet);
		}
		
		// the places that are shared amongst more than one subnet are border places
		Set<Place> borderPlaces = new HashSet<>();
		
		for (int i = 0; i < overallNetPlaces.size(); i++) {
			
			Set<Place> placesAtSubnet0 = overallNetPlaces.get(i);

			for (int j = 0; j < overallNetPlaces.size(); j++) {
				
				// dont check the same sets
				if (i == j) continue;
				
				Set<Place> placesAtSubnet1 = overallNetPlaces.get(j);
				Set<Place> borderPlacesBetweenTwoSubnets = new HashSet<>(placesAtSubnet0);
				borderPlacesBetweenTwoSubnets.retainAll(placesAtSubnet1);
				borderPlaces.addAll(borderPlacesBetweenTwoSubnets);

			}
			
		}
		
		// verify that all border places are in overall net
		for (Place place: borderPlaces) {
			if (!overallNet.getNet().getPlaces().contains(place)) {
				String msg = "[" + getClass().getSimpleName() + "] Overall net does not have place: " + place.toString();
				throw new IllegalStateException(msg);
			}
		}

		return borderPlaces;
	}

	public Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> getBorderEdges() {
		// get the set of edges in the overall net that each subnet relates to
		List<Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>> overallNetEdges = new ArrayList<>();
		
		for (int i = 0; i < subnets.getSize(); i++) {
			
			Map<DirectedGraphElement, DirectedGraphElement> parentChildMap = parentChildMaps.get(i);

			Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> overallNetEdgesAtSubnet = new HashSet<>();
			
			for (DirectedGraphElement element: parentChildMap.keySet()) {	
				if (element instanceof Arc)
					overallNetEdgesAtSubnet.add((PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>) element);
			}
			
			overallNetEdges.add(overallNetEdgesAtSubnet);
		}
		
		// the edges that are shared amongst more than one subnet are border edges
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> borderEdges = new HashSet<>();
		
		for (int i = 0; i < overallNetEdges.size(); i++) {
			
			Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edgesAtSubnet0 = overallNetEdges.get(i);

			for (int j = 0; j < overallNetEdges.size(); j++) {
				
				// dont check the same sets
				if (i == j) continue;
				
				Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edgesAtSubnet1 = overallNetEdges.get(j);
				Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> borderEdgesBetweenTwoSubnets = new HashSet<>(edgesAtSubnet0);
				borderEdgesBetweenTwoSubnets.retainAll(edgesAtSubnet1);
				borderEdges.addAll(borderEdgesBetweenTwoSubnets);

			}
			
		}
		
		// verify that all border edges are in overall net
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: borderEdges) {
			if (!overallNet.getNet().getEdges().contains(edge)) {
				String msg = "[" + getClass().getSimpleName() + "] Overall net does not have edge: " + edge.toString();
				throw new IllegalStateException(msg);
			}
		}

		return borderEdges;
	}

	public boolean isValid() {
		Set<Transition> borderTrans = getBorderTransitions();
		// check that none of the border transitions are invisible or with duplicated activity labels
		for (Transition transition: borderTrans) {
			if (transition.isInvisible())
				return false;
		}
		Set<String> seenSubnetActivityLabels = new HashSet<>();
		for (Transition transition: borderTrans) {
			if (seenSubnetActivityLabels.contains(transition.getLabel())) 
				return false;
			seenSubnetActivityLabels.add(transition.getLabel());
		}
		Set<String> seenActivityLabels = new HashSet<>();
		Set<String> duplicatedActivityLabels = new HashSet<>();
		for (Transition transition: overallNet.getNet().getTransitions()) {
			if (seenActivityLabels.contains(transition.getLabel()))
				duplicatedActivityLabels.add(transition.getLabel());
			seenActivityLabels.add(transition.getLabel());
		}
		// check that none of the border transition activity label has a duplicated activity label
		for (Transition transition: borderTrans) {
			if (duplicatedActivityLabels.contains(transition.getLabel())) 
				return false;
		}
		return true;
	}
	
}
