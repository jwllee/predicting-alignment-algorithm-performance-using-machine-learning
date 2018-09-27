package org.processmining.decomposedreplayer.experiments.parameters;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterReader {
    
    public TestRecomposingReplayWithMergeStrategyParameters readRReplayParams(String jsonString) {
        try {
            return mapJSONStringToObject(jsonString, TestRecomposingReplayWithMergeStrategyParameters.class);
        } catch (JsonMappingException jme) {
            String errorMsg = "Cannot map " + jsonString + " as " + 
            		TestRecomposingReplayWithMergeStrategyParameters.class.getTypeName();
            System.out.println(errorMsg);
            jme.printStackTrace();
        } catch (IOException ioe) {
            String errorMsg = "Cannot map " + jsonString + " as " +
            		TestRecomposingReplayWithMergeStrategyParameters.class.getTypeName();
            System.out.println(errorMsg);
            ioe.printStackTrace();
        }          
        
        return new TestRecomposingReplayWithMergeStrategyParameters();
    }
        
    private <T> T mapJSONStringToObject(String jsonString, Class<T> type) throws IOException, JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, type);
    }
	
}
