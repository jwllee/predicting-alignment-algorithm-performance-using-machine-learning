package org.processmining.experiments.metrics;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;

public interface AcceptingPetriNetMetric {

	public double compute(PluginContext context, AcceptingPetriNet apn);
	
	public String getName();
	
	
}
