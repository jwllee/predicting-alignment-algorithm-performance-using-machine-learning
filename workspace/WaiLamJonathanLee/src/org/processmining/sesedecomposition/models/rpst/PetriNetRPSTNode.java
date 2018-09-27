package org.processmining.sesedecomposition.models.rpst;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * 
 * Class representing a RPST node for a RPST generated from a Petri Net..
 * 
 * See: Artem Polyvyanyy, Jussi Vanhatalo, Hagen Volzer: Simplified Computation 
 * and Generalization of the Refined Process Structure Tree. WS-FM 2010: 25-41
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
public class PetriNetRPSTNode {
	
	/** Unique Identifier */
	private String id;
	/** Name of the RPST node */
    private String name;
    /** Description of the RPST node */
    private String desc;

	
	/** Set of Transitions */
	private Set<Transition> trans;
	/** Set of Places */
	private Set<Place> places;
	/** Set of Arcs */
	private Set<Arc> arcs;
	
	/** Entry node */
	private PetrinetNode entry;
	/** Exit node */
	private PetrinetNode exit;
	
	public PetriNetRPSTNode(String id, String name, String desc,
			Set<Transition> trans, Set<Place> places, Set<Arc> arcs,
			PetrinetNode entry, PetrinetNode exit){
		
		this.id = (id == null)? "" : id;
		this.name = (name == null)? "" : name;
		this.desc = (desc == null)? "" : desc;
		
		this.trans = (trans == null)?   new HashSet<Transition>(): trans;
		this.places = (places == null)? new HashSet<Place>(): places;
		this.arcs = (arcs == null)? new HashSet<Arc>() : arcs;
		
		this.entry = (entry == null)? null : entry;
		this.exit = (exit == null)? null: exit;	
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public Set<Transition> getTrans() {
		return trans;
	}

	public Set<Place> getPlaces() {
		return places;
	}

	public Set<Arc> getArcs() {
		return arcs;
	}

	public PetrinetNode getEntry() {
		return entry;
	}

	public PetrinetNode getExit() {
		return exit;
	}
	
	public String toString(){
		return this.name;
	}
	
}
