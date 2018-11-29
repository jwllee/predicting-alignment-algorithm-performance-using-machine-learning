package org.processmining.logalignment.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.TraceAlignment;
import org.processmining.logalignment.models.impl.LogAlignmentArrayFactory;
import org.processmining.logalignment.models.impl.LogAlignmentFactory;
import org.processmining.logalignment.models.impl.TraceAlignmentFactory;
import org.processmining.logalignment.parameters.FilterLogAlignmentParameters;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

public class FilterLogAlignmentAlgorithm {

	public LogAlignmentArray apply(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, FilterLogAlignmentParameters parameters) {
		LogAlignmentArray filteredAlignments = LogAlignmentArrayFactory.createLogAlignmentArray();
		filteredAlignments.init();
		for (int index = 0; index < alignments.getSize(); index++) {
			if (index < nets.getSize()) {
				LogAlignment alignment = alignments.getAlignment(index);
				AcceptingPetriNet net = nets.getNet(index);
				Set<String> transitionIds = new HashSet<String>();
				for (Transition transition : net.getNet().getTransitions()) {
					transitionIds.add(transition.getLabel());
				}
				filteredAlignments.addAlignment(filterPrivate(alignment, transitionIds));
			}
		}
		return filteredAlignments;
	}

	private LogAlignment filterPrivate(LogAlignment alignments, Set<String> transitionIds) {
		LogAlignment filteredAlignments = LogAlignmentFactory.createLogAlignment();
		filteredAlignments.init();
		filteredAlignments.setCluster(alignments.getCluster());
		for (List<XEventClass> trace : alignments.getAlignments().keySet()) {
			TraceAlignment filteredAlignment = TraceAlignmentFactory.createTraceAlignment();
			filteredAlignment.init();
			TraceAlignment alignment = alignments.getAlignment(trace);
			filteredAlignment.setReliable(alignment.isReliable());
			filteredAlignment.setMillis(alignment.getMillis());
			filteredAlignment.getConflictingActivities().putAll(alignment.getConflictingActivities());
			for (int index = 0; index < alignment.getSize(); index++) {
				Pair<StepTypes, Pair<XEventClass, String>> step = alignment.getLegalMoves().get(index);
				if (transitionIds.contains(step.getSecond().getSecond()) || alignments.getCluster().contains(step.getSecond().getFirst())) {
					filteredAlignment.addLegalMove(step.getFirst(), step.getSecond().getFirst(), step.getSecond()
							.getSecond());
				}
			}
			filteredAlignment.setCosts(alignment.getCosts());
			filteredAlignment.setMaxCosts(alignment.getMaxCosts());
			/*
			 * If the alignment has been shortened, then some silent transitions were removed.
			 * These also need to be removed from the sync-move numerator and denominator.
			 */
			double delta = filteredAlignment.getSize() - alignment.getSize();
			filteredAlignment.setDecomposedSyncMoveNumerator(alignment.getDecomposedSyncMoveNumerator() + delta);
			filteredAlignment.setDecomposedSyncMoveDenominator(alignment.getDecomposedSyncMoveDenominator() + delta);

			// adding the state info from alignment computation
			filteredAlignment.setStateCount(alignment.getStateCount());
			filteredAlignment.setQueuedStates(alignment.getQueuedStates());
			filteredAlignment.setTraversedArcs(alignment.getTraversedArcs());
			
			// add exit code from new alignment package
			filteredAlignment.setTraceExitCode(alignment.getTraceExitCode());
			
			filteredAlignments.putAlignment(trace, filteredAlignment);
		}
		return filteredAlignments;
	}
}
