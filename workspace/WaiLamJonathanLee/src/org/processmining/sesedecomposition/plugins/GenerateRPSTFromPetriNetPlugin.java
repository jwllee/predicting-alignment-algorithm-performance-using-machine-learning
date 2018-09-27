package org.processmining.sesedecomposition.plugins;

import java.util.Collection;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.sesedecomposition.algorithms.GenerateRPSTFromPetriNetAlgorithm;
import org.processmining.sesedecomposition.connections.GenerateRPSTFromAcceptingPetriNetConnection;
import org.processmining.sesedecomposition.help.GenerateRPSTFromPetriNetHelp;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPST;
import org.processmining.sesedecomposition.parameters.GenerateRPSTFromPetriNetParameters;

@Plugin(name = "Generate RPST from Accepting Petri Net", parameterLabels = { "Accepting Petri Net", "Parameters" }, 
returnLabels = { "Petri Net RPST" }, returnTypes = { PetriNetRPST.class }, help = GenerateRPSTFromPetriNetHelp.TEXT)

public class GenerateRPSTFromPetriNetPlugin extends GenerateRPSTFromPetriNetAlgorithm {
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Generate RPST from Accepting Petri Net", requiredParameterLabels = { 0, 1 })
	public PetriNetRPST run(PluginContext context, AcceptingPetriNet apn,
			GenerateRPSTFromPetriNetParameters parameters) {
		// Apply the algorithm depending on whether a connection already exists.
		return runConnections(context, apn, parameters);
	}
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Generate RPST from Accepting Petri Net, default", requiredParameterLabels = { 0 })
	public PetriNetRPST runDefault(PluginContext context, AcceptingPetriNet net) {
		GenerateRPSTFromPetriNetParameters parameters = new GenerateRPSTFromPetriNetParameters();
		String name = "RPST of " + net.getNet().getLabel();
		parameters.setName(name);
		// Apply the algorithm depending on whether a connection already exists.
		return runConnections(context, net, parameters);
	}
	
	private PetriNetRPST runConnections(PluginContext context, AcceptingPetriNet net,
			GenerateRPSTFromPetriNetParameters parameters) {
		if (parameters.isTryConnections()) {
			// Try to found a connection that matches the inputs and the parameters.
			Collection<GenerateRPSTFromAcceptingPetriNetConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						GenerateRPSTFromAcceptingPetriNetConnection.class, context);
				for (GenerateRPSTFromAcceptingPetriNetConnection connection : connections) {
					if (connection.getObjectWithRole(GenerateRPSTFromAcceptingPetriNetConnection.NET)
							.equals(net) && connection.getParameters().equals(parameters)) {
						// Found a match. Return the associated output as result of the algorithm.
						return connection
								.getObjectWithRole(GenerateRPSTFromAcceptingPetriNetConnection.RPST);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		// No connection found. Apply the algorithm to compute a fresh output result.
		PetriNetRPST rpst = apply(context, net, parameters);
		if (parameters.isTryConnections()) {
			// Store a connection containing the inputs, output, and parameters.
			context.getConnectionManager().addConnection(
					new GenerateRPSTFromAcceptingPetriNetConnection(net, rpst, parameters));
		}
		// Return the rpst
		return rpst;
	}

}
