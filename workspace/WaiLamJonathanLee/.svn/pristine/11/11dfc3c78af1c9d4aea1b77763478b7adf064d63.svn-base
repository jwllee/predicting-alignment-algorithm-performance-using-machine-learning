package org.processmining.sesedecomposition.utils.petrinet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class PetriNetUtils {
	
	public static Petrinet newPetriNet(String name, Set<Place> places, 
			Set<Transition> trans, Set<Arc> arcs, Map<PetrinetNode, PetrinetNode> map) {
		Petrinet net = PetrinetFactory.newPetrinet(name);
		
		for (Transition t: trans) {
			Transition t0 = net.addTransition(t.getLabel());
			t0.setInvisible(t.isInvisible());
			map.put(t, t0);
		}
		
		for (Place p: places) {
			Place p0 = net.addPlace(p.getLabel());
			map.put(p, p0);
		}
		
		for (Arc arc: arcs) {
			if (places.contains(arc.getSource())) { // Place -> Transition
				net.addArc((Place) map.get(arc.getSource()), 
						(Transition) map.get(arc.getTarget()), arc.getWeight());
			} else if (places.contains(arc.getTarget())) { // Transition -> Place
				net.addArc((Transition) map.get(arc.getSource()), 
						(Place) map.get(arc.getTarget()), arc.getWeight());
			}
		}
		
		return net;
	}
	
	public static AcceptingPetriNet cloneNet(AcceptingPetriNet origNet) {
		Map<DirectedGraphElement, DirectedGraphElement> map = 
				new HashMap<DirectedGraphElement, DirectedGraphElement>();
		return cloneNet(origNet, map);
	}

	public static AcceptingPetriNet cloneNet(AcceptingPetriNet origNet,
			Map<DirectedGraphElement, DirectedGraphElement> map) {
		Petrinet cloneNet = PetrinetFactory.clonePetrinet(origNet.getNet(), map);
		
		//Clone the Initial Marking
		Marking cloneIniM = new Marking();
		for(Place origP: origNet.getInitialMarking()){
			cloneIniM.add((Place) map.get(origP));
		}
		
		//Clone the Final Markings
		Set<Marking> cloneEndMarkings = new HashSet<Marking>();
		for(Marking origEndM: origNet.getFinalMarkings()){
			Marking cloneEndM = new Marking();
			for(Place origP: origEndM){
				cloneEndM.add((Place) map.get(origP));
			}
			cloneEndMarkings.add(cloneEndM);
		}
		
		//Construct the cloned Accepting Petri Net
		AcceptingPetriNet cloneAcceptingNet = AcceptingPetriNetFactory.createAcceptingPetriNet(cloneNet);
		cloneAcceptingNet.setInitialMarking(cloneIniM);
		cloneAcceptingNet.setFinalMarkings(cloneEndMarkings);

		return cloneAcceptingNet;
	}
	
	public static Set<Transition> getInvTransitions(Petrinet net) {
		Set<Transition> invisibles = new LinkedHashSet<Transition>();
		for (Transition t: net.getTransitions()) {
			if (t.isInvisible())
				invisibles.add(t);
		}
		return invisibles;
	}
	
	public static Set<Set<Transition>> getDuplicates(Petrinet net) {
		Map<String, Set<Transition>> mapDuplicates = new HashMap<String, Set<Transition>>();
		
		for (Transition t: net.getTransitions()) {
			String label = t.getLabel();
			Set<Transition> transitions = mapDuplicates.get(label);
			if (transitions == null) {
				transitions = new LinkedHashSet<Transition>();
				mapDuplicates.put(label, transitions);
			}
			transitions.add(t);
		}
		
		Set<Set<Transition>> duplicates = new LinkedHashSet<Set<Transition>>();
		for (Set<Transition> set: mapDuplicates.values()) {
			if (set.size() > 1) 
				duplicates.add(set);
		}
		
		return duplicates;
	}
	
	public static Set<Place> getStartPlaces(Petrinet net) {
		Set<Place> startPlaces = new HashSet<Place>();
		for (Place p: net.getPlaces()) {
			if (net.getInEdges(p).size() == 0) 
				startPlaces.add(p);
		}
		return startPlaces;
	}
	
	public static Set<Place> getEndPlaces(Petrinet net) {
		Set<Place> endPlaces = new HashSet<Place>();
		for (Place p: net.getPlaces()) {
			if (net.getOutEdges(p).size() == 0)
				endPlaces.add(p);
		}
		return endPlaces;
	}
	
}
