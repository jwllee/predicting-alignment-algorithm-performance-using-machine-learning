package org.processmining.logalignment.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.TraceAlignment;
import org.processmining.logalignment.parameters.ConvertLogAlignmentToReplayResultParameters;
import org.processmining.logalignment.utils.AlignmentInfoLabels;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import nl.tue.alignment.Replayer;

public class ConvertLogAlignmentToReplayResultAlgorithm {

	public final static String CONFLICTPREFIX = " conflicts on ";
	
	public PNRepResult apply(PluginContext context, LogAlignment logAlignment,
			AcceptingPetriNet net, XLog log, ConvertLogAlignmentToReplayResultParameters parameters) {
		Set<SyncReplayResult> results = new HashSet<SyncReplayResult>();
		XEventClassifier classifier = parameters.getClassifier();
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		Map<String, Transition> transitionMap = new HashMap<String, Transition>();
		for (Transition transition : net.getNet().getTransitions()) {
			transitionMap.put(transition.getLabel(), transition);
		}
		int traceIndex = 0;
		Map<List<XEventClass>, Set<Integer>> map = new HashMap<List<XEventClass>, Set<Integer>>();
		for (XTrace trace : log) {
			List<XEventClass> activities = new ArrayList<XEventClass>();
			for (XEvent event : trace) {
				activities.add(info.getEventClasses().getClassOf(event));
			}
			if (map.keySet().contains(activities)) {
				map.get(activities).add(traceIndex);
			} else {
				List<Object> nodeInstances = new ArrayList<Object>();
				List<StepTypes> stepTypes = new ArrayList<StepTypes>();
				TraceAlignment traceAlignment = logAlignment.getAlignment(activities);
				if (traceAlignment != null) {
					for (int i = 0; i < traceAlignment.getSize(); i++) {
						StepTypes stepType = traceAlignment.getLegalMoves().get(i).getFirst();
						stepTypes.add(i, stepType);
						if (stepType == StepTypes.LMGOOD || stepType == StepTypes.MREAL || stepType == StepTypes.MINVI) {
							nodeInstances.add(i,
									transitionMap.get(traceAlignment.getLegalMoves().get(i).getSecond().getSecond()));
						} else {
							nodeInstances.add(i, traceAlignment.getLegalMoves().get(i).getSecond().getFirst());
						}
					}
					SyncReplayResult result = new SyncReplayResult(nodeInstances, stepTypes, traceIndex);
					result.setReliable(traceAlignment.isReliable());
					// Add some info, otherwise the visualization chokes on the fact that there is no info.
					result.addInfo(PNRepResult.RAWFITNESSCOST, traceAlignment.getCosts());
					result.addInfo(PNRepResult.MAXMOVELOGCOST, traceAlignment.getMaxCosts());
					result.addInfo(PNRepResult.TIME, traceAlignment.getMillis());
					if (traceAlignment.getDecomposedSyncMoveNumerator() >= 0.0) {
						result.addInfo(AlignmentInfoLabels.DECOMPOSEDSYNCMOVENUMERATOR, traceAlignment.getDecomposedSyncMoveNumerator());
					}
					if (traceAlignment.getDecomposedSyncMoveDenominator() >= 0.0) {
						result.addInfo(AlignmentInfoLabels.DECOMPOSEDSYNCMOVEDENOMINATOR, traceAlignment.getDecomposedSyncMoveDenominator());
					}

					// add state info to result
					if (traceAlignment.getStateCount() >= 0.0) {
						result.addInfo(PNRepResult.NUMSTATEGENERATED, traceAlignment.getStateCount());
					}
					if (traceAlignment.getQueuedStates() >= 0.0) {
						result.addInfo(PNRepResult.QUEUEDSTATE, traceAlignment.getQueuedStates());
					}
					if (traceAlignment.getTraversedArcs() >= 0.0) {
						result.addInfo(PNRepResult.TRAVERSEDARCS, traceAlignment.getTraversedArcs());
					}
					result.addInfo(Replayer.TRACEEXITCODE, (double) traceAlignment.getTraceExitCode());
					
					/*
					 * Convert the information on conflicting activities.
					 */
					for (XEventClass activity : traceAlignment.getConflictingActivities().keySet()) {
						result.addInfo(CONFLICTPREFIX + activity, traceAlignment.getConflictingActivities().get(activity));
					}
//					for (XEventClass activity : activities) {
//						result.addInfo(CONFLICTPREFIX + activity, 
//								(traceAlignment.getConflictingActivities().keySet().contains(activity) 
//										? traceAlignment.getConflictingActivities().get(activity) : 0.0));
//					}
					map.put(activities, result.getTraceIndex());
					results.add(result);
				}
			}
			traceIndex++;
		}
		PNRepResult replayResult = new PNRepResultImpl(results);
		context.getProvidedObjectManager().createProvidedObject("DaC net", net.getNet(), PetrinetGraph.class, context);
		IPNReplayAlgorithm replayer = new PetrinetReplayerWithILP();
		Set<XEventClass> activities = new HashSet<XEventClass>();
		activities.addAll(info.getEventClasses().getClasses());
		activities.add(XUtils.MOVEONMODELACTIVITY);
		IPNReplayParameter replayerParameters = createReplayParameters(activities, XUtils.INVISIBLEACTIVITY, net);
		context.addConnection(new PNRepResultAllRequiredParamConnection("DaC alignment", net.getNet(), log, parameters
				.getMapping(), replayer, replayerParameters, replayResult));
		return replayResult;
	}

	public IPNReplayParameter createReplayParameters(Collection<XEventClass> activities, XEventClass invisibleActivity,
			AcceptingPetriNet net) {
		IPNReplayParameter parameters = new CostBasedCompleteParam(activities, invisibleActivity, net.getNet().getTransitions());
		parameters.setInitialMarking(net.getInitialMarking());
		Set<Marking> finalMarkings = net.getFinalMarkings();
		if (finalMarkings.isEmpty()) {
			finalMarkings = new HashSet<Marking>();
			finalMarkings.add(new Marking());
		}
		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
		return parameters;
	}
}
