package org.processmining.sesedecomposition.connections;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPST;
import org.processmining.sesedecomposition.parameters.GenerateRPSTFromPetriNetParameters;

public class GenerateRPSTFromAcceptingPetriNetConnection extends AbstractConnection {

	public final static String NET = "Accepting Petri Net";
	public final static String RPST = "Petri Net RPST";
	
	private GenerateRPSTFromPetriNetParameters parameters;
	
	public GenerateRPSTFromAcceptingPetriNetConnection(AcceptingPetriNet net,
			PetriNetRPST rpst, GenerateRPSTFromPetriNetParameters parameters) {
		super("Generate RPST from Accepting Petri Net Connection");
		put(NET, net);
		put(RPST, rpst);
		this.parameters = parameters;
	}
	
	public GenerateRPSTFromPetriNetParameters getParameters() {
		return parameters;
	}
	
}
