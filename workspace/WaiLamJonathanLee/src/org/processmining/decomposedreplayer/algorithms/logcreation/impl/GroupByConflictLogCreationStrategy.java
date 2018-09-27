package org.processmining.decomposedreplayer.algorithms.logcreation.impl;

import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationInformation;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationStrategy;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * Filter log so that only log traces that have conflicts on the unsplittable activities are included
 * in the next iteration.
 * 
 * @author jonathan
 *
 */
public class GroupByConflictLogCreationStrategy implements LogCreationStrategy {
	
	public static final String NAME = "Group by conflicts";
	
	private ConflictUtils conflictUtils = new ConflictUtils();

	public void createLog(XLog oldLog, XLog newLog, Map<Integer, Integer> indexMap, 
			Set<SyncReplayResult> currOpenAlignments, Set<SyncReplayResult> newOpenAlignments,
			LogCreationInformation info) {
		for (SyncReplayResult openAlignment: currOpenAlignments) {
			Set<XEventClass> conflictingActivities = conflictUtils.getConflicts(openAlignment).keySet();
			conflictingActivities.retainAll(info.getUnsplittables());
			if (!conflictingActivities.isEmpty()) {
				for (int index: openAlignment.getTraceIndex()) {
					// Remember the index to the trace in the original log
					indexMap.put(newLog.size(), index);
					newLog.add(oldLog.get(index));
				}
			} else {
				newOpenAlignments.add(openAlignment);
			}
		}
	}

	public String getName() {
		return NAME;
	}
	
}
