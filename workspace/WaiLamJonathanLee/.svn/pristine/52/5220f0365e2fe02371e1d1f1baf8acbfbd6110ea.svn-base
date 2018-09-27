package org.processmining.decomposedreplayer.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TraceAlignmentJson {

	public final String _id;
	// e.g., total_order, overall/decomposed, control_flow
	private Set<String> alignmentType;
	private Map<String, Object> stats;
	private List<StepJson> steps;
	private boolean reliable;
	
	public TraceAlignmentJson(String id) {
		this._id = id;
		this.setAlignmentType(new HashSet<String>());
		this.setStats(new HashMap<String, Object>());
		this.setSteps(new LinkedList<StepJson>());
	}
	
	public Set<String> getAlignmentType() {
		return alignmentType;
	}

	public void setAlignmentType(Set<String> alignmentType) {
		this.alignmentType = alignmentType;
	}

	public void addAlignmentTypeInfo(String info) {
		this.alignmentType.add(info);
	}
	
	public boolean removeAlignmentTypeInfo(String info) {
		if (alignmentType.contains(info))
			return alignmentType.remove(info);
		return false;
	}
	
	public Map<String, Object> getStats() {
		return stats;
	}

	public void setStats(Map<String, Object> stats) {
		this.stats = stats;
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

	public List<StepJson> getSteps() {
		return steps;
	}

	public void setSteps(List<StepJson> steps) {
		this.steps = steps;
	}

	public boolean isReliable() {
		return reliable;
	}

	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}
	
}
