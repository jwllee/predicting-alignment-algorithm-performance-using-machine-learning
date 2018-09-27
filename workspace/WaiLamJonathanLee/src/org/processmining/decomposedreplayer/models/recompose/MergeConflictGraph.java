package org.processmining.decomposedreplayer.models.recompose;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MergeConflictGraph {
	
	public static final String DUMMY = "dummy";

	private Map<Edge, Double> edgeWeightMap;
	private Set<String> nodes;
	
	public MergeConflictGraph() {
		edgeWeightMap = new HashMap<>();
		nodes = new HashSet<>();
	}
	
	public void addEdge(Edge edge, double weight) {
		if (edgeWeightMap.containsKey(edge)) {
			edgeWeightMap.put(edge, edgeWeightMap.get(edge) + weight);
		} else {
			String u = edge.either();
			String v = edge.other(u);
			nodes.add(u);
			nodes.add(v);
			edgeWeightMap.put(edge, weight);
		}
	}
	
	public Map<Edge, Double> getEdgeWeightMap() {
		return edgeWeightMap;
	}

	public Set<Edge> getEdges() {
		return edgeWeightMap.keySet();
	}

	public Set<String> getNodes() {
		return nodes;
	}

	public void setNodes(Set<String> nodes) {
		this.nodes = nodes;
	}
	
	public int nofNodes() {
		return nodes.size();
	}
	
	public int nofEdges() {
		return edgeWeightMap.size();
	}
	
	@Override
	public String toString() {
		String string = "";
		for (Edge edge: edgeWeightMap.keySet()) {
			if (string.equals("")) {
				String edgeString = edge.toString();
				String edgeWeight = edgeWeightMap.get(edge).toString();
				string = edgeString + ", " + edgeWeight;
			}
			else {
				String edgeString = edge.toString();
				String edgeWeight = edgeWeightMap.get(edge).toString();
				string += ("\n" + edgeString + ", " + edgeWeight);
			}
		}
		return string;
	}
	
}
