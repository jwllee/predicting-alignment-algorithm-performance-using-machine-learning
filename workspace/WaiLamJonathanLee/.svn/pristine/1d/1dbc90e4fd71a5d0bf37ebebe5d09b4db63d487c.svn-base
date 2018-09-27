package org.processmining.decomposedreplayer.algorithms.recompose.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;
import org.processmining.decomposedreplayer.models.recompose.Edge;
import org.processmining.decomposedreplayer.models.recompose.MergeConflictGraph;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class MergeConflictGraphRecomposeStrategy implements RecomposeStrategy {

	public static final String NAME = "Merge conflict graph";
	public static final double THRESHOLD = 0.8;
	
	private ConflictUtils conflictUtils = new ConflictUtils();
	private MergeConflictGraph graph;
	
	public double computeMaxEdgeWeight(MergeConflictGraph graph) {
		double max = -1;
		for (Double weight: graph.getEdgeWeightMap().values()) {
			if (Double.compare(max, weight) < 0)
				max = weight;
		}
		return max;
	}
	
	public MergeConflictGraph filterOutWeakEdges(MergeConflictGraph graph, double threshold) {
		MergeConflictGraph newGraph = new MergeConflictGraph();
		Map<Edge, Double> edgeWeightMap = graph.getEdgeWeightMap();
		for (Edge edge: edgeWeightMap.keySet()) {
			double edgeWeight = edgeWeightMap.get(edge);
			if (Double.compare(edgeWeight, threshold) >= 0) {
				// keep this edge, copy it of course
				Edge newEdge = new Edge(edge);
				newGraph.addEdge(newEdge, edgeWeight);
			}
		}
		return newGraph;
	}
	
	public Set<XEventClass> recompose(RecomposeInformation info) {
		Map<XEventClass, Double> logConflicts = info.getLogConflicts();
		Set<XEventClass> conflictingActivities = new HashSet<>(logConflicts.keySet());
		Set<SyncReplayResult> openAlignments = info.getOpenAlignments();
		
		// construct merge conflict graph
		graph = new MergeConflictGraph();
		
		System.out.println("[" + getClass().getSimpleName() + "] Constructing merge conflict graph...");
		
		for (SyncReplayResult traceAlignment: openAlignments) {
			Map<XEventClass, Double> traceConflicts = conflictUtils.getConflicts(traceAlignment);
			List<XEventClass> traceConflictKeys = new ArrayList<>(traceConflicts.keySet());
			
			System.out.println("[" + getClass().getSimpleName() + "] Conflicting activities: " + traceConflictKeys);
			
			if (traceConflictKeys.size() == 1) {
				// use a dummy node
				String n0 = traceConflictKeys.get(0).getId();
				Edge edge = new Edge(n0, MergeConflictGraph.DUMMY);
				graph.addEdge(edge, 1.0);
			} else {
				// Each pair of conflicting activities form an edge with weight 1
				for (int i = 0; i < traceConflictKeys.size() - 1; i++) {
					XEventClass a0 = traceConflictKeys.get(i);
					String n0 = a0.getId();
					for (int j = i + 1; j < traceConflictKeys.size(); j++) {
						XEventClass a1 = traceConflictKeys.get(j);
						String n1 = a1.getId();
//						System.out.println("[" + getClass().getSimpleName() + "] Putting " + n0 + ", " + n1 + " as edge.");
						Edge edge = new Edge(n0, n1);
						graph.addEdge(edge, 1.0);
					}
				}
			}
		}
		
//		System.out.println("[" + getClass().getSimpleName() + "] Merge conflict graph: \n" + graph.toString());
		System.out.println("[" + getClass().getSimpleName() + "] Number of conflicting activities: " + graph.nofNodes());
		System.out.println("[" + getClass().getSimpleName() + "] Number of conflicting activity relations: " + graph.nofEdges());
		
		// check that the nodes of merge conflict graph equals the conflicting activities
		Set<XEventClass> toCheck = new HashSet<>(conflictingActivities);
		for (String node: graph.getNodes()) {
			Set<XEventClass> toRemove = new HashSet<>();
			for (XEventClass activity: toCheck) {
				if (activity.getId().equals(node))
					toRemove.add(activity);
			}
			toCheck.removeAll(toRemove);
		}
		
		if (!toCheck.isEmpty()) {
			throw new IllegalStateException(toCheck + " are not nodes in merge conflict graph!");
		}
		
		// compute threshold for this graph
		double maxEdgeWeight = computeMaxEdgeWeight(graph);
		double weightThreshold = THRESHOLD * maxEdgeWeight;
		
		System.out.println("[" + getClass().getSimpleName() + "] Max edge weight: " + maxEdgeWeight);
		System.out.println("[" + getClass().getSimpleName() + "] Weight threshold of " + THRESHOLD + ": " + weightThreshold);
		
		MergeConflictGraph filteredGraph = filterOutWeakEdges(graph, weightThreshold);
//		System.out.println("[" + getClass().getSimpleName() + "] Filtered merge conflict graph: \n" + filteredGraph.toString());
		System.out.println("[" + getClass().getSimpleName() + "] Number of filtered conflicting activities: " + filteredGraph.nofNodes());
		System.out.println("[" + getClass().getSimpleName() + "] Number of filtered conflicting activity relations: " + filteredGraph.nofEdges());
		
		// add all the nodes as activities to recompose
		Set<XEventClass> recomposeActivities = new HashSet<>();
		
		for (String node: filteredGraph.getNodes()) {
			for (XEventClass candidate: conflictingActivities) {
				if (candidate.getId().equals(node))
					recomposeActivities.add(candidate);
			}
		}
		
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing on " + recomposeActivities.size() + " activities: " + recomposeActivities);
		
		recomposeActivities.addAll(info.getUnsplittables());
		
		return recomposeActivities;
	}

	public String getName() {
		return NAME;
	}

}
