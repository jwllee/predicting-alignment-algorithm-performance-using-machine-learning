package org.processmining.experiments.metrics.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.framework.plugin.PluginContext;

public class AcceptingPetriNetNofArcsMetric implements AcceptingPetriNetMetric {

	public static final String NAME = "Number of arcs";
	
	public double compute(PluginContext context, AcceptingPetriNet apn) {
		return apn.getNet().getEdges().size();
	}

	public String getName() {
		return NAME;
	}

}
