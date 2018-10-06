package org.processmining.logalignment.plugins;

import java.util.Collection;

import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.algorithms.CreateReplayCostFactorFromActivityClusterArrayAlgorithm;
import org.processmining.logalignment.connections.CreateReplayCostFactorFromActivityClusterArrayConnection;
import org.processmining.logalignment.help.CreateReplayCostFactorFromActivityClusterArrayHelp;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.parameters.CreateReplayCostFactorFromActivityClusterArrayParameters;

@Plugin(name = "Determine Activity Costs", parameterLabels = { "Activity Cluster Array", "Parameters" }, returnLabels = { "Replay Costs" }, returnTypes = { ReplayCostFactor.class }, help = CreateReplayCostFactorFromActivityClusterArrayHelp.TEXT)
public class CreateReplayCostFactorFromActivityClusterArrayPlugin extends CreateReplayCostFactorFromActivityClusterArrayAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Determine Activity Costs, Default", requiredParameterLabels = { 0 })
	public ReplayCostFactor runDefault(PluginContext context, ActivityClusterArray clusters) {
		CreateReplayCostFactorFromActivityClusterArrayParameters parameters = new CreateReplayCostFactorFromActivityClusterArrayParameters();
		return runConnection(context, clusters, parameters);
	}

	@Deprecated
	public ReplayCostFactor createDefault(PluginContext context, ActivityClusterArray clusters) {
		return runDefault(context, clusters);
	}

	@PluginVariant(variantLabel = "Determine Activity Costs, Parameters", requiredParameterLabels = { 0, 1 })
	public ReplayCostFactor run(PluginContext context, ActivityClusterArray clusters, CreateReplayCostFactorFromActivityClusterArrayParameters parameters) {
		return runConnection(context, clusters, parameters);
	}

	@Deprecated
	public ReplayCostFactor createParameters(PluginContext context, ActivityClusterArray clusters, CreateReplayCostFactorFromActivityClusterArrayParameters parameters) {
		return run(context, clusters, parameters);
	}

	private ReplayCostFactor runConnection(PluginContext context, ActivityClusterArray clusters,
			CreateReplayCostFactorFromActivityClusterArrayParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<CreateReplayCostFactorFromActivityClusterArrayConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						CreateReplayCostFactorFromActivityClusterArrayConnection.class, context, clusters);
				for (CreateReplayCostFactorFromActivityClusterArrayConnection connection : connections) {
					if (connection.getObjectWithRole(CreateReplayCostFactorFromActivityClusterArrayConnection.CLUSTERS)
							.equals(clusters) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(CreateReplayCostFactorFromActivityClusterArrayConnection.FACTOR);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		ReplayCostFactor factor = apply(context, clusters, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new CreateReplayCostFactorFromActivityClusterArrayConnection(clusters, factor, parameters));
		}
		return factor;
	}
}
