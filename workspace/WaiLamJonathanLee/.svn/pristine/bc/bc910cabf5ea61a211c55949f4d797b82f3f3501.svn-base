package org.processmining.decomposedreplayer.algorithms.recompose;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.decomposedreplayer.models.recompose.ConflictSetEdge;
import org.processmining.decomposedreplayer.models.recompose.MergeConflictSetGraph;
import org.processmining.decomposedreplayer.models.utils.Weighted;
import org.processmining.decomposedreplayer.models.utils.WeightedConflictSet;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public abstract class TopKMergeConflictSetGraphRecomposeStrategy implements RecomposeStrategy {

	public static final String NAME = "Merge conflict set graph";
//	public static final int S = 20;
//	public static final int K = 5;
	
	private ConflictUtils conflictUtils = new ConflictUtils();
	private MergeConflictSetGraph graph;
		
	public Set<XEventClass> recompose(RecomposeInformation info) {
		Set<XEventClass> toRecompose = new HashSet<>();
		Set<SyncReplayResult> openAlignments = info.getOpenAlignments();
		
		System.out.println("[" + getClass().getSimpleName() + "] Constructing merge conflict set graph...");
		
		Set<Set<XEventClass>> conflictSetSets = new HashSet<>();
		
		for (SyncReplayResult traceAlignment: openAlignments) {
			Map<XEventClass, Double> traceConflicts = conflictUtils.getConflicts(traceAlignment);
			Set<XEventClass> conflictSet = new HashSet<>(traceConflicts.keySet());
			conflictSetSets.add(conflictSet);
		}
		
		// you need a minimum k nodes to form k edges
		if (conflictSetSets.size() <= getK()) {
			// add all to recompose
			System.out.println("[" + getClass().getSimpleName() + "] Number of conflict sets is less than " + getK());
			for (Set<XEventClass> conflictSet: conflictSetSets)
				toRecompose.addAll(conflictSet);
			System.out.println("[" + getClass().getSimpleName() + "] Recomposing on " + toRecompose.size() + 
					" activities: " + toRecompose);
			toRecompose.addAll(info.getUnsplittables());
			return toRecompose;
		}
		
		List<Weighted> conflictSetList = new LinkedList<>();
		for (Set<XEventClass> conflictSet: conflictSetSets) {
			WeightedConflictSet weighted = new WeightedConflictSet(conflictSet);
			conflictSetList.add(weighted);
		}
		
		int maxNofNodes = conflictSetList.size();
		
		if (conflictSetList.size() > (getK() * getS())) {
			maxNofNodes = (getK() * getS());
//			WeightedShuffle.weightedShuffle(conflictSetList, 123, true);
			conflictSetList.sort(new Comparator<Weighted>() {

				public int compare(Weighted o1, Weighted o2) {
					if (o1.getWeight() < o2.getWeight())
						return -1;
					else if (o1.getWeight() == o2.getWeight())
						return 0;
					else 
						return 1;
				}
			});
		}

		Set<ConflictSetEdge> edges = new HashSet<>();
		
		for (int i = 0; i < maxNofNodes - 1; i++) {
			Set<XEventClass> set0 = ((WeightedConflictSet) conflictSetList.get(i)).getConflictSet();
			for (int j = i + 1; j < maxNofNodes; j++) {
				Set<XEventClass> set1 = ((WeightedConflictSet) conflictSetList.get(j)).getConflictSet();
				// the conflict sets are nodes
				ConflictSetEdge edge = new ConflictSetEdge(set0, set1);
				edges.add(edge);
			}
		}
		
		graph = new MergeConflictSetGraph(edges);
		
		System.out.println("[" + getClass().getSimpleName() + "] Merge conflict set graph: \n" + graph.getStatString());
		
		// remove edges until number of edge reaches k
		int nofRemoved = 0;
		while(graph.nofEdges() > getK()) {
			ConflictSetEdge removed = graph.removeWeakestEdge();
			nofRemoved += 1;
//			System.out.println("[" + getClass().getSimpleName() + "] Removed edge: " + removed);
		}
		
		System.out.println("[" + getClass().getSimpleName() + "] Removed " + nofRemoved + " edges.");
		System.out.println("[" + getClass().getSimpleName() + "] Trimmed merge conflict set graph: \n" + graph.toString());

		for (ConflictSetEdge edge: graph.getEdges()) {
			Set<XEventClass> set0 = edge.either();
			Set<XEventClass> set1 = edge.other(set0);
			toRecompose.addAll(set0);
			toRecompose.addAll(set1);
		}
				
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing on " + toRecompose.size() + 
				" activities: " + toRecompose);
		
		toRecompose.addAll(info.getUnsplittables());
		
		return toRecompose;
	}

	public abstract int getK();
	
	public abstract int getS();
}
