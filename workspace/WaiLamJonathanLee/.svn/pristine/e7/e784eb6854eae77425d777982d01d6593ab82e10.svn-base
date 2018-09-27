package org.processmining.decomposedreplayer.models.recompose;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public class MergeConflictSetGraph {
	
	private List<ConflictSetEdge> edges;
	private Set<Set<XEventClass>> nodes;
	
	public MergeConflictSetGraph(Set<ConflictSetEdge> edges) {
		this.edges = new LinkedList<>(edges);
		
		this.edges.sort(new Comparator<ConflictSetEdge>() {

			public int compare(ConflictSetEdge o1, ConflictSetEdge o2) {
				return o1.compareTo(o2);
			}
		});
		
		nodes = new HashSet<>();
		for (ConflictSetEdge edge: edges) {
			Set<XEventClass> set0 = edge.either();
			Set<XEventClass> set1 = edge.other(set0);
			nodes.add(set0);
			nodes.add(set1);
		}
	}
	
	public Set<ConflictSetEdge> getEdges() {
		return new HashSet<>(edges);
	}
	
	public ConflictSetEdge removeWeakestEdge() {
		ConflictSetEdge removed = edges.remove(0);
		nodes.remove(removed.either());
		nodes.remove(removed.other(removed.either()));
		return removed;
	}
	
	public Set<Set<XEventClass>> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Set<XEventClass>> nodes) {
		this.nodes = nodes;
	}
	
	public int nofNodes() {
		return nodes.size();
	}
	
	public int nofEdges() {
		return edges.size();
	}
	
	public String getStatString() {
		return "No. of nodes: " + nofNodes() + "\n" + 
				"No. of edges: " + nofEdges();
	}
	
	@Override
	public String toString() {
		String string = "";
		for (ConflictSetEdge edge: edges) {
			if (string.equals("")) {
				String edgeString = edge.toString();
				String edgeWeight = edge.getWeight() + "";
				string = edgeString + ", " + edgeWeight;
			}
			else {
				String edgeString = edge.toString();
				String edgeWeight = edge.getWeight() + "";
				string += ("\n" + edgeString + ", " + edgeWeight);
			}
		}
		return string;
	}
	
}
