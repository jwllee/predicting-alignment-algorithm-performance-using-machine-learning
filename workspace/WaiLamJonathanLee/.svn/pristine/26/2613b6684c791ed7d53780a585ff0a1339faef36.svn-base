package org.processmining.decomposedreplayer.experiments.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	public Map<String, String> readJsonStringAsStringMap(String json) {
		
		Map<String, String> map = new HashMap<>();
		
		try {

			ObjectMapper mapper = new ObjectMapper();

			// convert JSON string to Map
			map = mapper.readValue(json, new TypeReference<Map<String, String>>(){});


		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return map;
		
	}
	
}
