package org.processmining.sesedecomposition.utils.rpst;

import java.util.HashMap;
import java.util.Map;

import org.jbpt.petri.Flow;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * Class to wrap the Petri Net implementations of both tools, ProM and jbpt.
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
public class ProMJBPTPetriNet {
	
	private Petrinet prom;
	
	private PetriNet jbpt;
	
	private Map<PetrinetNode,Node> promNode2jbptNode;
	
	private Map<Node,PetrinetNode> jbptNode2promNode;
	
	private Map<Flow, Arc> jbptArc2promArc;

	
	public ProMJBPTPetriNet( Petrinet net){
		this.promNode2jbptNode = new HashMap<PetrinetNode,Node>();
		this.jbptNode2promNode = new HashMap<Node,PetrinetNode>();
		this.jbptArc2promArc = new HashMap<Flow,Arc>();
		this.prom = net;
		prom2jbpt(net);
	}
	
	private void prom2jbpt (Petrinet prom){
		this.jbpt = new PetriNet();
		
		//Transitions
		for(Transition promT: this.prom.getTransitions()){
			org.jbpt.petri.Transition jbptT = new org.jbpt.petri.Transition(promT.getLabel());
			this.jbpt.addTransition(jbptT);
			this.promNode2jbptNode.put(promT, jbptT);
			this.jbptNode2promNode.put(jbptT, promT);
		}
		
		//Places
		for(Place promP: this.prom.getPlaces()){
			org.jbpt.petri.Place jbptP = new org.jbpt.petri.Place(promP.getLabel());
			this.jbpt.addPlace(jbptP);
			this.promNode2jbptNode.put(promP, jbptP);
			this.jbptNode2promNode.put(jbptP, promP);
		}
		
		//Arcs
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> promE: this.prom.getEdges()){
			
			Node source = this.promNode2jbptNode.get(promE.getSource());
			Node target = this.promNode2jbptNode.get(promE.getTarget());
			Flow flow = this.jbpt.addFlow(source, target);
			this.jbptArc2promArc.put(flow, this.prom.getArc(promE.getSource(), promE.getTarget()));
		}
		
	}
	

	public Petrinet getProm() {
		return prom;
	}

	public PetriNet getJbpt() {
		return jbpt;
	}
	
	public PetrinetNode jbpt2PromNode(Node jbptNode){
		return this.jbptNode2promNode.get(jbptNode);
	}
	
	public Arc jbpt2PromArc(Flow jbptArc){
		return this.jbptArc2promArc.get(jbptArc);
	}


	
	
}
