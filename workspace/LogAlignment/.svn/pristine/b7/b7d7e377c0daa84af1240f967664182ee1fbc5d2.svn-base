package org.processmining.logalignment.algorithms;

import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.impl.ReplayCostFactorImpl;
import org.processmining.logalignment.parameters.CreateReplayCostFactorFromActivityClusterArrayParameters;

public class CreateReplayCostFactorFromActivityClusterArrayAlgorithm {

	public ReplayCostFactor apply(PluginContext context, ActivityClusterArray clusters,
			CreateReplayCostFactorFromActivityClusterArrayParameters parameters) {
		ReplayCostFactor cost = new ReplayCostFactorImpl();
		cost.init("Activity Costs", clusters);
		return cost;
	}
}
