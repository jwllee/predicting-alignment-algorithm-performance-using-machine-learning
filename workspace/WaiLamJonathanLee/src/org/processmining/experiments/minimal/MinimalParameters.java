package org.processmining.experiments.minimal;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MinimalParameters {

	public String netPath = "";
	
	public String logPath = "";
	
	public String outFile = "";
	
	@Override
	public String toString() {
		return "Net path: " + netPath + "\n" + 
				"Log path: " + logPath + "\n" + 
				"Out file: " + outFile + "\n";
	}
	
	public static MinimalParameters readMinimalParams(String jsonString) {
		
		try {
			
			ObjectMapper mapper = new ObjectMapper();
	        return mapper.readValue(jsonString, MinimalParameters.class);
			
		} catch (JsonMappingException jme) {
    		
    		String errorMsg = "Cannot map " + jsonString + " as " + 
    				MinimalParameters.class.getTypeName();
    		System.out.println(errorMsg);
    		jme.printStackTrace();
    		
    	} catch (IOException ioe) {
    		
    		String errorMsg = "Cannot map " + jsonString + " as " + 
    				MinimalParameters.class.getTypeName();
    		System.out.println(errorMsg);
    		ioe.printStackTrace();
    		
    	}
    	
    	return new MinimalParameters();
		
	}
}
