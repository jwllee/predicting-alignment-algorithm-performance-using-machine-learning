package org.processmining.logalignment.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.algorithms.MergeLogAlignmentArrayIntoLogAlignmentAlgorithm;
import org.processmining.logalignment.connections.MergeLogAlignmentArrayIntoLogAlignmentConnection;
import org.processmining.logalignment.dialogs.MergeLogAlignmentArrayIntoLogAlignmentDialog;
import org.processmining.logalignment.help.MergeLogAlignmentArrayIntoLogAlignmentHelp;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;

@Plugin(name = "Merge Log Alignments", parameterLabels = { "Event Log", "Log Alignment Array",
		"Accepting Petri Net Array", "Parameters" }, returnLabels = { "Log Alignment" }, returnTypes = { LogAlignment.class }, help = MergeLogAlignmentArrayIntoLogAlignmentHelp.TEXT)
public class MergeLogAlignmentArrayIntoLogAlignmentPlugin extends MergeLogAlignmentArrayIntoLogAlignmentAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Merge Log Alignments, UI", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignment runUI(UIPluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets) {
		MergeLogAlignmentArrayIntoLogAlignmentParameters parameters = new MergeLogAlignmentArrayIntoLogAlignmentParameters(
				log, alignments);
		MergeLogAlignmentArrayIntoLogAlignmentDialog dialog = new MergeLogAlignmentArrayIntoLogAlignmentDialog();
		int n = 0;
		String[] title = { "Configure merge (classifier)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 0, dialog.getPanel(log, parameters, n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
				dialog.finish();
			} else {
				return null;
			}
		}
		return runConnection(context, log, alignments, nets, parameters);
	}

	@Deprecated
	public LogAlignment convertDialog(UIPluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets) {
		return runUI(context, log, alignments, nets);
	}

	@PluginVariant(variantLabel = "Merge Log Alignments, Default", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignment runDefault(PluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets) {
		MergeLogAlignmentArrayIntoLogAlignmentParameters parameters = new MergeLogAlignmentArrayIntoLogAlignmentParameters(
				log, alignments);
		return runConnection(context, log, alignments, nets, parameters);
	}

	@Deprecated
	public LogAlignment convertParameters(PluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets) {
		return runDefault(context, log, alignments, nets);
	}

	@PluginVariant(variantLabel = "Merge Log Alignments, Parameters", requiredParameterLabels = { 0, 1, 2, 3 })
	public LogAlignment run(PluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		return runConnection(context, log, alignments, nets, parameters);
	}

	@Deprecated
	public LogAlignment convertParameters(PluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		return run(context, log, alignments, nets, parameters);
	}

	private LogAlignment runConnection(PluginContext context, XLog log, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<MergeLogAlignmentArrayIntoLogAlignmentConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						MergeLogAlignmentArrayIntoLogAlignmentConnection.class, context, log, alignments);
				for (MergeLogAlignmentArrayIntoLogAlignmentConnection connection : connections) {
					if (connection.getObjectWithRole(MergeLogAlignmentArrayIntoLogAlignmentConnection.LOG).equals(log)
							&& connection
									.getObjectWithRole(MergeLogAlignmentArrayIntoLogAlignmentConnection.ALIGNMENTS)
									.equals(alignments) && connection.getParameters().equals(parameters)) {
						return connection.getObjectWithRole(MergeLogAlignmentArrayIntoLogAlignmentConnection.ALIGNMENT);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		LogAlignment alignment = apply(log, alignments, nets, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new MergeLogAlignmentArrayIntoLogAlignmentConnection(log, alignments, alignment, parameters));
		}
		return alignment;
	}

}
