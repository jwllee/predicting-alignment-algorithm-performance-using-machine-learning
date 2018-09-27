package org.processmining.experiments.parameters;

public class TestDecomposedReplayParameters {

	// log and model names
	public String log = "";
	public String model = "";
	
	public String logPath = "";
    public String modelPath = "";

	public String configuration = "";
	public String iteration = "";
	public String outFile = "";
	
	public int moveOnLogCosts = 0;
	public int moveOnModelCosts = 0;
	public long deadline = 0;

	@Override
	public String toString() {
		return "Configurations: " + configuration + "\n" + 
                "Deadline: " + deadline + "\n" + 
                "Iteration: " + iteration + "\n" + 
                "Log: " + log + "\n" + 
                "Model: " + model + "\n" + 
                "Log path: " + logPath + "\n" + 
                "Model path: " + modelPath + "\n" + 
                "Out file: " + outFile;
	}
	
}
