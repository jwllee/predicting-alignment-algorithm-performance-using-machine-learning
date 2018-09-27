package org.processmining.decomposedreplayer.configurations.impl;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public abstract class DecomposedAbstractMatrixFilterReplayConfiguration extends DecomposedAbstractReplayConfiguration {

	public abstract String getStrategy();

	public PNRepResult apply(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters, ActivityClusterArray clusters) {
		long msecs;
		AcceptingPetriNetArray nets = null;
		if (clusters == null) {
			
			msecs = -System.currentTimeMillis();
			CausalActivityMatrix matrix = getMatrix(context, log, net, parameters);
			context.getProvidedObjectManager().createProvidedObject("Matrix", matrix, CausalActivityMatrix.class,
					context);
			msecs += System.currentTimeMillis();
			parameters.displayMessage("[DecomposedReplayPlugin] Creating matrix took " + msecs + " milliseconds.");

			msecs = -System.currentTimeMillis();
			CausalActivityGraph graph = getGraph(context, matrix, parameters);
			context.getProvidedObjectManager().createProvidedObject("Graph", graph, CausalActivityGraph.class, context);
			msecs += System.currentTimeMillis();
			parameters.displayMessage("[DecomposedReplayPlugin] Creating graph took " + msecs + " milliseconds.");

			msecs = -System.currentTimeMillis();
			clusters = getClusters(context, graph, parameters);
			if (parameters.isCombineClusters()) {
				clusters = combineClusters(clusters);
			}
			context.getProvidedObjectManager().createProvidedObject("Clusters", clusters, ActivityClusterArray.class,
					context);
			msecs += System.currentTimeMillis();
			parameters.displayMessage("[DecomposedReplayPlugin] Creating " + clusters.getClusters().size()
					+ " clusters took " + msecs + " milliseconds.");

		} else {
			parameters.displayMessage("[DecomposedReplayPlugin] Found " + clusters.getClusters().size() + " clusters.");
		}

		msecs = -System.currentTimeMillis();
		ReplayCostFactor factor = getFactor(context, clusters, parameters);
		context.getProvidedObjectManager().createProvidedObject("Factor", factor, ReplayCostFactor.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Creating factor took " + msecs + " milliseconds.");

		msecs = -System.currentTimeMillis();
		EventLogArray logs = getLogs(context, log, clusters, parameters);
		context.getProvidedObjectManager().createProvidedObject("Logs", logs, EventLogArray.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Decomposing log took " + msecs + " milliseconds.");

		msecs = -System.currentTimeMillis();
		AcceptingPetriNetArray filterNets = (nets != null ? nets : getNets(context, net, clusters, parameters,
				getStrategy()));
		context.getProvidedObjectManager().createProvidedObject("Nets (filter)", filterNets,
				AcceptingPetriNetArray.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage(
				"[DecomposedReplayPlugin] Decomposing net by filtering took " + msecs + " milliseconds.");

		msecs = -System.currentTimeMillis();
		ReplayResultArray results = getResults(context, logs, filterNets, factor, clusters, parameters);
		context.getProvidedObjectManager().createProvidedObject("Results", results, ReplayResultArray.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Decomposed replay took " + msecs + " milliseconds.");

		msecs = -System.currentTimeMillis();
		LogAlignmentArray alignments = getAlignments(context, logs, filterNets, results, factor, parameters);
		context.getProvidedObjectManager().createProvidedObject("Alignments", alignments, LogAlignmentArray.class,
				context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Creating alignments took " + msecs + " milliseconds.");

		msecs = -System.currentTimeMillis();
		LogAlignment alignment = getAlignment(context, alignments, log, filterNets, parameters);
		context.getProvidedObjectManager().createProvidedObject("Alignment", alignment, LogAlignment.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Merging alignments took " + msecs + " milliseconds.");

		msecs = -System.currentTimeMillis();
		PNRepResult result = getResult(context, alignment, log, net, parameters);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Creating replay took " + msecs + " milliseconds.");
		return result;
	}
}
