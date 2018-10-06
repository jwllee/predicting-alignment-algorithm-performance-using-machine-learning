package org.processmining.logalignment.connections;

import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.parameters.CreateReplayCostFactorFromActivityClusterArrayParameters;

public class CreateReplayCostFactorFromActivityClusterArrayConnection extends AbstractConnection {

	public final static String CLUSTERS = "Clusters";
	public final static String FACTOR = "Factor";

	private CreateReplayCostFactorFromActivityClusterArrayParameters parameters;

	public CreateReplayCostFactorFromActivityClusterArrayConnection(ActivityClusterArray clusters, ReplayCostFactor factor, CreateReplayCostFactorFromActivityClusterArrayParameters parameters) {
		super("Convert Replay Result to Log Alignment Connection");
		put(CLUSTERS, clusters);
		put(FACTOR, factor);
		this.parameters = new CreateReplayCostFactorFromActivityClusterArrayParameters(parameters);
	}

	public CreateReplayCostFactorFromActivityClusterArrayParameters getParameters() {
		return parameters;
	}
}
