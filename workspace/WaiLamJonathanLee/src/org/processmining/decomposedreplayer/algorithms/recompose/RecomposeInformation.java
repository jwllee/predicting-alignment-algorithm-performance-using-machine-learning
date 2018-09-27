package org.processmining.decomposedreplayer.algorithms.recompose;

import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public interface RecomposeInformation {

	Map<XEventClass, Double> getLogConflicts();
	
	PluginContext getContext();
	
	Set<XEventClass> getUnsplittables();
	
	Set<SyncReplayResult> getOpenAlignments();
	
	Set<XEventClass> getInitialDecomposition();
	
	ActivityClusterArray getLastActivityClusters();
	
}
