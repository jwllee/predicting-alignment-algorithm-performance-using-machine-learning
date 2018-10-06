package org.processmining.logalignment.plugins;

import java.util.Collection;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.algorithms.FilterLogAlignmentAlgorithm;
import org.processmining.logalignment.connections.FilterLogAlignmentConnection;
import org.processmining.logalignment.help.FilterLogAlignmentHelp;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.parameters.FilterLogAlignmentParameters;

@Plugin(name = "Filter Log Alignments", parameterLabels = { "Log Alignment Array", "Accepting Petri Net Array" }, returnLabels = { "Log Alignment Array" }, returnTypes = { LogAlignmentArray.class }, help = FilterLogAlignmentHelp.TEXT)
public class FilterLogAlignmentPlugin extends FilterLogAlignmentAlgorithm{

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Filter Log Alignments, Default", requiredParameterLabels = { 0, 1 })
	public LogAlignmentArray runDefault(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets) {
		FilterLogAlignmentParameters parameters = new FilterLogAlignmentParameters();
		return runConnection(context, alignments, nets, parameters);
	}

	@Deprecated
	public LogAlignmentArray filterDefault(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets) {
		return runDefault(context, alignments, nets);
	}

	@PluginVariant(variantLabel = "Filter Log Alignments, Parameters", requiredParameterLabels = { 0, 1 })
	public LogAlignmentArray run(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, FilterLogAlignmentParameters parameters) {
		return runConnection(context, alignments, nets, parameters);
	}

	private LogAlignmentArray runConnection(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, FilterLogAlignmentParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<FilterLogAlignmentConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						FilterLogAlignmentConnection.class, context, alignments, nets);
				for (FilterLogAlignmentConnection connection : connections) {
					if (connection.getObjectWithRole(FilterLogAlignmentConnection.ALIGNMENTS).equals(alignments) 
							&& connection.getObjectWithRole(FilterLogAlignmentConnection.NETS).equals(nets)
							&& connection.getParameters().equals(parameters)) {
						return connection.getObjectWithRole(FilterLogAlignmentConnection.FITLEREDALIGNMENTS);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		LogAlignmentArray filteredAlignments = apply(context, alignments, nets, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new FilterLogAlignmentConnection(alignments, nets, filteredAlignments, parameters));
		}
		return filteredAlignments;
	}
}
