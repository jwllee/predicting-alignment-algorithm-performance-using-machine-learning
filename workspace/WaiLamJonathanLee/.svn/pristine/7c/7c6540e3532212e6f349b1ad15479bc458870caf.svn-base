package org.processmining.experiments.metrics.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class AcceptingPetriNetNofInvisibleTransitionsMetric implements AcceptingPetriNetMetric {

	public static final String NAME = "Number of invisible transitions";
	
	public double compute(PluginContext context, AcceptingPetriNet apn) {
		int cnt = 0;
		for (Transition tran: apn.getNet().getTransitions()) {
			if (tran.isInvisible())
				cnt += 1;
		}
		return cnt;
	}

	public String getName() {
		return NAME;
	}

}
