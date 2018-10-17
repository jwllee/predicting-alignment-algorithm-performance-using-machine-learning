package org.processmining.alignment.monolithic;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlignmentTestParameters {

	public String log = "";
	public String model = "";

	public String logPath = "";
    public String modelPath = "";

    public String debug = "";
    public String configuration = "";
	public String iteration = "";
	public String outFile = "";
	public String resultDir = "";
	
	public int moveOnLogCosts = 0;
	public int moveOnModelCosts = 0;
	public int timeoutPerTraceInSec = 0;

	@Override
	public String toString() {
		return  "Result directory: " + resultDir + "\n" +
				"Debug: " + debug + "\n" +
				"Configurations: " + configuration + "\n" + 
                "Time out per trace in sec: " + timeoutPerTraceInSec + "\n" + 
                "Iteration: " + iteration + "\n" + 
                "Log: " + log + "\n" + 
                "Model: " + model + "\n" + 
                "Log path: " + logPath + "\n" + 
                "Model path: " + modelPath + "\n" + 
                "Out file: " + outFile;
	}
	
	public static AlignmentTestParameters readParams(String jsonString) {
		
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			AlignmentTestParameters params = mapper.readValue(jsonString, AlignmentTestParameters.class);
			return params;
			
		} catch (JsonMappingException jme) {
			
			String errorMsg = "Cannot map " + jsonString + " as " + AlignmentTestParameters.class.getSimpleName();
			System.out.println(errorMsg);
			jme.printStackTrace();
			
		} catch (IOException ioe) {
			
			String errorMsg = "Cannot map " + jsonString + " as " + AlignmentTestParameters.class.getSimpleName();
			System.out.println(errorMsg);
			ioe.printStackTrace();
			
		}
		
		return new AlignmentTestParameters();
	}
}
