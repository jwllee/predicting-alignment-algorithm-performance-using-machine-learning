package org.processmining.experiments.parameters;

public class ComputeAcceptingPetriNetMetricsParameters {

	public String netPath = "";
	
	public String outFile = "";
	
	@Override
	public String toString() {
		return "Net path: " + netPath + "\n" + 
				"Out file: " + outFile + "\n";
	}
	
}
