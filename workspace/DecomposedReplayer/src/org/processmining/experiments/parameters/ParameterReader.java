package org.processmining.experiments.parameters;

import java.io.IOException;

import org.processmining.experiments.configurations.CleanDataConfigurations;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterReader {
    
    public TestRecomposingReplayParameters readRReplayParams(String jsonString) {
        try {
            return mapJSONStringToObject(jsonString, TestRecomposingReplayParameters.class);
        } catch (JsonMappingException jme) {
            String errorMsg = "Cannot map " + jsonString + " as " + 
            		TestRecomposingReplayParameters.class.getTypeName();
            System.out.println(errorMsg);
            jme.printStackTrace();
        } catch (IOException ioe) {
            String errorMsg = "Cannot map " + jsonString + " as " +
            		TestRecomposingReplayParameters.class.getTypeName();
            System.out.println(errorMsg);
            ioe.printStackTrace();
        }          
        
        return new TestRecomposingReplayParameters();
    }
    
    public TestDecomposedReplayParameters readDReplayParams(String jsonString) {
    	try {
    		return mapJSONStringToObject(jsonString, TestDecomposedReplayParameters.class);
    	} catch (JsonMappingException jme) {
    		String errorMsg = "Cannot map" + jsonString + " as " + 
    				TestDecomposedReplayParameters.class.getTypeName();
    		System.out.println(errorMsg);
    		jme.printStackTrace();
    	} catch (IOException ioe) {
    		String errorMsg = "Cannot map" + jsonString + " as " + 
    				TestDecomposedReplayParameters.class.getTypeName();
    		System.out.println(errorMsg);
    		ioe.printStackTrace();
    	}
    	
    	return new TestDecomposedReplayParameters();
    }
    
    public CleanDataConfigurations readCleanDataConfigs(String jsonString) {
    	try {
    		return mapJSONStringToObject(jsonString, CleanDataConfigurations.class);
    	} catch (JsonMappingException jme) {
    		String errorMsg = "Cannot map " + jsonString + " as " + 
    				CleanDataConfigurations.class.getTypeName();
    		System.out.println(errorMsg);
    		jme.printStackTrace();
    	} catch (IOException ioe) {
    		String errorMsg = "Cannot map " + jsonString + " as " + 
    				CleanDataConfigurations.class.getTypeName();
    		System.out.println(errorMsg);
    		ioe.printStackTrace();
    	}
    	return new CleanDataConfigurations();
    }
    
    public TestSharedActivitiesParameters readSharedActsParams(String jsonString) {
    	
    	try {
    		
    		return mapJSONStringToObject(jsonString, TestSharedActivitiesParameters.class);
    		
    	} catch (JsonMappingException jme) {
    		
    		String errorMsg = "Cannot map " + jsonString + " as " + 
    				TestSharedActivitiesParameters.class.getTypeName();
    		System.out.println(errorMsg);
    		jme.printStackTrace();
    		
    	} catch (IOException ioe) {
    		
    		String errorMsg = "Cannot map " + jsonString + " as " + 
    				TestSharedActivitiesParameters.class.getTypeName();
    		System.out.println(errorMsg);
    		ioe.printStackTrace();
    		
    	}
    	
    	return new TestSharedActivitiesParameters();
    }
    
    private <T> T mapJSONStringToObject(String jsonString, Class<T> type) throws IOException, JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, type);
    }
	
}
