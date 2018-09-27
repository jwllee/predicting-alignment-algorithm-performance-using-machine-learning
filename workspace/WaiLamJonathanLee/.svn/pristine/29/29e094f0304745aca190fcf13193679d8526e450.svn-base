package org.processmining.sesedecomposition.utils.rpst;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPST;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.utils.petrinet.PetriNetUtils;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class PetriNetRPSTUtils {
	
	public static PetriNetRPST cloneRPST(PetriNetRPST rpst) {
		
		PetriNetRPST clone = new PetriNetRPST(null, null, null, null);
		
		clone.setName(rpst.getName());
		
		Map<DirectedGraphElement, DirectedGraphElement> map = 
				new HashMap<DirectedGraphElement, DirectedGraphElement>();
		clone.setNet(PetriNetUtils.cloneNet(rpst.getNet(), map));
		
		clone.setTree(new DirectedSparseGraph<PetriNetRPSTNode, String>());
		
		Map<PetriNetRPSTNode, PetriNetRPSTNode> node2node = 
	    		new HashMap<PetriNetRPSTNode, PetriNetRPSTNode>();
		

	    for(PetriNetRPSTNode node: rpst.getTree().getVertices()){
	    	
	    	String id = node.getId();
	        String name = node.getName();
	        String desc = node.getDesc();
	    	
	    	Set<Transition> trans = new HashSet<Transition>();
	    	for(Transition t: node.getTrans()){
	    		trans.add((Transition)map.get(t));
	    	}
	    	Set<Place> places = new HashSet<Place>();
	    	for(Place p: node.getPlaces()){
	    		places.add((Place)map.get(p));
	    	}
	    	
	    	Set<Arc> arcs = new HashSet<Arc>();
	    	for(Arc a: node.getArcs()){
	    		PetrinetNode source = a.getSource();
	    		PetrinetNode target = a.getTarget();
	    		arcs.add(clone.getNet().getNet().getArc((PetrinetNode)map.get(source), (PetrinetNode)map.get(target)));
	    	}
	    	
	    	PetrinetNode entry = (PetrinetNode) map.get(node.getEntry());
	    	PetrinetNode exit = (PetrinetNode) map.get(node.getExit());
	    	
	    	PetriNetRPSTNode cloneNode = new PetriNetRPSTNode(id,name,desc,trans,places,arcs,entry,exit);
	    	node2node.put(node, cloneNode);
	    	clone.getTree().addVertex(cloneNode);
	    }
	    
	    //Root
	    clone.setRoot(node2node.get(rpst.getRoot()));
	    
	    //Create Arcs
	    for(PetriNetRPSTNode node: rpst.getTree().getVertices()){
	    	for(PetriNetRPSTNode child: rpst.getTree().getSuccessors(node)){
	    		String edge = rpst.getTree().findEdge(node, child);
	    		clone.getTree().addEdge(edge, node2node.get(node), node2node.get(child));
	    	}
	    }
		
		return clone;
	}
	
	public static Set<Place> getEntryPlaces(List<PetriNetRPSTNode> nodes) {
		Set<Place> places = new HashSet<Place>();
		for (PetriNetRPSTNode node: nodes) {
			if (node.getEntry() instanceof Place) 
				places.add((Place) node.getEntry());
		}
		return places;
	}
	
	public static Set<Place> getExitPlaces(List<PetriNetRPSTNode> nodes) {
		Set<Place> places = new HashSet<Place>();
		for (PetriNetRPSTNode node: nodes) {
			if (node.getExit() instanceof Place) 
				places.add((Place) node.getExit());
		}
		return places;
	}
}
