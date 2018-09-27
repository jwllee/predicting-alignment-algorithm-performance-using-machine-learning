package org.processmining.decomposedreplayer.experiments.parameters;

import java.util.HashSet;
import java.util.Set;

public class TestRecomposingReplayWithMergeStrategyParameters {

	// log and model names
	public String log = "";
	public String model = "";
	
	public String modelPath = "";
	public String logPath = "";
	
	public String iteration = "";
	public String outFile = "";
	
	public int globalDuration = -1;
	public int localDuration = -1;
	public int moveOnLogCosts = -1;
	public int moveOnModelCosts = -1;
	public int intervalRelative = -1;
	public int intervalAbsolute = -1;
	public int maxConflicts = -1;
	public int alignmentPercentage = -1;
	public int nofIterations = -1;
	
	public boolean useHideAndReduceAbstraction = true;
	
	public Set<String> initialDecompositionSet = new HashSet<>();

	public String decomposition = "";
	public String initDecompFile = "";
	
	public String recomposeStrategy = "";
	public String logCreationStrategy = "";
	
	public boolean preferBorderTransitions = false;
	public boolean addConflictOnlyOnce = true;
	
	@Override
	public String toString() {
		return "Iteration: " + iteration + "\n" + 
               "Log: " + log + "\n" + 
               "Model: " + model + "\n" + 
               "Log path: " + logPath + "\n" + 
               "Model path: " + modelPath + "\n" + 
               "Decomposition: " + decomposition + "\n" +
               "Initial decomposition file" + initDecompFile + "\n" +
               "Out file: " + outFile + "\n" + 
               "Global duration: " + globalDuration + "\n" + 
               "Local duration: " + localDuration + "\n" + 
               "Move on log costs: " + moveOnLogCosts + "\n" + 
               "Move on model costs: " + moveOnModelCosts + "\n" + 
               "Relative interval: " + intervalRelative + "\n" + 
               "Absolute interval: " + intervalAbsolute + "\n" + 
               "Max conflicts: " + maxConflicts + "\n" + 
               "Alignment percentage: " + alignmentPercentage + "\n" + 
               "Nb. of iterations: " + nofIterations + "\n" + 
               "Use hide and reduce abstraction: " + useHideAndReduceAbstraction + "\n" + 
               "Prefer border transitions: " + preferBorderTransitions + "\n" + 
               "Add conflicts only once: " + addConflictOnlyOnce + "\n" +
               "Recompose strategy: " + recomposeStrategy + "\n" + 
               "Log creation strategy: " + logCreationStrategy;
	}
	
}
