package org.processmining.sesedecomposition.connections;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.sesedecomposition.parameters.DecomposeBySESEsWithBridgingParameters;

public class DecomposeBySESEsWithBridgingConnection extends AbstractConnection {
	
	public final static String NET = "Accepting Petri Net";
	public final static String NETARRAY = "Accepting Petri Net Array";
	
	private DecomposeBySESEsWithBridgingParameters parameters;
	
	public DecomposeBySESEsWithBridgingConnection(AcceptingPetriNet net, 
			AcceptingPetriNetArray netArray, DecomposeBySESEsWithBridgingParameters parameters) {
		super("Decompose Accepting Petri Net by SESE with Bridging Connection");
		put(NET, net);
		put(NETARRAY, netArray);
		this.parameters = parameters;
	}
	
	public DecomposeBySESEsWithBridgingParameters getParameters() {
		return parameters;
	}

}
