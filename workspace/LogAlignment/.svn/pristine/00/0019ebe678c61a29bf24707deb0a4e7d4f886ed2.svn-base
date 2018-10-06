package org.processmining.logalignment.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.algorithms.ReplayEventLogArrayOnAcceptingPetriNetArrayAlgorithm;
import org.processmining.logalignment.connections.ReplayEventLogArrayOnAcceptingPetriNetArrayConnection;
import org.processmining.logalignment.dialogs.ReplayEventLogArrayOnAcceptingPetriNetArrayDialog;
import org.processmining.logalignment.help.ReplayEventLogArrayOnAcceptingPetriNetArrayHelp;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;

@Plugin(name = "Replay Event Logs", parameterLabels = { "Event Log Array", "Accepting Petri net Array", "Activity Cluster Array", "Replay Cost",
		"Parameters" }, returnLabels = { "Replay Result Array" }, returnTypes = { ReplayResultArray.class }, help = ReplayEventLogArrayOnAcceptingPetriNetArrayHelp.TEXT)
public class ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin extends
		ReplayEventLogArrayOnAcceptingPetriNetArrayAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Replay Event Logs, UI", requiredParameterLabels = { 0, 1 })
	public ReplayResultArray runUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets) {
		return replayUI(context, logs, nets, null);
	}

	@Deprecated
	public ReplayResultArray replayUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets) {
		return runUI(context, logs, nets);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Replay Event Logs, Cost + UI", requiredParameterLabels = { 0, 1, 3 })
	public ReplayResultArray runUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor cost) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(
				logs, nets);
		ReplayEventLogArrayOnAcceptingPetriNetArrayDialog dialog = new ReplayEventLogArrayOnAcceptingPetriNetArrayDialog();
		int n = 0;
		String[] title = { "Configure replay (classifier)", "Configure replay (transition-activity map)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 1, dialog.getPanel(logs, nets, parameters, n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
			} else {
				return null;
			}
		}
		return runConnection(context, logs, nets, null, cost, parameters);
	}

	@Deprecated
	public ReplayResultArray replayUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor cost) {
		return runUI(context, logs, nets, cost);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Replay Event Logs, Default", requiredParameterLabels = { 0, 1 })
	public ReplayResultArray runDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets) {
		return replayDefault(context, logs, nets, null);
	}

	@Deprecated
	public ReplayResultArray replayDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets) {
		return runDefault(context, logs, nets);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Replay Event Logs, Cost + Default", requiredParameterLabels = { 0, 1, 3 })
	public ReplayResultArray runDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor cost) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(
				logs, nets);
		return runConnection(context, logs, nets, null, cost, parameters);
	}

	@Deprecated
	public ReplayResultArray replayDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor cost) {
		return runDefault(context, logs, nets, cost);
	}

	@PluginVariant(variantLabel = "Replay Event Logs, Parameters", requiredParameterLabels = { 0, 1, 4 })
	public ReplayResultArray run(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return replayParameters(context, logs, nets, null, parameters);
	}

	@Deprecated
	public ReplayResultArray replayParameters(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return run(context, logs, nets, null, parameters);
	}

	@PluginVariant(variantLabel = "Replay Event Logs, Cost + Parameters", requiredParameterLabels = { 0, 1, 3, 4 })
	public ReplayResultArray run(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor cost, ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return runConnection(context, logs, nets, null, cost, parameters);
	}

	@PluginVariant(variantLabel = "Replay Event Logs, Clusters + Cost + Parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
	public ReplayResultArray run(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ActivityClusterArray clusters, ReplayCostFactor cost, ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return runConnection(context, logs, nets, clusters, cost, parameters);
	}

	@Deprecated
	public ReplayResultArray replayParameters(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor cost, ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return run(context, logs, nets, cost, parameters);
	}

	private ReplayResultArray runConnection(PluginContext context, EventLogArray logs,
			AcceptingPetriNetArray nets, ActivityClusterArray clusters, ReplayCostFactor cost,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<ReplayEventLogArrayOnAcceptingPetriNetArrayConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.class, context, logs, nets);
				for (ReplayEventLogArrayOnAcceptingPetriNetArrayConnection connection : connections) {
					if (connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.LOGS)
							.equals(logs)
							&& connection.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.NETS)
									.equals(nets) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(ReplayEventLogArrayOnAcceptingPetriNetArrayConnection.REPLAYS);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		ReplayResultArray replays = apply(context, logs, nets, clusters, cost, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new ReplayEventLogArrayOnAcceptingPetriNetArrayConnection(nets, logs, replays, parameters));
		}
		return replays;
	}

}
