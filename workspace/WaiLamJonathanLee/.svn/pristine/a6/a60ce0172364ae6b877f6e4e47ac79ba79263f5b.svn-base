package org.processmining.experiments.metrics.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.framework.plugin.PluginContext;

public class AcceptingPetriNetNofPlacesMetric implements AcceptingPetriNetMetric {

	public static final String NAME = "Number of places";
	
	public double compute(PluginContext context, AcceptingPetriNet apn) {
		return apn.getNet().getPlaces().size();
	}

	public String getName() {
		return NAME;
	}

}
