package org.processmining.experiments.utils;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ReplayResultCsvWriter {
	
	private final static StringFileWriter writer = new StringFileWriter();

	public static void writeReplayResultToCsv(SyncReplayResult replayResult, String fp, 
			TransEvClassMapping mapping) {
		List<String> moves = alignment2moves(replayResult, mapping);
		writer.writeStringListToFile(moves, fp, true);
	}
	
	private static List<String> alignment2moves(SyncReplayResult replayResult, TransEvClassMapping mapping) {
		List<String> moves = new ArrayList<>();
		moves.add("Log,Model");
		
		String move = "";
		
		for (int i = 0; i < replayResult.getStepTypes().size(); i++) {
			StepTypes stepType = replayResult.getStepTypes().get(i);
			Object nodeInstance = replayResult.getNodeInstance().get(i);
			
			if (isMove(stepType)) {
				XEventClass eventClass = null;
				
				if (nodeInstance instanceof XEventClass) {
					eventClass = (XEventClass) nodeInstance;
				} else if (nodeInstance instanceof Transition) {
					eventClass = mapping.get(nodeInstance);
				} else {
					System.err.println("Unknown node instance: " + nodeInstance);
				}
				
				// add as a string
				if (stepType == StepTypes.LMGOOD) {
					move = eventClass.getId() + "," + eventClass.getId();
				} else if (stepType == StepTypes.L) {
					move = eventClass.getId() + ",>>";
				} else if (stepType == StepTypes.MREAL) {
					move = ">>," + eventClass.getId();
				} else if (stepType == StepTypes.MINVI) {
					move = ">>,invis";
				}
				
				moves.add(move);
			}
		}
		
		return moves;
	}
	
	private static boolean isMove(StepTypes stepType) {
		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L || stepType == StepTypes.MREAL
				|| stepType == StepTypes.MINVI;
	}
	
}
