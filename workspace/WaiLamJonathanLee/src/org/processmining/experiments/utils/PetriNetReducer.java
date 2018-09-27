package org.processmining.experiments.utils;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinetclassicalreductor.parameters.ReduceUsingMurataRulesParameters;
import org.processmining.acceptingpetrinetclassicalreductor.plugins.ReduceUsingMurataRulesPlugin;
import org.processmining.contexts.cli.CLIPluginContext;

public class PetriNetReducer {
	
	private ReduceUsingMurataRulesPlugin netReducer;
	private AcceptingPetriNet apn;
	
	public PetriNetReducer(AcceptingPetriNet apn, ReduceUsingMurataRulesPlugin netReducer) {
		this.netReducer = netReducer;
		this.apn = apn;
	}
	
	public AcceptingPetriNet reduceAPN(CLIPluginContext context) {
		ReduceUsingMurataRulesParameters parameters = new ReduceUsingMurataRulesParameters();
		// retain behavior
		parameters.setRetainBehavior(true);
		return netReducer.apply(context, apn, parameters);
	}
	
}
