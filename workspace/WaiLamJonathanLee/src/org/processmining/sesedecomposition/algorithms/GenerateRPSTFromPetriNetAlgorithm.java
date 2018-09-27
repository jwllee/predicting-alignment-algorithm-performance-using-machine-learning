package org.processmining.sesedecomposition.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.petri.Flow;
import org.jbpt.petri.Node;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPST;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.parameters.GenerateRPSTFromPetriNetParameters;
import org.processmining.sesedecomposition.utils.petrinet.PetriNetUtils;
import org.processmining.sesedecomposition.utils.rpst.ProMJBPTPetriNet;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class GenerateRPSTFromPetriNetAlgorithm {

	private AcceptingPetriNet clonedNet;
	
	private DirectedGraph<PetriNetRPSTNode, String> tree;
	
	private ProMJBPTPetriNet multiNet;
	
	private RPST<Flow,Node> rpstJBPT;
		
	public PetriNetRPST apply(PluginContext context, AcceptingPetriNet apn, 
			GenerateRPSTFromPetriNetParameters parameters) {
		Map<DirectedGraphElement, DirectedGraphElement> map = new HashMap<DirectedGraphElement, DirectedGraphElement>();
		this.clonedNet = PetriNetUtils.cloneNet(apn, map);
		this.tree = new DirectedSparseGraph<PetriNetRPSTNode, String>();
		this.multiNet = new ProMJBPTPetriNet(this.clonedNet.getNet());
		this.rpstJBPT = generateRPST(multiNet);
		
		PetriNetRPSTNode root = makeRootNode();
		this.tree.addVertex(root);
		
		addAllRPSTComponentToTree(rpstJBPT.getRoot(), root);
		
		PetriNetRPST rpst = new PetriNetRPST(parameters.getName(), 
				clonedNet, tree, root);
		return rpst;
	}
	
	private PetriNetRPSTNode makeRootNode() {
		IRPSTNode<Flow, Node> rootJBPT = rpstJBPT.getRoot();
		PetriNetRPSTNode root = createRPSTNode(rootJBPT, multiNet);
		return root;
	}
	
	private RPST<Flow,Node> generateRPST(ProMJBPTPetriNet multiNet) {
		RPST<Flow,Node> rpstJBPT = new RPST<Flow,Node>(multiNet.getJbpt());
		return rpstJBPT;
	}
	
	private void addAllRPSTComponentToTree(IRPSTNode<Flow, Node> rootJBPT, PetriNetRPSTNode root) {
		//Preparation for exploring the RPST Tree
		Queue<IRPSTNode<Flow, Node>> toExploreJBPT = new LinkedList<IRPSTNode<Flow, Node>>();
		toExploreJBPT.add(rootJBPT);
		Queue<PetriNetRPSTNode> toExploreRPST = new LinkedList<PetriNetRPSTNode>();
		toExploreRPST.add(root);
		
		while(!toExploreRPST.isEmpty()){
			IRPSTNode<Flow, Node> currJBPT = toExploreJBPT.poll();
			PetriNetRPSTNode curr = toExploreRPST.poll();
			
			Collection<IRPSTNode<Flow, Node>> childrenJBPT = rpstJBPT.getChildren(currJBPT);
			for(IRPSTNode<Flow, Node> childJBPT : childrenJBPT){
				PetriNetRPSTNode child = createRPSTNode(childJBPT, multiNet);
				addNodeToTree(child, curr);
				
				toExploreJBPT.add(childJBPT);
				toExploreRPST.add(child);
			}
		}
	}
	
	private void addNodeToTree(PetriNetRPSTNode child, PetriNetRPSTNode parent) {
		this.tree.addVertex(child);
		this.tree.addEdge(parent.getId() + "=>" + child.getId(), parent, child);
	}
	
	private PetriNetRPSTNode createRPSTNode(IRPSTNode<Flow, Node> nodeJBPT, ProMJBPTPetriNet multiNet) {
		//Arcs
		Set<Arc> arcs = new HashSet<Arc>();
		Set<Node> nodes = new HashSet<Node>();
		for(Flow flow: nodeJBPT.getFragment()){
			arcs.add(multiNet.jbpt2PromArc(flow));
			nodes.add(flow.getSource());
			nodes.add(flow.getTarget());
		}
		
		//Places and Transitions
		Set<Transition> trans = new HashSet<Transition>();
		Set<Place> places = new HashSet<Place>();
		for(Node node: nodes){
			if(multiNet.jbpt2PromNode(node) instanceof Transition) trans.add((Transition) multiNet.jbpt2PromNode(node));
			else if(multiNet.jbpt2PromNode(node) instanceof Place) places.add((Place) multiNet.jbpt2PromNode(node));
		}
		
		//Build the node
		return new PetriNetRPSTNode(nodeJBPT.getId(), nodeJBPT.getName(), nodeJBPT.getDescription(), 
				trans, places, arcs, multiNet.jbpt2PromNode(nodeJBPT.getEntry()), multiNet.jbpt2PromNode(nodeJBPT.getExit()));
	}
}
