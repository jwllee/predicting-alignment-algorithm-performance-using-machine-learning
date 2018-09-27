package org.processmining.sesedecomposition.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sesedecomposition.algorithms.DecomposeBySESEsWithBridgingAlgorithm;
import org.processmining.sesedecomposition.connections.DecomposeBySESEsWithBridgingConnection;
import org.processmining.sesedecomposition.dialogs.DecomposeBySESEsWithBridgingDialog;
import org.processmining.sesedecomposition.help.DecomposeBySESEsWithBridgingHelp;
import org.processmining.sesedecomposition.parameters.DecomposeBySESEsWithBridgingParameters;

@Plugin(name = "Decompose Accepting Petri Net into Accepting Petri Net Array by SESE-based decomposition", 
parameterLabels = { "Accepting Petri Net", "Parameters" }, 
returnLabels = { "Accepting Petri Net Array" }, returnTypes = { AcceptingPetriNetArray.class }, help = DecomposeBySESEsWithBridgingHelp.TEXT)
public class DecomposeBySESEsWithBridgingPlugin extends DecomposeBySESEsWithBridgingAlgorithm {
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Decompose Accepting Petri Net into Accepting Petri Net Array By "
			+ "SESE-based decomposition, UI", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray runDefault(UIPluginContext context, AcceptingPetriNet net) {
		DecomposeBySESEsWithBridgingParameters parameters = new DecomposeBySESEsWithBridgingParameters();
		DecomposeBySESEsWithBridgingDialog dialog = new DecomposeBySESEsWithBridgingDialog();
		int n = 0;
		String[] title = {"Configure the maximum number of arcs per subnet"};
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 0, dialog.getPanel(net, parameters, n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
				
			} else {
				return null;
			}
		}
		// Apply the algorithm depending on whether a connection already exists.
		return runConnections(context, net, parameters);
	}
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Decompose Accepting Petri Net into Accepting Petri Net Array by "
			+ "SESE-based decomposition", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNetArray run(PluginContext context, AcceptingPetriNet net,
			DecomposeBySESEsWithBridgingParameters parameters) {
		// Apply the algorithm depending on whether a connection already exists.
		return runConnections(context, net, parameters);
	}

	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Decompose Accepting Petri Net into Accepting Petri Net Array by "
			+ "SESE-based decomposition, Default", requiredParameterLabels = { 0 })
	public AcceptingPetriNetArray runDefault(PluginContext context, AcceptingPetriNet net) {
		DecomposeBySESEsWithBridgingParameters parameters = new DecomposeBySESEsWithBridgingParameters();
		// Apply the algorithm depending on whether a connection already exists.
		return runConnections(context, net, parameters);
	}
	
	private AcceptingPetriNetArray runConnections(PluginContext context, AcceptingPetriNet net,
			DecomposeBySESEsWithBridgingParameters parameters) {
		if (parameters.isTryConnections()) {
			// Try to found a connection that matches the inputs and the parameters.
			Collection<DecomposeBySESEsWithBridgingConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						DecomposeBySESEsWithBridgingConnection.class, context);
				for (DecomposeBySESEsWithBridgingConnection connection : connections) {
					if (connection.getObjectWithRole(DecomposeBySESEsWithBridgingConnection.NET)
							.equals(net) && connection.getParameters().equals(parameters)) {
						// Found a match. Return the associated output as result of the algorithm.
						return connection
								.getObjectWithRole(DecomposeBySESEsWithBridgingConnection.NETARRAY);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		// No connection found. Apply the algorithm to compute a fresh output result.
		AcceptingPetriNetArray netArray = apply(context, net, parameters);
		if (parameters.isTryConnections()) {
			// Store a connection containing the inputs, output, and parameters.
			context.getConnectionManager().addConnection(
					new DecomposeBySESEsWithBridgingConnection(net, netArray, parameters));
		}
		// Return the accepting petri net array
		return netArray;
	}
	
}
