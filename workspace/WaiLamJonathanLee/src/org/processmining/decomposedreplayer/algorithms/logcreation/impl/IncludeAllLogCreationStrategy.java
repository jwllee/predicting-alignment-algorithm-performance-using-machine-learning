package org.processmining.decomposedreplayer.algorithms.logcreation.impl;

import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationInformation;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationStrategy;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * Include all the log traces of the open alignments into the new log.
 * 
 * @author jonathan
 *
 */
public class IncludeAllLogCreationStrategy implements LogCreationStrategy {
	
	public static final String NAME = "Include all";

	public void createLog(XLog oldLog, XLog newLog, Map<Integer, Integer> indexMap, 
			Set<SyncReplayResult> currOpenAlignments, Set<SyncReplayResult> newOpenAlignments,
			LogCreationInformation info) {
		for (SyncReplayResult openAlignment: currOpenAlignments) {
			for (int index: openAlignment.getTraceIndex()) {
				// Remember the index to the trace in the original log
				indexMap.put(newLog.size(), index);
				newLog.add(oldLog.get(index));
			}
		}
	}
	
	public String getName() {
		return NAME;
	}
	
}
