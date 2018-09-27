package org.processmining.decomposedreplayer.algorithms.logcreation;

import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public interface LogCreationStrategy {

	public void createLog(XLog oldLog, XLog newLog, Map<Integer, Integer> indexMap, 
			Set<SyncReplayResult> currOpenAlignments, Set<SyncReplayResult> newOpenAlignments,
			LogCreationInformation info);
	
	public String getName();
	
}
