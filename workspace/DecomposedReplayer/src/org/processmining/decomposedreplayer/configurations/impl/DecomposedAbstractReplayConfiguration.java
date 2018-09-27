package org.processmining.decomposedreplayer.configurations.impl;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinetdecomposer.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;
import org.processmining.acceptingpetrinetdecomposer.plugins.DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionReplaceNoReduceStrategy;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.activityclusterarraycreator.parameters.ModifyActivityClusterArrayParameters;
import org.processmining.activityclusterarraycreator.plugins.ConvertCausalActivityGraphToActivityClusterArrayPlugin;
import org.processmining.activityclusterarraycreator.plugins.ModifyActivityClusterArrayPlugin;
import org.processmining.activityclusterarrayextractor.parameters.ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters;
import org.processmining.activityclusterarrayextractor.plugins.ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.causalactivitygraphcreator.plugins.ConvertCausalActivityMatrixToCausalActivityGraphPlugin;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixcreator.creators.impl.MatrixPNCreator;
import org.processmining.causalactivitymatrixcreator.parameters.CreateFromAcceptingPetriNetParameters;
import org.processmining.causalactivitymatrixcreator.plugins.CreateFromAcceptingPetriNetPlugin;
import org.processmining.causalactivitymatrixminer.miners.impl.HAFMidiMatrixMiner;
import org.processmining.causalactivitymatrixminer.parameters.DiscoverFromEventLogParameters;
import org.processmining.causalactivitymatrixminer.plugins.DiscoverFromEventLogPlugin;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.ConvertLogAlignmentToReplayResultParameters;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.logalignment.parameters.CreateReplayCostFactorFromActivityClusterArrayParameters;
import org.processmining.logalignment.parameters.FilterLogAlignmentParameters;
import org.processmining.logalignment.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;
import org.processmining.logalignment.plugins.ConvertLogAlignmentToReplayResultPlugin;
import org.processmining.logalignment.plugins.ConvertReplayResultToLogAlignmentPlugin;
import org.processmining.logalignment.plugins.CreateReplayCostFactorFromActivityClusterArrayPlugin;
import org.processmining.logalignment.plugins.FilterLogAlignmentPlugin;
import org.processmining.logalignment.plugins.MergeLogAlignmentArrayIntoLogAlignmentPlugin;
import org.processmining.logalignment.plugins.ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin;
import org.processmining.logdecomposer.filters.impl.DecompositionInFilter;
import org.processmining.logdecomposer.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;
import org.processmining.logdecomposer.plugins.DecomposeEventLogUsingActivityClusterArrayPlugin;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public abstract class DecomposedAbstractReplayConfiguration implements DecomposedReplayConfiguration {

	protected String decompositionStrategy;

	public DecomposedAbstractReplayConfiguration() {
		this.decompositionStrategy = DecompositionReplaceNoReduceStrategy.NAME;
	}

	public DecomposedAbstractReplayConfiguration(String decompositionStrategy) {
		this.decompositionStrategy = decompositionStrategy;
	}


	public CausalActivityMatrix getMatrix(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		return parameters.isDiscoverMatrixFromNet() ? getMatrixNet(context, net, parameters)
				: getMatrixLog(context, log, parameters);
	}

	public CausalActivityMatrix getMatrixNet(PluginContext context, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		CreateFromAcceptingPetriNetPlugin plugin = new CreateFromAcceptingPetriNetPlugin();
		CreateFromAcceptingPetriNetParameters params = new CreateFromAcceptingPetriNetParameters(net);
		params.setClassifier(parameters.getClassifier());
		params.setCreator(MatrixPNCreator.NAME);
		return plugin.run(context, net, params);
	}

	public CausalActivityMatrix getMatrixLog(PluginContext context, XLog log, DecomposedReplayParameters parameters) {
		DiscoverFromEventLogPlugin plugin = new DiscoverFromEventLogPlugin();
		DiscoverFromEventLogParameters params = new DiscoverFromEventLogParameters(log);
		params.setClassifier(parameters.getClassifier());
		params.setMiner(new HAFMidiMatrixMiner().getName());
		return plugin.run(context, log, params);
	}

	public CausalActivityGraph getGraph(PluginContext context, CausalActivityMatrix matrix,
			DecomposedReplayParameters parameters) {
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin plugin = new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters params = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		params.setZeroValue(0.5);
		params.setConcurrencyRatio(0.0);
		//		params.setIncludeThreshold(0.005);
		return plugin.run(context, matrix, params);
	}

	public ActivityClusterArray getClusters(PluginContext context, CausalActivityGraph graph,
			DecomposedReplayParameters parameters) {
		ConvertCausalActivityGraphToActivityClusterArrayPlugin plugin = new ConvertCausalActivityGraphToActivityClusterArrayPlugin();
		ConvertCausalActivityGraphToActivityClusterArrayParameters params = new ConvertCausalActivityGraphToActivityClusterArrayParameters(
				graph);
		params.setCheckBackwardArc(false);
		params.setIncludeAll(true);
		return plugin.run(context, graph, params);
	}

	public ActivityClusterArray getClusters(PluginContext context, AcceptingPetriNetArray nets, CausalActivityMatrix matrix, 
			DecomposedReplayParameters parameters) {
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin plugin = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin();
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters params = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters(nets, new HashSet<XEventClass>(matrix.getActivities()));
		params.setMapping(parameters.getMapping());
		params.setIOAware(true);
		return plugin.run(context, nets, params);
	}

	public ActivityClusterArray getModifiedClusters(PluginContext context, CausalActivityGraph graph, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		ModifyActivityClusterArrayPlugin plugin = new ModifyActivityClusterArrayPlugin();
		ModifyActivityClusterArrayParameters params = new ModifyActivityClusterArrayParameters(clusters);
		params.setNrOfClusters((clusters.getClusters().size() * parameters.getPercentage()) / 100);
		return plugin.run(context, clusters, graph, params);
	}
	
	public ReplayCostFactor getFactor(PluginContext context, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		CreateReplayCostFactorFromActivityClusterArrayPlugin plugin = new CreateReplayCostFactorFromActivityClusterArrayPlugin();
		CreateReplayCostFactorFromActivityClusterArrayParameters params = new CreateReplayCostFactorFromActivityClusterArrayParameters();
		return plugin.run(context, clusters, params);
	}

	public EventLogArray getLogs(PluginContext context, XLog log, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		DecomposeEventLogUsingActivityClusterArrayPlugin plugin = new DecomposeEventLogUsingActivityClusterArrayPlugin();
		DecomposeEventLogUsingActivityClusterArrayParameters params = new DecomposeEventLogUsingActivityClusterArrayParameters(
				log);
		params.setClassifier(parameters.getClassifier());
		params.setRemoveEmptyTraces(false);
		params.setFilter(DecompositionInFilter.NAME);
		params.setAddStartEndEvents(false);
		return plugin.run(context, log, clusters, params);
	}

	public AcceptingPetriNetArray getNets(PluginContext context, AcceptingPetriNet net, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters, String strategy) {
		DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin plugin = new DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin();
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters params = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(
				net, clusters, parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		params.setIncludeClusterTransitions(true);
		params.setStrategy(strategy);
		params.setPercentage(parameters.getPercentage());
		params.setUnsplittableActivities(new HashSet<XEventClass>(parameters.getUnsplittableActivities()));
		return plugin.run(context, net, clusters, params);
	}

	public ReplayResultArray getResults(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor factor, ActivityClusterArray clusters, DecomposedReplayParameters parameters) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin plugin = new ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin();
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters params = new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(
				logs, nets);
		params.setClassifier(parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		params.setMoveOnLogCosts(parameters.getMoveOnLogCosts());
		params.setMoveOnModelCosts(parameters.getMoveOnModelCosts());
		params.setDeadline(parameters.getDeadline());
		if (parameters.isPreferBorderTransitions()) {
			return plugin.run(context, logs, nets, clusters, factor, params);
		}
		return plugin.run(context, logs, nets, factor, params);
	}

	public LogAlignmentArray getAlignments(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray results, ReplayCostFactor factor, DecomposedReplayParameters parameters) {
		ConvertReplayResultToLogAlignmentPlugin plugin = new ConvertReplayResultToLogAlignmentPlugin();
		ConvertReplayResultToLogAlignmentParameters params = new ConvertReplayResultToLogAlignmentParameters(logs,
				nets);
		params.setClassifier(parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		return plugin.run(context, logs, nets, results, factor, params);
	}

	public LogAlignmentArray getFilteredAlignments(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, DecomposedReplayParameters parameters) {
		FilterLogAlignmentPlugin plugin = new FilterLogAlignmentPlugin();
		FilterLogAlignmentParameters params = new FilterLogAlignmentParameters();
		return plugin.run(context, alignments, nets, params);
	}

	public LogAlignment getAlignment(PluginContext context, LogAlignmentArray alignments, XLog log,
			AcceptingPetriNetArray nets, DecomposedReplayParameters parameters) {
		MergeLogAlignmentArrayIntoLogAlignmentPlugin plugin = new MergeLogAlignmentArrayIntoLogAlignmentPlugin();
		MergeLogAlignmentArrayIntoLogAlignmentParameters params = new MergeLogAlignmentArrayIntoLogAlignmentParameters(
				log, alignments);
		params.setClassifier(parameters.getClassifier());
		params.setPseudoUnreliable(true);
		params.setAddConflictOnlyOnce(parameters.isAddConflictOnlyOnce());
		return plugin.run(context, log, alignments, nets, params);
	}

	public PNRepResult getResult(PluginContext context, LogAlignment alignment, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		ConvertLogAlignmentToReplayResultPlugin plugin = new ConvertLogAlignmentToReplayResultPlugin();
		ConvertLogAlignmentToReplayResultParameters params = new ConvertLogAlignmentToReplayResultParameters(log, net);
		params.setClassifier(parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		return plugin.run(context, alignment, net, log, params);
	}

	protected ActivityClusterArray combineClusters(ActivityClusterArray clusters) {
		ActivityClusterArray combinedClusters = ActivityClusterArrayFactory.createActivityClusterArray();
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			activities.addAll(cluster);
		}
		combinedClusters.init("", activities);
		combinedClusters.addCluster(activities);
		ActivityClusterArray results = combinedClusters;
		return results;
	}
}
