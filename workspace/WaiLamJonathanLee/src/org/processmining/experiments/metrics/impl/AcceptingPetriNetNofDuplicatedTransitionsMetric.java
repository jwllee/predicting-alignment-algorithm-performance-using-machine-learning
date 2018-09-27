package org.processmining.experiments.metrics.impl;

import java.util.HashSet;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class AcceptingPetriNetNofDuplicatedTransitionsMetric implements AcceptingPetriNetMetric {

	public static final String NAME = "Number of duplicated transitions";
	
	public double compute(PluginContext context, AcceptingPetriNet apn) {
		Set<String> seen = new HashSet<>();
		Set<String> duplicated = new HashSet<>();
		for (Transition tran: apn.getNet().getTransitions()) {
			String label = tran.getLabel();
			if (seen.contains(label)) {
				// is duplicated
				duplicated.add(label);
			} else {
				seen.add(label);
			}
		}
		return duplicated.size();
	}

	public String getName() {
		return NAME;
	}

}
