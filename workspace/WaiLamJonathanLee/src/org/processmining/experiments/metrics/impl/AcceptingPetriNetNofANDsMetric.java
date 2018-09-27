package org.processmining.experiments.metrics.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class AcceptingPetriNetNofANDsMetric implements AcceptingPetriNetMetric {

	public final static String NAME = "Number of ANDs";
	
	public double compute(PluginContext context, AcceptingPetriNet apn) {
		Petrinet net = apn.getNet();
		Marking initialMarking = apn.getInitialMarking();
		
		// assuming that it is a workflow 
		if (initialMarking.size() > 1)
			throw new IllegalArgumentException("Initial marking has more than one place " + initialMarking.toString() + ".");
		// AND splits are where transitions have more than one outgoing arc
		int cnt = 0;
		for (Transition tran: net.getTransitions()) {
			if (net.getOutEdges(tran).size() > 1) {
				cnt += 1;
			}
		}
		return cnt;
	}

	public String getName() {
		return NAME;
	}

}
