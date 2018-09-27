package org.processmining.sesedecomposition.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.parameters.ConvertRPSTNodeToPetriNetParameters;

public class ConvertRPSTNodeToPetriNetConnection extends AbstractConnection {
	
	public final static String RPSTNODE = "Petri net RPST node";
	public final static String NET = "Petri net";
	
	private ConvertRPSTNodeToPetriNetParameters parameters;
	
	public ConvertRPSTNodeToPetriNetConnection(PetriNetRPSTNode node, Petrinet net,
			ConvertRPSTNodeToPetriNetParameters parameters) {
		super("Convert RPST Node To Petri Net Connection");
		put(RPSTNODE, node);
		put(NET, net);
		this.parameters = parameters;
	}
	
	public ConvertRPSTNodeToPetriNetParameters getParameters() {
		return parameters;
	}
}
