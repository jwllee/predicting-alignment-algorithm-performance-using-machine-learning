package org.processmining.decomposedreplayer.configurations;

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

import com.fluxicon.slickerbox.components.NiceSlider;

public interface DecomposedReplayConfiguration {

	public PNRepResult apply(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters, ActivityClusterArray clusters);

	public CausalActivityMatrix getMatrix(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters);

	public CausalActivityMatrix getMatrixNet(PluginContext context, AcceptingPetriNet net,
			DecomposedReplayParameters parameters);

	public CausalActivityMatrix getMatrixLog(PluginContext context, XLog log, DecomposedReplayParameters parameters);

	public CausalActivityGraph getGraph(PluginContext context, CausalActivityMatrix matrix,
			DecomposedReplayParameters parameters);

	public ActivityClusterArray getClusters(PluginContext context, CausalActivityGraph graph,
			DecomposedReplayParameters parameters);
	
	public ReplayCostFactor getFactor(PluginContext context, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters);
	
	public EventLogArray getLogs(PluginContext context, XLog log, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters);

	public AcceptingPetriNetArray getNets(PluginContext context, AcceptingPetriNet net, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters, String strategy);

	public ReplayResultArray getResults(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor factor, ActivityClusterArray clusters, DecomposedReplayParameters parameters);

	public LogAlignmentArray getAlignments(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray results, ReplayCostFactor factor, DecomposedReplayParameters parameters);

	public LogAlignmentArray getFilteredAlignments(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, DecomposedReplayParameters parameters);

	public LogAlignment getAlignment(PluginContext context, LogAlignmentArray alignments, XLog log,
			AcceptingPetriNetArray nets, DecomposedReplayParameters parameters);

	public PNRepResult getResult(PluginContext context, LogAlignment alignment, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters);

	public String getName();
	
	public void update(NiceSlider slider);
}
