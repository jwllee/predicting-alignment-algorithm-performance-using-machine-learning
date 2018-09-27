package org.processmining.tests.experiments.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetArrayFactory;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.experiments.utils.AcceptingPetriNetArrayUtils;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

public class TestAcceptingPetriNetArrayUtils {

	@Test
	public void simpleTest() {
		assertEquals(2, 1 + 1);
	}

	@Test
	public void testAcceptingPetriNetArrayWithDisjointNets() {
		
		AcceptingPetriNetArray netArray = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray();
		
		// create net 1
		Petrinet net1 = PetrinetFactory.newPetrinet("net1");
		Place place1a = net1.addPlace("place1a");
		Place place1b = net1.addPlace("place1b");
		Transition trans1a = net1.addTransition("trans1a");
		net1.addArc(place1a, trans1a);
		net1.addArc(trans1a, place1b);
		AcceptingPetriNet apn1 = AcceptingPetriNetFactory.createAcceptingPetriNet(net1);
		
		// create net 2
		Petrinet net2 = PetrinetFactory.newPetrinet("net2");
		Place place2a = net2.addPlace("place2a");
		Place place2b = net2.addPlace("place2b");
		Transition trans2a = net2.addTransition("trans2a");
		net2.addArc(place2a, trans2a);
		net2.addArc(trans2a, place2b);
		AcceptingPetriNet apn2 = AcceptingPetriNetFactory.createAcceptingPetriNet(net2);
		
		netArray.addNet(apn1);
		netArray.addNet(apn2);
		
		AcceptingPetriNetArrayUtils utils = new AcceptingPetriNetArrayUtils();
		List<XEventClass> sharedActs = utils.getSharedActivities(netArray);
		
		assertEquals(true, sharedActs.isEmpty());
	}
	
	@Test
	public void testAcceptingPetriNetArrayWithOneSharedTransition() {
		
		AcceptingPetriNetArray netArray = AcceptingPetriNetArrayFactory.createAcceptingPetriNetArray();
		
		// create net 1
		Petrinet net1 = PetrinetFactory.newPetrinet("net1");
		Place place1a = net1.addPlace("place1a");
		Place place1b = net1.addPlace("place1b");
		Transition trans1a = net1.addTransition("trans1a");
		Transition transb1 = net1.addTransition("transb");
		net1.addArc(place1a, trans1a);
		net1.addArc(trans1a, place1b);
		net1.addArc(place1b, transb1);
		AcceptingPetriNet apn1 = AcceptingPetriNetFactory.createAcceptingPetriNet(net1);
		
		// create net 2
		Petrinet net2 = PetrinetFactory.newPetrinet("net2");
		Place place2a = net2.addPlace("place2a");
		Place place2b = net2.addPlace("place2b");
		Transition trans2a = net2.addTransition("trans2a");
		Transition transb2 = net2.addTransition("transb");
		net2.addArc(place2a, trans2a);
		net2.addArc(trans2a, place2b);
		net2.addArc(place2b, transb2);
		AcceptingPetriNet apn2 = AcceptingPetriNetFactory.createAcceptingPetriNet(net2);
		
		netArray.addNet(apn1);
		netArray.addNet(apn2);
		
		AcceptingPetriNetArrayUtils utils = new AcceptingPetriNetArrayUtils();
		List<XEventClass> sharedTrans = utils.getSharedActivities(netArray);
		
		assertEquals(false, sharedTrans.isEmpty());
		assertEquals(1, sharedTrans.size());
		assertEquals("transb", sharedTrans.get(0).getId());
	}
	
}
