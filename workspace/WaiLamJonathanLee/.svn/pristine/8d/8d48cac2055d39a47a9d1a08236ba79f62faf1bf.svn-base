package org.processmining.sesedecomposition.algorithms;

import java.util.List;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPST;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.parameters.DecomposeBySESEsWithBridgingParameters;
import org.processmining.sesedecomposition.plugins.GenerateRPSTFromPetriNetPlugin;
import org.processmining.sesedecomposition.utils.dc.BridgingUtils;
import org.processmining.sesedecomposition.utils.dc.PartitioningUtils;

public class DecomposeBySESEsWithBridgingAlgorithm {

	public AcceptingPetriNetArray apply(PluginContext context, AcceptingPetriNet apn,
			DecomposeBySESEsWithBridgingParameters parameters) {
		
		// Generate the RPST
		GenerateRPSTFromPetriNetPlugin generateRPSTPlugin = new GenerateRPSTFromPetriNetPlugin();
		PetriNetRPST rpst = generateRPSTPlugin.runDefault(context, apn);
		context.getProvidedObjectManager().createProvidedObject(rpst.getName(), rpst, 
				PetriNetRPST.class, context);
		
		// Make a K-partition over the RPST
		List<PetriNetRPSTNode> partNodes = PartitioningUtils.partitioning(rpst, parameters.getMaxSize());
		
		// Bridging (create explicit separate nets for the common places among nets)
		AcceptingPetriNetArray netsBridged = BridgingUtils.bridging(rpst.getNet(), partNodes);
		
		return netsBridged;
	}
	
}
