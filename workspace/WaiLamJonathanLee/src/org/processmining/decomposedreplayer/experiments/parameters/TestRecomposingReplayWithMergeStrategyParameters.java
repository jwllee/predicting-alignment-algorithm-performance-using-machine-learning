package org.processmining.decomposedreplayer.experiments.parameters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestRecomposingReplayWithMergeStrategyParameters {
	public static final String ASTAR = "astar";
	public static final String INC = "inc";
	public static final String INC0 = "inc0";
	public static final String INC3 = "inc3";
	public static final String INC_PLUS = "inc+";

	public static final String STATS = "stats";
	public static final String DOT = "dot";
	
	// log and model names
	public String log = "";
	public String model = "";
	
	public String modelPath = "";
	public String logPath = "";
	
	public String iteration = "";
	public String outFile = "";
	public String resultDir = "";
	
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

	public String algorithmType;
	public boolean moveSort;
	public boolean queueSort;
	public boolean preferExact;
	public int nThreads;
	public boolean useInt;
	public String debug;
	public int timeoutPerTraceInSecs;
	public int maximumNumberOfStates;
	public int costUpperBound;
	public boolean partiallyOrderEvents;
	public boolean preProcessUsingPlaceBasedConstraints;
	public int initialSplits;
	public boolean printAlignments;
	
	@Override
	public String toString() {
		return "Result directory: " + resultDir + "\n" +
			   "Iteration: " + iteration + "\n" + 
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
               "Log creation strategy: " + logCreationStrategy + "\n" + 
               "Algorithm type: " + algorithmType + "\n" + 
               "Move sort: " + moveSort + "\n" +
               "Queue sort: " + queueSort + "\n" + 
               "Prefer exact: " + preferExact + "\n" + 
               "Nb. of threads: " + nThreads + "\n" +
               "Use ILP: " + useInt + "\n" + 
               "Debug: " + debug + "\n" + 
               "Timeout per trace in secs: " + timeoutPerTraceInSecs + "\n" +
               "Maximum number of states: " + maximumNumberOfStates + "\n" +
               "Cost upper bound: " + costUpperBound + "\n" +
               "Partially order events: " + partiallyOrderEvents + "\n" +
               "Preprocess using place based constraints: " + preProcessUsingPlaceBasedConstraints + "\n" +
               "Initial splits: " + initialSplits + "\n" +
               "Print alignments: " + printAlignments + "\n";
	}
	
	public static TestRecomposingReplayWithMergeStrategyParameters readParams(String jsonString) {
		try {
			
			ObjectMapper mapper = new ObjectMapper();
			TestRecomposingReplayWithMergeStrategyParameters params = mapper.readValue(jsonString, TestRecomposingReplayWithMergeStrategyParameters.class);
			return params;
			
		} catch (JsonMappingException jme) {
			
			String errorMsg = "Cannot map " + jsonString + " as " + TestRecomposingReplayWithMergeStrategyParameters.class.getSimpleName();
			System.out.println(errorMsg);
			jme.printStackTrace();
			
		} catch (IOException ioe) {
			
			String errorMsg = "Cannot map " + jsonString + " as " + TestRecomposingReplayWithMergeStrategyParameters.class.getSimpleName();
			System.out.println(errorMsg);
			ioe.printStackTrace();
			
		}
		
		return new TestRecomposingReplayWithMergeStrategyParameters();
	}
	
}
