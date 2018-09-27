package org.processmining.sesedecomposition.utils.dc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetArrayFactory;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.utils.petrinet.PetriNetUtils;
import org.processmining.sesedecomposition.utils.rpst.PetriNetRPSTUtils;

public class BridgingUtils {

	public static AcceptingPetriNetArray bridging(AcceptingPetriNet apn, List<PetriNetRPSTNode> nodes) {
		
		Petrinet net = apn.getNet();
		
		Set<Place> startPlace = PetriNetUtils.getStartPlaces(net);
		Set<Place> endPlace = PetriNetUtils.getEndPlaces(net);
		
		Set<Place> entryPlaces = PetriNetRPSTUtils.getEntryPlaces(nodes);
		Set<Place> exitPlaces =  PetriNetRPSTUtils.getExitPlaces(nodes);
		
		Set<Place> sharedPlaces = new HashSet<Place>();
		sharedPlaces.addAll(entryPlaces);
		sharedPlaces.addAll(exitPlaces);
		sharedPlaces.removeAll(startPlace);
		sharedPlaces.removeAll(endPlace);
		
		AcceptingPetriNetArray netArray = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray();
		netArray.init();
		
		for (PetriNetRPSTNode node: nodes) {
			AcceptingPetriNet s = computeSESE(apn, node, sharedPlaces);
			if (s.getNet().getEdges().size() > 0) 
				netArray.addNet(s);
		}
		
		for (Place sharedPlace: sharedPlaces) {
			AcceptingPetriNet b = computeBridge(apn, sharedPlace);
			if (b.getNet().getEdges().size() > 0)
				netArray.addNet(b);
		}
		
		return netArray;
	}
	
	private static AcceptingPetriNet computeSESE(AcceptingPetriNet net, PetriNetRPSTNode sese, 
			Set<Place> sharedPlaces) {
		
		//Compute the remaining Arcs and Nodes (after removing the ones connected with shared places)
		Set<Arc> remainArcs = new HashSet<Arc>();
		for(Arc arc : sese.getArcs()){	
			if( !sharedPlaces.contains(arc.getSource()) && !sharedPlaces.contains(arc.getTarget())){
				remainArcs.add(arc);
			}
		}
		Set<Transition> remainTrans = new HashSet<Transition>();
		Set<Place> remainPlaces = new HashSet<Place>();
		for(Arc arc: remainArcs){
			if(arc.getSource() instanceof Transition){
				remainTrans.add((Transition)arc.getSource());
			}
			else if(arc.getSource() instanceof Place){
				remainPlaces.add((Place)arc.getSource());
			}
			if(arc.getTarget() instanceof Transition){
				remainTrans.add((Transition)arc.getTarget());
			}
			else if(arc.getTarget() instanceof Place){
				remainPlaces.add((Place)arc.getTarget());
			}
		}
	
		//Construct a new Petri Net with the remaining arcs and nodes
		
		Map<PetrinetNode, PetrinetNode> map = new HashMap<PetrinetNode, PetrinetNode>();
		Petrinet newPN = PetriNetUtils.newPetriNet(sese.getName(), remainPlaces, 
				remainTrans, remainArcs, map);
		
		//Compute the new initial and final markings
		Marking newIniM = new Marking();
		for(Place p: net.getInitialMarking()){
			if( remainPlaces.contains(p)){
				newIniM.add((Place)map.get(p));
			}
		}
		Set<Marking> newEndMarkings = new HashSet<Marking>();
		for(Marking origEndM: net.getFinalMarkings()){
			Marking newEndM = new Marking();
			for(Place origP: origEndM){
				if(remainPlaces.contains(origP)){
					newEndM.add((Place)map.get(origP));
				}
			}
			newEndMarkings.add(newEndM);
		}
		
		//Construct the Accepting Petri Net
		AcceptingPetriNet acceptingNet = AcceptingPetriNetFactory.createAcceptingPetriNet(newPN, 
				newIniM, newEndMarkings);
		return acceptingNet;		
	}

	private static AcceptingPetriNet computeBridge(AcceptingPetriNet net, Place b) {
		//Get the transitions to include in the component
		Set<Transition> transitions = new HashSet<Transition>();
		Set<Transition> inT = new HashSet<Transition>();
		Set<Transition> outT = new HashSet<Transition>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: 
			net.getNet().getInEdges(b)){
			transitions.add((Transition)edge.getSource());
			inT.add((Transition)edge.getSource());
		}
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge: 
			net.getNet().getOutEdges(b)){
			transitions.add((Transition)edge.getTarget());
			outT.add((Transition)edge.getTarget());
		}
		
		//Construct a new Petri Net with the remaining arcs and nodes
		Map<PetrinetNode, PetrinetNode> map = new HashMap<PetrinetNode, PetrinetNode>();
		Petrinet newPN = PetrinetFactory.newPetrinet("Br"+b.getLabel());	
		for(Transition t: transitions){
			Transition newT = newPN.addTransition(t.getLabel());
			newT.setInvisible(t.isInvisible());
			map.put(t, newT);
		}
		Place newP = newPN.addPlace(b.getLabel());
		for(Transition t: inT){
			newPN.addArc((Transition)map.get(t), newP, net.getNet().getArc(t, b).getWeight());
		}
		for(Transition t: outT){
			newPN.addArc(newP, (Transition)map.get(t), net.getNet().getArc(b, t).getWeight());
		}
		
		//Compute the new initial and final markings
		Marking newIniM = new Marking();
		for(Place p: net.getInitialMarking()){
			if( b == p){
				newIniM.add((Place)map.get(p));
			}
		}
		Set<Marking> newEndMarkings = new HashSet<Marking>();
		for(Marking origEndM: net.getFinalMarkings()){
			Marking newEndM = new Marking();
			for(Place origP: origEndM){
				if(b== origP){
					newEndM.add((Place)map.get(origP));
				}
			}
			newEndMarkings.add(newEndM);
		}
		
		//Construct the Accepting Petri Net
		AcceptingPetriNet acceptingNet = AcceptingPetriNetFactory.createAcceptingPetriNet(newPN, 
				newIniM, newEndMarkings);
		return acceptingNet;
		
	}
}
