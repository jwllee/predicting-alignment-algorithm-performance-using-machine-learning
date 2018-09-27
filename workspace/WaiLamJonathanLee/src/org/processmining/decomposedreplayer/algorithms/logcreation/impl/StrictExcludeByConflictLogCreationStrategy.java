package org.processmining.decomposedreplayer.algorithms.logcreation.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationInformation;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationStrategy;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class StrictExcludeByConflictLogCreationStrategy implements LogCreationStrategy {

	public static final String NAME = "Strict exclude by conflicts";
	
	private ConflictUtils conflictUtils = new ConflictUtils();
	
	public void createLog(XLog oldLog, XLog newLog, Map<Integer, Integer> indexMap,
			Set<SyncReplayResult> currOpenAlignments, Set<SyncReplayResult> newOpenAlignments,
			LogCreationInformation info) {
		Set<XEventClass> borderActivities = info.getBorderActivities();
		for (SyncReplayResult openAlignment: currOpenAlignments) {
			Set<XEventClass> conflictingActivities = conflictUtils.getConflicts(openAlignment).keySet();
//			System.out.println("[" + getClass().getSimpleName() + "] Conflicting activities of sub-alignment: " + conflictingActivities);
//			System.out.println("[" + getClass().getSimpleName() + "] Border activities: " + borderActivities);
			Set<XEventClass> aux = new HashSet<>(borderActivities);
			aux.retainAll(conflictingActivities);
//			System.out.println("[" + getClass().getSimpleName() + "] Intersection activity set: " + aux);
			// border activities cannot contain any conflicting activities
			if (aux.isEmpty()) {
				for (int index: openAlignment.getTraceIndex()) {
					// remember the index to the trace in the original log
					indexMap.put(newLog.size(), index);
					newLog.add(oldLog.get(index));
				}
			} else {
				newOpenAlignments.add(openAlignment);
			}
		}
		
		if (newLog.size() == 0) 
			throw new IllegalStateException("[" + getClass().getSimpleName() + "] New log has 0 log traces!");
	}

	public String getName() {
		return NAME;
	}

}
