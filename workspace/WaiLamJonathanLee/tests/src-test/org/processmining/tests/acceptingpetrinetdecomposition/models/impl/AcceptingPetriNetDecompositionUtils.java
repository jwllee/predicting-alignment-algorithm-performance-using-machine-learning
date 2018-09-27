package org.processmining.tests.acceptingpetrinetdecomposition.models.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetArrayFactory;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinetdecomposition.models.AcceptingPetriNetDecomposition;
import org.processmining.acceptingpetrinetdecomposition.models.AcceptingPetriNetDecompositionFactory;
import org.processmining.acceptingpetrinetdecomposition.models.impl.AcceptingPetriNetDecompositionFactoryImpl;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class AcceptingPetriNetDecompositionUtils {

	AcceptingPetriNetDecomposition makeSmallValidDecompositionWithThreeSubnets() {
		
		String netName = "net0";
		Petrinet net = PetrinetFactory.newPetrinet(netName);
		AcceptingPetriNet apn = AcceptingPetriNetFactory.createAcceptingPetriNet(net);
		
		Place p0 = net.addPlace("p0");
		Place p1 = net.addPlace("p1");
		Place p2 = net.addPlace("p2");
		
		Transition t0 = net.addTransition("t0");
		Transition t1 = net.addTransition("t1");

		Arc a0 = net.addArc(p0, t0);
		Arc a1 = net.addArc(t0, p1);
		Arc a2 = net.addArc(p1, t1);
		Arc a3 = net.addArc(t1, p2);
		
		Marking initialMarking = new Marking();
		initialMarking.add(p0);
		
		Marking end = new Marking();
		end.add(p2);
		Set<Marking> finalMarkings = new HashSet<>();
		finalMarkings.add(end);
		
		apn.setInitialMarking(initialMarking);
		apn.setFinalMarkings(finalMarkings);
		
		// make the 3 subnets of the maximal decomposition of overall net
		// subnet 0
		String subnetName0 = "subnet0";
		Petrinet subnet0 = PetrinetFactory.newPetrinet(subnetName0);
		AcceptingPetriNet subApn0 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet0);
		
		Place sub0P0 = subnet0.addPlace("p0");
		Transition sub0T0 = subnet0.addTransition("t0");
		Arc sub0A0 = subnet0.addArc(sub0P0, sub0T0);
		
		Marking subInitialMarking = new Marking();
		subInitialMarking.add(sub0P0);
		
		subApn0.setInitialMarking(subInitialMarking);
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap0 = new HashMap<>();
		subnetMap0.put(p0, sub0P0);
		subnetMap0.put(t0, sub0T0);
		subnetMap0.put(a0, sub0A0);
		
		// subnet 1
		String subnetName1 = "subnet1";
		Petrinet subnet1 = PetrinetFactory.newPetrinet(subnetName1);
		AcceptingPetriNet subApn1 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet1);
		
		Place sub1P1 = subnet1.addPlace("p1");
		Transition sub1T0 = subnet1.addTransition("t0");
		Transition sub1T1 = subnet1.addTransition("t1");
		Arc sub1A1 = subnet1.addArc(sub1T0, sub1P1);
		Arc sub1A2 = subnet1.addArc(sub1P1, sub1T1);
		
		// no need to set initial and final markings for subnet 1 because it does not have any
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap1 = new HashMap<>();
		subnetMap1.put(p1, sub1P1);
		subnetMap1.put(t0, sub1T0);
		subnetMap1.put(t1, sub1T1);
		subnetMap1.put(a1, sub1A1);
		subnetMap1.put(a2, sub1A2);
		
		// subnet 2
		String subnetName2 = "subnet2";
		Petrinet subnet2 = PetrinetFactory.newPetrinet(subnetName2);
		AcceptingPetriNet subApn2 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet2);
		
		Place sub2P2 = subnet2.addPlace("p2");
		Transition sub2T1 = subnet2.addTransition("t1");
		Arc sub2A3 = subnet2.addArc(sub2T1, sub2P2);
		
		Marking subEnd = new Marking();
		subEnd.add(sub2P2);
		Set<Marking> subFinalMarkings = new HashSet<>();
		subFinalMarkings.add(subEnd);
		
		subApn2.setFinalMarkings(subFinalMarkings);
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap2 = new HashMap<>();
		subnetMap2.put(t1, sub2T1);
		subnetMap2.put(p2, sub2P2);
		subnetMap2.put(a3, sub2A3);
		
		// creating accepting petri net array and parentChildMaps
		List<AcceptingPetriNet> subnetList = new ArrayList<>();
		subnetList.add(subApn0);
		subnetList.add(subApn1);
		subnetList.add(subApn2);
		AcceptingPetriNetArray subnetArray = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray(subnetList);
		
		List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps = new ArrayList<>();
		parentChildMaps.add(subnetMap0);
		parentChildMaps.add(subnetMap1);
		parentChildMaps.add(subnetMap2);
		
		// based on the tests that the factory works
		AcceptingPetriNetDecompositionFactory factory = new AcceptingPetriNetDecompositionFactoryImpl();
		return factory.makeDecomposition(apn, subnetArray, parentChildMaps);
	}
	
	AcceptingPetriNetDecomposition makeSmallValidDecompositionWithInternalTransitions() {
		
		String netName = "net0";
		Petrinet net = PetrinetFactory.newPetrinet(netName);
		AcceptingPetriNet apn = AcceptingPetriNetFactory.createAcceptingPetriNet(net);
		
		Place p0 = net.addPlace("p0");
		Place p1 = net.addPlace("p1");
		Place p2 = net.addPlace("p2");
		
		Transition t0 = net.addTransition("t0");
		Transition t1 = net.addTransition("t1");

		Arc a0 = net.addArc(p0, t0);
		Arc a1 = net.addArc(t0, p1);
		Arc a2 = net.addArc(p1, t1);
		Arc a3 = net.addArc(t1, p2);
		
		Marking initialMarking = new Marking();
		initialMarking.add(p0);
		
		Marking end = new Marking();
		end.add(p2);
		Set<Marking> finalMarkings = new HashSet<>();
		finalMarkings.add(end);
		
		apn.setInitialMarking(initialMarking);
		apn.setFinalMarkings(finalMarkings);
		
		// make the 2 subnets of a valid decomposition of overall net
		// subnet 0
		String subnetName0 = "subnet0";
		Petrinet subnet0 = PetrinetFactory.newPetrinet(subnetName0);
		AcceptingPetriNet subApn0 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet0);
		
		Place sub0P0 = subnet0.addPlace("p0");
		Transition sub0T0 = subnet0.addTransition("t0");
		Arc sub0A0 = subnet0.addArc(sub0P0, sub0T0);
		
		Marking subInitialMarking = new Marking();
		subInitialMarking.add(sub0P0);
		
		subApn0.setInitialMarking(subInitialMarking);
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap0 = new HashMap<>();
		subnetMap0.put(p0, sub0P0);
		subnetMap0.put(t0, sub0T0);
		subnetMap0.put(a0, sub0A0);
		
		// subnet 1
		String subnetName1 = "subnet1";
		Petrinet subnet1 = PetrinetFactory.newPetrinet(subnetName1);
		AcceptingPetriNet subApn1 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet1);
		
		Place sub1P1 = subnet1.addPlace("p1");
		Place sub1P2 = subnet1.addPlace("p2");
		Transition sub1T0 = subnet1.addTransition("t0");
		Transition sub1T1 = subnet1.addTransition("t1");
		Arc sub1A1 = subnet1.addArc(sub1T0, sub1P1);
		Arc sub1A2 = subnet1.addArc(sub1P1, sub1T1);
		Arc sub1A3 = subnet1.addArc(sub1T1, sub1P2);
		
		Marking subEnd = new Marking();
		subEnd.add(sub1P2);
		Set<Marking> subFinalMarkings = new HashSet<>();
		subFinalMarkings.add(subEnd);

		Map<DirectedGraphElement, DirectedGraphElement> subnetMap1 = new HashMap<>();
		subnetMap1.put(p1, sub1P1);
		subnetMap1.put(p2, sub1P2);
		subnetMap1.put(t0, sub1T0);
		subnetMap1.put(t1, sub1T1);
		subnetMap1.put(a1, sub1A1);
		subnetMap1.put(a2, sub1A2);
		subnetMap1.put(a3, sub1A3);
		
		// creating accepting petri net array and parentChildMaps
		List<AcceptingPetriNet> subnetList = new ArrayList<>();
		subnetList.add(subApn0);
		subnetList.add(subApn1);
		AcceptingPetriNetArray subnetArray = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray(subnetList);
		
		List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps = new ArrayList<>();
		parentChildMaps.add(subnetMap0);
		parentChildMaps.add(subnetMap1);
		
		// based on the tests that the factory works
		AcceptingPetriNetDecompositionFactory factory = new AcceptingPetriNetDecompositionFactoryImpl();
		return factory.makeDecomposition(apn, subnetArray, parentChildMaps);
	}
	
	AcceptingPetriNetDecomposition makeInvalidDecompositionWithDuplicatedBorderTransitions() {
		String netName = "net0";
		Petrinet net = PetrinetFactory.newPetrinet(netName);
		AcceptingPetriNet apn = AcceptingPetriNetFactory.createAcceptingPetriNet(net);
		
		Place p0 = net.addPlace("p0");
		Place p1 = net.addPlace("p1");
		Place p2 = net.addPlace("p2");
		
		Transition t0 = net.addTransition("t0");
		Transition t0Dup = net.addTransition("t0");

		Arc a0 = net.addArc(p0, t0);
		Arc a1 = net.addArc(t0, p1);
		Arc a2 = net.addArc(p1, t0Dup);
		Arc a3 = net.addArc(t0Dup, p2);
		
		Marking initialMarking = new Marking();
		initialMarking.add(p0);
		
		Marking end = new Marking();
		end.add(p2);
		Set<Marking> finalMarkings = new HashSet<>();
		finalMarkings.add(end);
		
		apn.setInitialMarking(initialMarking);
		apn.setFinalMarkings(finalMarkings);
		
		// make the 3 subnets of the maximal decomposition of overall net
		// subnet 0
		String subnetName0 = "subnet0";
		Petrinet subnet0 = PetrinetFactory.newPetrinet(subnetName0);
		AcceptingPetriNet subApn0 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet0);
		
		Place sub0P0 = subnet0.addPlace("p0");
		Transition sub0T0 = subnet0.addTransition("t0");
		Arc sub0A0 = subnet0.addArc(sub0P0, sub0T0);
		
		Marking subInitialMarking = new Marking();
		subInitialMarking.add(sub0P0);
		
		subApn0.setInitialMarking(subInitialMarking);
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap0 = new HashMap<>();
		subnetMap0.put(p0, sub0P0);
		subnetMap0.put(t0, sub0T0);
		subnetMap0.put(a0, sub0A0);
		
		// subnet 1
		String subnetName1 = "subnet1";
		Petrinet subnet1 = PetrinetFactory.newPetrinet(subnetName1);
		AcceptingPetriNet subApn1 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet1);
		
		Place sub1P1 = subnet1.addPlace("p1");
		Transition sub1T0 = subnet1.addTransition("t0");
		Transition sub1T0Dup = subnet1.addTransition("t0");
		Arc sub1A1 = subnet1.addArc(sub1T0, sub1P1);
		Arc sub1A2 = subnet1.addArc(sub1P1, sub1T0Dup);
		
		// no need to set initial and final markings for subnet 1 because it does not have any
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap1 = new HashMap<>();
		subnetMap1.put(p1, sub1P1);
		subnetMap1.put(t0, sub1T0);
		subnetMap1.put(t0Dup, sub1T0Dup);
		subnetMap1.put(a1, sub1A1);
		subnetMap1.put(a2, sub1A2);
		
		// subnet 2
		String subnetName2 = "subnet2";
		Petrinet subnet2 = PetrinetFactory.newPetrinet(subnetName2);
		AcceptingPetriNet subApn2 = AcceptingPetriNetFactory.createAcceptingPetriNet(subnet2);
		
		Place sub2P2 = subnet2.addPlace("p2");
		Transition sub2T0Dup = subnet2.addTransition("t0");
		Arc sub2A3 = subnet2.addArc(sub2T0Dup, sub2P2);
		
		Marking subEnd = new Marking();
		subEnd.add(sub2P2);
		Set<Marking> subFinalMarkings = new HashSet<>();
		subFinalMarkings.add(subEnd);
		
		subApn2.setFinalMarkings(subFinalMarkings);
		
		Map<DirectedGraphElement, DirectedGraphElement> subnetMap2 = new HashMap<>();
		subnetMap2.put(t0Dup, sub2T0Dup);
		subnetMap2.put(p2, sub2P2);
		subnetMap2.put(a3, sub2A3);
		
		// creating accepting petri net array and parentChildMaps
		List<AcceptingPetriNet> subnetList = new ArrayList<>();
		subnetList.add(subApn0);
		subnetList.add(subApn1);
		subnetList.add(subApn2);
		AcceptingPetriNetArray subnetArray = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray(subnetList);
		
		List<Map<DirectedGraphElement, DirectedGraphElement>> parentChildMaps = new ArrayList<>();
		parentChildMaps.add(subnetMap0);
		parentChildMaps.add(subnetMap1);
		parentChildMaps.add(subnetMap2);
		
		// since there are no transitions with duplicated activity labels, the parentChildMaps
		// computed by the factory should correspond to the correct one
		AcceptingPetriNetDecompositionFactory factory = new AcceptingPetriNetDecompositionFactoryImpl();
		AcceptingPetriNetDecomposition decomposition = factory.makeDecomposition(apn, subnetArray, parentChildMaps);
		
		return decomposition;
	}
}
