package org.processmining.experiments.metrics.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;

public class AcceptingPetriNetNofXORsMetric implements AcceptingPetriNetMetric {

	public final static String NAME = "Number of XORs";
	
	public double compute(PluginContext context, AcceptingPetriNet apn) {
		// find the number of XOR splits where places have more than one outgoing arc
		int cnt = 0;
		Petrinet net = apn.getNet();
		for (Place place: net.getPlaces()) {
			if (net.getOutEdges(place).size() > 1) {
				cnt += 1;
			}
		}
		return cnt;
	}

	public String getName() {
		return NAME;
	}

}
