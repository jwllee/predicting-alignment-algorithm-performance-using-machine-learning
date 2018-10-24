package org.processmining.alignment.monolithic;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlignmentTestParameters {

	public static final String ASTAR = "astar";
	public static final String INC0 = "inc0";
	public static final String INC3 = "inc3";
	public static final String INC_PLUS = "inc+";
	public static final String PLANNING = "planning";

	public String log = "";
	public String model = "";

	public String logPath = "";
    public String modelPath = "";
	
    public String debug = "";
    public String configuration = "";
    public int threads = 1;
    public boolean moveSort = false;
    public boolean useInt = false;
    public boolean partialOrder = false;
    public boolean preferExact = true;
    public boolean queueSort = true;
    
	public String iteration = "";
	public String outFile = "";
	public String resultDir = "";
	
	public int moveOnLogCosts = 0;
	public int moveOnModelCosts = 0;
	public int timeoutPerTraceInSec = 0;
	
	public boolean printAlignment = false;

	@Override
	public String toString() {
		return  "Result directory: " + resultDir + "\n" +
				"Debug: " + debug + "\n" +
				"Configurations: " + configuration + "\n" + 
				"Threads: " + threads + "\n" +
                "Time out per trace in sec: " + timeoutPerTraceInSec + "\n" + 
				"Move sort: " + moveSort + "\n" +
                "Use ILP: " + useInt + "\n" +
				"Partial order events: " + partialOrder + "\n" +
                "Prefer exact heuristics: " + preferExact + "\n" +
				"Queue sort: " + queueSort + "\n" +
                "Iteration: " + iteration + "\n" + 
                "Log: " + log + "\n" + 
                "Model: " + model + "\n" + 
                "Log path: " + logPath + "\n" + 
                "Model path: " + modelPath + "\n" + 
                "Out file: " + outFile + "\n" +
                "Print alignment: " + printAlignment;
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
