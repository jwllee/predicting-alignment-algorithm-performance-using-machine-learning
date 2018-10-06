package org.processmining.logalignment.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.algorithms.ConvertLogAlignmentToReplayResultAlgorithm;
import org.processmining.logalignment.connections.ConvertLogAlignmentToReplayResultConnection;
import org.processmining.logalignment.dialogs.ConvertLogAlignmentToReplayResultDialog;
import org.processmining.logalignment.help.ConvertLogAlignmentToReplayResultHelp;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.parameters.ConvertLogAlignmentToReplayResultParameters;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

@Plugin(name = "Convert to Replay Results", parameterLabels = { "Log Alignment", "Accepting Petri Net", "Event Log",
		"Parameters" }, returnLabels = { "Replay Results" }, returnTypes = { PNRepResult.class }, help = ConvertLogAlignmentToReplayResultHelp.TEXT)
public class ConvertLogAlignmentToReplayResultPlugin extends ConvertLogAlignmentToReplayResultAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert to Replay Results, UI", requiredParameterLabels = { 0, 1, 2 })
	public PNRepResult runUI(UIPluginContext context, LogAlignment logAlignment, AcceptingPetriNet net, XLog log) {
		ConvertLogAlignmentToReplayResultParameters parameters = new ConvertLogAlignmentToReplayResultParameters(log,
				net);
		ConvertLogAlignmentToReplayResultDialog dialog = new ConvertLogAlignmentToReplayResultDialog();
		int n = 0;
		String[] title = { "Configure conversion (classifier)", "Configure conversion (transition-activity map)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 1, dialog.getPanel(log, net, parameters, n));
			switch (result) {
				case NEXT :
					n++;
					break;
				case PREV :
					n--;
					break;
				case FINISHED :
					break;
				default :
					return null;
			}
		}
		return runConnection(context, logAlignment, net, log, parameters);
	}

	/**
	 * @deprecated Use runUI() instead
	 */
	@Deprecated
	public PNRepResult convertUI(UIPluginContext context, LogAlignment logAlignment, AcceptingPetriNet net, XLog log) {
		return runUI(context, logAlignment, net, log);
	}

	@PluginVariant(variantLabel = "Convert to Replay Results, Default", requiredParameterLabels = { 0, 1, 2 })
	public PNRepResult runDefault(PluginContext context, LogAlignment logAlignment, AcceptingPetriNet net,
			XLog log) {
		return runConnection(context, logAlignment, net, log,
				new ConvertLogAlignmentToReplayResultParameters(log, net));
	}

	/**
	 * @deprecated Use runDefault() instead
	 */
	@Deprecated
	public PNRepResult convertParameters(PluginContext context, LogAlignment logAlignment, AcceptingPetriNet net,
			XLog log) {
		return runDefault(context, logAlignment, net, log);
	}

	@PluginVariant(variantLabel = "Convert to Replay Results, Parameters", requiredParameterLabels = { 0, 1, 2, 3 })
	public PNRepResult run(PluginContext context, LogAlignment logAlignment, AcceptingPetriNet net,
			XLog log, ConvertLogAlignmentToReplayResultParameters parameters) {
		return runConnection(context, logAlignment, net, log, parameters);
	}

	/**
	 * @deprecated Use run() instead
	 */
	@Deprecated
	public PNRepResult convertParameters(PluginContext context, LogAlignment logAlignment, AcceptingPetriNet net,
			XLog log, ConvertLogAlignmentToReplayResultParameters parameters) {
		return run(context, logAlignment, net, log, parameters);
	}

	private PNRepResult runConnection(PluginContext context, LogAlignment logAlignment, AcceptingPetriNet net, XLog log,
			ConvertLogAlignmentToReplayResultParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<ConvertLogAlignmentToReplayResultConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						ConvertLogAlignmentToReplayResultConnection.class, context, logAlignment, net, log);
				for (ConvertLogAlignmentToReplayResultConnection connection : connections) {
					if (connection.getObjectWithRole(ConvertLogAlignmentToReplayResultConnection.ALIGNMENT).equals(logAlignment)
							&& connection.getObjectWithRole(ConvertLogAlignmentToReplayResultConnection.NET).equals(net)
							&& connection.getObjectWithRole(ConvertLogAlignmentToReplayResultConnection.LOG).equals(log)
							&& connection.getParameters().equals(parameters)) {
						return connection.getObjectWithRole(ConvertLogAlignmentToReplayResultConnection.RESULT);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		
		PNRepResult result = apply(context, logAlignment, net, log, parameters);
		
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new ConvertLogAlignmentToReplayResultConnection(logAlignment, net, log, result, parameters));
		}
		return result;
	}
}
