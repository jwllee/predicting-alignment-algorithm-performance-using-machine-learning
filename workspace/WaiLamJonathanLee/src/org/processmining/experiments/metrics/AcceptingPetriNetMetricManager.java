package org.processmining.experiments.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofANDsMetric;
import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofArcsMetric;
import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofDuplicatedTransitionsMetric;
import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofInvisibleTransitionsMetric;
import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofPlacesMetric;
import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofTransitionsMetric;
import org.processmining.experiments.metrics.impl.AcceptingPetriNetNofXORsMetric;

public class AcceptingPetriNetMetricManager {

	private static AcceptingPetriNetMetricManager instance = null;
	private List<AcceptingPetriNetMetric> metrics;
	private AcceptingPetriNetMetric defaultMetric;
	private boolean sorted;
	
	private AcceptingPetriNetMetricManager() {
		metrics = new ArrayList<>();
		defaultMetric = null;
		sorted = false;
		register(new AcceptingPetriNetNofANDsMetric(), false);
		register(new AcceptingPetriNetNofArcsMetric(), false);
		register(new AcceptingPetriNetNofDuplicatedTransitionsMetric(), false);
		register(new AcceptingPetriNetNofInvisibleTransitionsMetric(), false);
		register(new AcceptingPetriNetNofPlacesMetric(), false);
		register(new AcceptingPetriNetNofTransitionsMetric(), false);
		register(new AcceptingPetriNetNofXORsMetric(), false);
	}
	
	public static AcceptingPetriNetMetricManager getInstance() {
		if (instance == null) 
			instance = new AcceptingPetriNetMetricManager();
		return instance;
	}
	
	public void register(AcceptingPetriNetMetric metric, boolean isDefault) {
		metrics.add(metric);
		if (isDefault) {
			defaultMetric = metric;
		}
		sorted = false;
	}
	
	public List<AcceptingPetriNetMetric> getMetrics() {
		if (!sorted) {
			Collections.sort(metrics, new Comparator<AcceptingPetriNetMetric>() {
				
				public int compare(AcceptingPetriNetMetric m1, AcceptingPetriNetMetric m2) {
					return m1.getName().compareTo(m2.getName());
				}
			});
			sorted = true;
		}
		return metrics;
	}
	
	public boolean isDefault(AcceptingPetriNetMetric metric) {
		return metric == defaultMetric;
	}
	
	public AcceptingPetriNetMetric getMetric(String name) {
		if (name != null) {
			for (AcceptingPetriNetMetric metric: metrics) {
				if (name.equals(metric.getName())) {
					return metric;
				}
			}
		}
		return defaultMetric;
	}
	
}
