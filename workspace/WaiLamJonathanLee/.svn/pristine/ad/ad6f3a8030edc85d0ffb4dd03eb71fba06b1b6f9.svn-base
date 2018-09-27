package org.processmining.decomposedreplayer.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogAlignmentJson {

	private Map<String, TraceAlignmentJson> traceAlignments;
	
	public LogAlignmentJson() {
		this.setTraceAlignments(new HashMap<String, TraceAlignmentJson>());
	}

	public Map<String, TraceAlignmentJson> getTraceAlignments() {
		return traceAlignments;
	}

	public void setTraceAlignments(Map<String, TraceAlignmentJson> traceAlignments) {
		this.traceAlignments = traceAlignments;
	}
	
	public void addTraceAlignment(TraceAlignmentJson alignment) {
		if (traceAlignments.containsKey(alignment._id)) {
			String err = String.format("Already contain alignment id: %s", alignment._id);
			throw new IllegalArgumentException(err);
		}
		traceAlignments.put(alignment._id, alignment);
	}
	
	public String writeToFile(String filepath) {
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonInString = "";
		try {
			mapper.writeValue(new File(filepath), this);
			jsonInString = mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonInString;
	}
}
