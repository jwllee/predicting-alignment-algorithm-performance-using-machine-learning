package org.processmining.experiments.parameters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestAcceptingPetriNetDecompositionParameters {

	public static final String CLASSIFIER_CONCEPTNAME = "conceptName";
    public static final String CLASSIFIER_STANDARD = "standard";
	
	public String netFilePath = "";
	public String classifierType = "";
	public String decompositionStrategy = "";
	public String netArrayFilePath = "";
	public boolean includeClusterTransitions = false;
	
	public String unsplittableFilePath = "";
	public Set<String> unsplittableActLabels = new HashSet<>();
	
	public Map<String, String> optionalConfigs = new HashMap<>();
	
	@Override
	public String toString() {
		
		return "Net file path: " + netFilePath + "\n" + 
				"Classifier type: " + classifierType + "\n" + 
				"Decomposition strategy: " + decompositionStrategy + "\n" + 
				"Net array file path: " + netArrayFilePath + "\n" + 
				"Include cluster transitions: " + includeClusterTransitions + "\n" + 
				"Unsplittable file path: " + unsplittableFilePath + "\n" +
				"Unsplittable activity labels: " + unsplittableActLabels.toString() + "\n" + 
				"Optional configurations: " + optionalConfigs.toString();
		
	}
	
}
