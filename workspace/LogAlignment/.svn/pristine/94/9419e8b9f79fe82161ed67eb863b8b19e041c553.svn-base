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
import org.processmining.logalignment.algorithms.ConvertEventLogArrayToActivityClusterArrayAlgorithm;
import org.processmining.logalignment.algorithms.ConvertReplayResultToLogAlignmentAlgorithm;
import org.processmining.logalignment.connections.ConvertReplayResultToLogAlignmentConnection;
import org.processmining.logalignment.dialogs.ConvertReplayResultToLogAlignmentDialog;
import org.processmining.logalignment.help.ConvertReplayResultToLogAlignmentHelp;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;

@Plugin(name = "Convert to Log Alignments", parameterLabels = { "Event Log Array", "Accepting Petri net Array",
		"Replay Result Array", "Replay Cost Factor", "Parameters" }, returnLabels = { "Log Alignment Array" }, returnTypes = { LogAlignmentArray.class }, help = ConvertReplayResultToLogAlignmentHelp.TEXT)
public class ConvertReplayResultToLogAlignmentPlugin extends ConvertReplayResultToLogAlignmentAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert Replay Results, UI", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignmentArray runUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults) {
		return convertDialog(context, logs, nets, replayResults, null);
	}

	@Deprecated
	public LogAlignmentArray convertDialog(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults) {
		return runUI(context, logs, nets, replayResults);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert Replay Results, Costs + UI", requiredParameterLabels = { 0, 1, 2, 3 })
	public LogAlignmentArray runUI(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ReplayCostFactor factor) {
		ConvertReplayResultToLogAlignmentParameters parameters = new ConvertReplayResultToLogAlignmentParameters(logs,
				nets);
		ConvertReplayResultToLogAlignmentDialog dialog = new ConvertReplayResultToLogAlignmentDialog();
		int n = 0;
		String[] title = { "Configure conversion (classifier)", "Configure conversion (transition-activity map)" };
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
		return runConnection(context, logs, nets, replayResults, (new ConvertEventLogArrayToActivityClusterArrayAlgorithm()).apply(logs, parameters), factor,
				parameters);
	}

	@Deprecated
	public LogAlignmentArray convertDialog(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ReplayCostFactor factor) {
		return runUI(context, logs, nets, replayResults, factor);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert Replay Results, Default", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignmentArray runDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults) {
		return convertDefault(context, logs, nets, replayResults, null);
	}

	@Deprecated
	public LogAlignmentArray convertDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults) {
		return runDefault(context, logs, nets, replayResults);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert Replay Results, Costs + Default", requiredParameterLabels = { 0, 1, 2, 3 })
	public LogAlignmentArray runDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ReplayCostFactor factor) {
		ConvertReplayResultToLogAlignmentParameters parameters = new ConvertReplayResultToLogAlignmentParameters(logs,
				nets);
		return runConnection(context, logs, nets, replayResults, (new ConvertEventLogArrayToActivityClusterArrayAlgorithm()).apply(logs, parameters), factor,
				parameters);
	}

	@Deprecated
	public LogAlignmentArray convertDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ReplayCostFactor factor) {
		return runDefault(context, logs, nets, replayResults, factor);
	}

	@PluginVariant(variantLabel = "Convert Replay Results, Parameters", requiredParameterLabels = { 0, 1, 2, 3 })
	public LogAlignmentArray run(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ConvertReplayResultToLogAlignmentParameters parameters) {
		return runConnection(context, logs, nets, replayResults, (new ConvertEventLogArrayToActivityClusterArrayAlgorithm()).apply(logs, parameters), null, parameters);
	}

	@Deprecated
	public LogAlignmentArray convertParameters(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ConvertReplayResultToLogAlignmentParameters parameters) {
		return run(context, logs, nets, replayResults, null);
	}

	@PluginVariant(variantLabel = "Convert Replay Results, Costs + Parameters", requiredParameterLabels = { 0, 1, 2, 3,
			4 })
	public LogAlignmentArray run(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ReplayCostFactor factor,
			ConvertReplayResultToLogAlignmentParameters parameters) {
		return runConnection(context, logs, nets, replayResults, (new ConvertEventLogArrayToActivityClusterArrayAlgorithm()).apply(logs, parameters), factor,
				parameters);
	}

	@Deprecated
	public LogAlignmentArray convertParameters(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, ReplayCostFactor factor,
			ConvertReplayResultToLogAlignmentParameters parameters) {
		return run(context, logs, nets, replayResults, factor, parameters);
	}

	private LogAlignmentArray runConnection(PluginContext context, EventLogArray logs,
			AcceptingPetriNetArray nets, ReplayResultArray replayResults, ActivityClusterArray clusters,
			ReplayCostFactor factor, ConvertReplayResultToLogAlignmentParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<ConvertReplayResultToLogAlignmentConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						ConvertReplayResultToLogAlignmentConnection.class, context, replayResults);
				for (ConvertReplayResultToLogAlignmentConnection connection : connections) {
					if (connection.getObjectWithRole(ConvertReplayResultToLogAlignmentConnection.RESULTS).equals(
							replayResults)
							&& connection.getParameters().equals(parameters)) {
						return connection.getObjectWithRole(ConvertReplayResultToLogAlignmentConnection.ALIGNMENTS);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		LogAlignmentArray alignments = apply(logs, nets, replayResults, clusters, factor, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new ConvertReplayResultToLogAlignmentConnection(logs, nets, replayResults, alignments, parameters));
		}
		return alignments;
	}

}
