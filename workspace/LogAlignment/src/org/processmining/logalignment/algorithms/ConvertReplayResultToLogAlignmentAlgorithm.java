package org.processmining.logalignment.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.models.TraceAlignment;
import org.processmining.logalignment.models.impl.LogAlignmentArrayFactory;
import org.processmining.logalignment.models.impl.LogAlignmentFactory;
import org.processmining.logalignment.models.impl.TraceAlignmentFactory;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.logalignment.utils.AlignmentInfoLabels;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ConvertReplayResultToLogAlignmentAlgorithm {

	public LogAlignmentArray apply(EventLogArray logs, AcceptingPetriNetArray nets, ReplayResultArray replayResults,
			ActivityClusterArray clusters, ReplayCostFactor factor,
			ConvertReplayResultToLogAlignmentParameters parameters) {
		LogAlignmentArray logAlignments = LogAlignmentArrayFactory.createLogAlignmentArray();
		logAlignments.init();
		for (int index = 0; index < replayResults.getSize(); index++) {
			XEventClass invisibleActivity = XUtils.INVISIBLEACTIVITY;
			TransEvClassMapping mapping = new TransEvClassMapping(parameters.getClassifier(), invisibleActivity);
			Map<String, XEventClass> activities = new HashMap<String, XEventClass>();
			for (Transition transition : parameters.getMapping().keySet()) {
				if (!parameters.getMapping().get(transition).equals(invisibleActivity)) {
					activities.put(transition.getLabel(), parameters.getMapping().get(transition));
				}
			}
			for (int i = 0; i < nets.getSize(); i++) {
				for (Transition transition : nets.getNet(i).getNet().getTransitions()) {
					mapping.put(transition, activities.get(transition.getLabel()));
				}
			}
			logAlignments.addAlignment(apply(replayResults.getReplay(index), mapping, clusters.getCluster(index),
					factor));
		}
		return logAlignments;
	}

	private LogAlignment apply(PNRepResult replayResults, TransEvClassMapping tecMap, Set<XEventClass> cluster,
			ReplayCostFactor factor) {
		LogAlignment logAlignment = LogAlignmentFactory.createLogAlignment();
		logAlignment.init();
		for (SyncReplayResult replayResult : replayResults) {
			TraceAlignment traceAlignment = apply(replayResult, tecMap, factor);
			logAlignment.putAlignment(traceAlignment.getLogMoves(), traceAlignment);
			logAlignment.setCluster(cluster);
		}
		return logAlignment;
	}

	private TraceAlignment apply(SyncReplayResult replayResult, TransEvClassMapping tecMap, ReplayCostFactor factor) {
		TraceAlignment traceAlignment = TraceAlignmentFactory.createTraceAlignment();
		traceAlignment.init();
		if (replayResult.getInfo().containsKey(PNRepResult.RAWFITNESSCOST)) {
			traceAlignment.setCosts(replayResult.getInfo().get(PNRepResult.RAWFITNESSCOST)
					/ (factor != null ? factor.getCostBase() : 1.0));
		}
		if (replayResult.getInfo().containsKey(PNRepResult.MAXMOVELOGCOST)) {
			traceAlignment.setMaxCosts(replayResult.getInfo().get(PNRepResult.MAXMOVELOGCOST)
					/ (factor != null ? factor.getCostBase() : 1.0));
		}
		if (replayResult.getInfo().containsKey(AlignmentInfoLabels.DECOMPOSEDSYNCMOVENUMERATOR)) {
			traceAlignment.setDecomposedSyncMoveNumerator(replayResult.getInfo().get(
					AlignmentInfoLabels.DECOMPOSEDSYNCMOVENUMERATOR));
		}
		if (replayResult.getInfo().containsKey(AlignmentInfoLabels.DECOMPOSEDSYNCMOVEDENOMINATOR)) {
			traceAlignment.setDecomposedSyncMoveDenominator(replayResult.getInfo().get(
					AlignmentInfoLabels.DECOMPOSEDSYNCMOVEDENOMINATOR));
		}
		traceAlignment.setReliable(replayResult.isReliable());
		// adding the state info from alignment computation
		if (replayResult.getInfo().containsKey(PNRepResult.NUMSTATEGENERATED)) {
//			System.out.printf("[%s] No. of generated states: %.2f%n", 
//					getClass().getSimpleName(), replayResult.getInfo().get(PNRepResult.NUMSTATEGENERATED));
			traceAlignment.setStateCount(replayResult.getInfo().get(PNRepResult.NUMSTATEGENERATED));
		}
		if (replayResult.getInfo().containsKey(PNRepResult.QUEUEDSTATE)) {
//			System.out.printf("[%s] No. of queued states: %.2f%n", 
//					getClass().getSimpleName(), replayResult.getInfo().get(PNRepResult.QUEUEDSTATE));
			traceAlignment.setQueuedStates(replayResult.getInfo().get(PNRepResult.QUEUEDSTATE));
		}
		if (replayResult.getInfo().containsKey(PNRepResult.TRAVERSEDARCS)) {
//			System.out.printf("[%s] No. of traversed arcs: %.2f%n", 
//					getClass().getSimpleName(), replayResult.getInfo().get(PNRepResult.TRAVERSEDARCS));
			traceAlignment.setTraversedArcs(replayResult.getInfo().get(PNRepResult.TRAVERSEDARCS));
		}
		traceAlignment.setMillis(replayResult.getInfo().get(PNRepResult.TIME));
		//		int size = 0;
		//		for (int i = 0; i < replayResult.getStepTypes().size(); i++) {
		//			StepTypes stepType = replayResult.getStepTypes().get(i);
		//			if (isMove(stepType)) {
		//				size++;
		//			}
		//		}
		Map<String, XEventClass> activities = new HashMap<String, XEventClass>();
		for (Transition transition : tecMap.keySet()) {
			activities.put(transition.getLabel(), tecMap.get(transition));
		}
		for (int i = 0; i < replayResult.getStepTypes().size(); i++) {
			StepTypes stepType = replayResult.getStepTypes().get(i);
			Object nodeInstance = replayResult.getNodeInstance().get(i);
			if (isMove(stepType)) {
				XEventClass eventClass = null;
				String transitionId = null;
				if (nodeInstance instanceof XEventClass) {
					/*
					 * nodeInstance is an XEventClass
					 */
					eventClass = (XEventClass) nodeInstance;
				} else if (nodeInstance instanceof Transition) {
					/*
					 * nodeInstance is a Transition
					 */
					//					eventClass = tecMap.get(nodeInstance);
					transitionId = ((Transition) nodeInstance).getLabel();
					eventClass = activities.get(transitionId);
				} else {
					System.err.println("Unknown node instance: " + nodeInstance);
				}
				traceAlignment.addLegalMove(stepType, eventClass, transitionId);
			}
		}
		return traceAlignment;
	}

	private boolean isMove(StepTypes stepType) {
		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L || stepType == StepTypes.MREAL
				|| stepType == StepTypes.MINVI;
	}
}
