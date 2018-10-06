package org.processmining.experiments.parameters;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDecomposedReplayParameters {

	// log and model names
	public String log = "";
	public String model = "";
	
	public String logPath = "";
    public String modelPath = "";

	public String configuration = "";
	public String iteration = "";
	public String outFile = "";
	public String resultDir = "";
	
	public int moveOnLogCosts = 0;
	public int moveOnModelCosts = 0;
	public long deadline = 0;

	@Override
	public String toString() {
		return  "Result directory: " + resultDir + "\n" +
				"Configurations: " + configuration + "\n" + 
                "Deadline: " + deadline + "\n" + 
                "Iteration: " + iteration + "\n" + 
                "Log: " + log + "\n" + 
                "Model: " + model + "\n" + 
                "Log path: " + logPath + "\n" + 
                "Model path: " + modelPath + "\n" + 
                "Out file: " + outFile;
	}
	
	public static TestDecomposedReplayParameters readParams(String jsonString) {
		
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			TestDecomposedReplayParameters params = mapper.readValue(jsonString, TestDecomposedReplayParameters.class);
			return params;
			
		} catch (JsonMappingException jme) {
			
			String errorMsg = "Cannot map " + jsonString + " as " + TestDecomposedReplayParameters.class.getSimpleName();
			System.out.println(errorMsg);
			jme.printStackTrace();
			
		} catch (IOException ioe) {
			
			String errorMsg = "Cannot map " + jsonString + " as " + TestDecomposedReplayParameters.class.getSimpleName();
			ioe.printStackTrace();
			
		}
		
		return new TestDecomposedReplayParameters();
		
	}
	
}
