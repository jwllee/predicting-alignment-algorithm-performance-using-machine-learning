package org.processmining.alignment.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.alignment.monolithic.AlignmentTestParameters;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class AlignUtils {
	
	public static void toCsv(AlignmentTestParameters params, SyncReplayResult result, PrintStream stream) {
		
		
		
	}

	public static void toCsv(SyncReplayResult result, TransEvClassMapping mapping, PrintStream stream) {
		List<String> moves = toMoves(result, mapping);

		synchronized (stream) {
			for (int i = 0; i < moves.size(); ++i) {
				stream.println(moves.get(i));
			}
		}
	}
	
	public static List<String> toMoves(SyncReplayResult result, TransEvClassMapping mapping) {
		
		List<String> moves = new ArrayList<>();
		moves.add("Log,Model");
		
		String move = "";
		
		for (int i = 0; i < result.getStepTypes().size(); ++i) {
			
			StepTypes stepType = result.getStepTypes().get(i);
			Object nodeInstance = result.getNodeInstance().get(i);
			
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
