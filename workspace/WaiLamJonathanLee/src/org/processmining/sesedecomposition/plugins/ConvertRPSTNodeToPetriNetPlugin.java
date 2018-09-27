package org.processmining.sesedecomposition.plugins;

import java.util.Collection;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.sesedecomposition.algorithms.ConvertRPSTNodeToPetriNetAlgorithm;
import org.processmining.sesedecomposition.connections.ConvertRPSTNodeToPetriNetConnection;
import org.processmining.sesedecomposition.help.ConvertRPSTNodeToPetriNetHelp;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.parameters.ConvertRPSTNodeToPetriNetParameters;

@Plugin(name = "Convert RPST node to Petri Net", parameterLabels = { "Petri Net RPST Node", "Parameters" }, 
	returnLabels = { "Petri Net" }, returnTypes = { AcceptingPetriNet.class }, help = ConvertRPSTNodeToPetriNetHelp.TEXT)
public class ConvertRPSTNodeToPetriNetPlugin extends ConvertRPSTNodeToPetriNetAlgorithm {
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Convert RPST node to petri net", requiredParameterLabels = { 0, 1 })
	public Petrinet run(PluginContext context, PetriNetRPSTNode node, 
			ConvertRPSTNodeToPetriNetParameters parameters) {
		// Apply the algorithm depending on whether a connection already exists.
		return runConnections(context, node, parameters);
	}

	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Convert RPST node to petri net, Default", requiredParameterLabels = { 0 })
	public Petrinet runDefault(PluginContext context, PetriNetRPSTNode node) {
		ConvertRPSTNodeToPetriNetParameters parameters = new ConvertRPSTNodeToPetriNetParameters();
		// Apply the algorithm depending on whether a connection already exists.
		return run(context, node, parameters);
	}
	
	private Petrinet runConnections(PluginContext context, PetriNetRPSTNode node,
			ConvertRPSTNodeToPetriNetParameters parameters) {
		if (parameters.isTryConnections()) {
			// Try to found a connection that matches the inputs and the parameters.
			Collection<ConvertRPSTNodeToPetriNetConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						ConvertRPSTNodeToPetriNetConnection.class, context);
				for (ConvertRPSTNodeToPetriNetConnection connection : connections) {
					if (connection.getObjectWithRole(ConvertRPSTNodeToPetriNetConnection.RPSTNODE)
							.equals(node) && connection.getParameters().equals(parameters)) {
						// Found a match. Return the associated output as result of the algorithm.
						return connection
								.getObjectWithRole(ConvertRPSTNodeToPetriNetConnection.NET);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		// No connection found. Apply the algorithm to compute a fresh output result.
		Petrinet net = apply(context, node, parameters);
		if (parameters.isTryConnections()) {
			// Store a connection containing the inputs, output, and parameters.
			context.getConnectionManager().addConnection(
					new ConvertRPSTNodeToPetriNetConnection(node, net, parameters));
		}
		// Return the petri net.
		return net;
	}
}
