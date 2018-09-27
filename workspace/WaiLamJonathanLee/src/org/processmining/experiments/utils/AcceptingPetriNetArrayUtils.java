package org.processmining.experiments.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class AcceptingPetriNetArrayUtils {

	public List<XEventClass> getSharedActivities(AcceptingPetriNetArray netArray) {
		
		List<XEventClass> seenActivities = new ArrayList<>();
		Set<XEventClass> sharedActivities = new HashSet<>();
		
		for (int ind = 0; ind < netArray.getSize(); ind++) {
			AcceptingPetriNet apn = netArray.getNet(ind);
			Petrinet net = apn.getNet();
			
			for (Transition trans: net.getTransitions()) {
				
				XEventClass activity = new XEventClass(trans.getLabel(), 0);
				if (seenActivities.contains(activity)) 
					sharedActivities.add(activity);
				else
					seenActivities.add(activity);
				
			}
			
		}
		
		List<XEventClass> sharedActsList = new ArrayList<>();
		sharedActsList.addAll(sharedActivities);
		
		return sharedActsList;
		
	}
	
}
