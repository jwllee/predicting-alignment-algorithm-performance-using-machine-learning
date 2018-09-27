package org.processmining.decomposedreplayer.utils;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.logalignment.algorithms.ConvertLogAlignmentToReplayResultAlgorithm;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ConflictUtils {

	public Map<XEventClass, Double> getConflicts(SyncReplayResult result) {
		Map<XEventClass, Double> conflicts = new HashMap<XEventClass, Double>();
		for (String label : result.getInfo().keySet()) {
			if (label.startsWith(ConvertLogAlignmentToReplayResultAlgorithm.CONFLICTPREFIX)
					&& result.getInfo().get(label) > 0.0) {
				conflicts.put(
						new XEventClass(label.substring(ConvertLogAlignmentToReplayResultAlgorithm.CONFLICTPREFIX
								.length()), 0), result.getInfo().get(label));
			}
		}
		return conflicts;
	}
	
	public void addConflicts(Map<XEventClass, Double> logConflicts, Map<XEventClass, Double> traceConflicts) {
		for (XEventClass activity : traceConflicts.keySet()) {
			logConflicts.put(activity, traceConflicts.get(activity)
					+ (logConflicts.containsKey(activity) ? logConflicts.get(activity) : 0.0));
		}
	}

	
}
