package org.processmining.decomposedreplayer.algorithms.replay;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterReplayConfiguration;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public abstract class AbstractRecomposingReplayAlgorithm implements RecomposingReplayAlgorithm {
	
	private int iteration;

	protected ConflictUtils conflictUtils = new ConflictUtils();

	protected double emptyTraceCosts = -1;

	public final void recomposeReplay() {
		
		initialDecompose();
		
		iteration = 0;
		
		while (!stopReplay() || iteration == 0) {
			iteration++;
			
			if (iteration > 1)
				recompose();
			
			decomposedReplay();
			
			mergeIterationResults();
			
		}
		
		wrapUp();
	}
	
	protected abstract void initialDecompose();
	
	protected abstract boolean stopReplay();
	
	protected abstract void recompose();
	
	protected abstract void decomposedReplay();
	
	protected abstract void mergeIterationResults();
	
	protected abstract void wrapUp();

	protected Pair<Double, Double> getFitnessInterval(PluginContext context, Set<SyncReplayResult> alignments, XLog log,
			AcceptingPetriNet net, RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters) {
		emptyTraceCosts = getEmptyTraceCost(context, log, net, workspace, parameters);
		double sumLoCosts = 0;
		double sumHiCosts = 0;
		double sumMaxCosts = 0;
		for (SyncReplayResult alignment : alignments) {
			double costs = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
			double maxCosts = emptyTraceCosts + log.get(alignment.getTraceIndex().first()).size()
					* parameters.getMoveOnLogCosts();
			int nofTraces = alignment.getTraceIndex().size();
			sumLoCosts += nofTraces * costs;
			sumMaxCosts += nofTraces * maxCosts;
			if (conflictUtils.getConflicts(alignment).isEmpty()) {
				sumHiCosts += nofTraces * costs;
			} else {
				sumHiCosts += nofTraces * maxCosts;
			}
		}
		return new Pair<Double, Double>(1.0 - sumHiCosts / sumMaxCosts, 1.0 - sumLoCosts / sumMaxCosts);
	}

	protected double getEmptyTraceCost(PluginContext context, XLog log, AcceptingPetriNet net,
			RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters) {
		if (parameters.isPlanB()) {
			return getEmptyTraceCostPlanB(context, log, net, workspace, parameters);
		}
		if (emptyTraceCosts < 0.0) {
			XLog emptyTraceLog = workspace.factory.createLog();
			emptyTraceLog.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
			emptyTraceLog.getClassifiers().addAll(log.getClassifiers());
			emptyTraceLog.add(workspace.factory.createTrace());
			DecomposedReplayParameters pars = new DecomposedReplayParameters(log, net,
					workspace.parameters.getClassifier());
			pars.setPercentage(0);
			DecomposedReplayConfiguration config = new DecomposedGenericFilterReplayConfiguration();
			pars.setUnsplittableActivities(workspace.parameters.getUnsplittableActivities());
			pars.setMoveOnLogCosts(parameters.getMoveOnLogCosts());
			pars.setMoveOnModelCosts(parameters.getMoveOnModelCosts());
			pars.setMapping(parameters.getMapping());
			PNRepResult logAlignment = config.apply(context, emptyTraceLog, net, pars, null);
			double modelMoveCosts = logAlignment.iterator().next().getInfo().get(PNRepResult.RAWFITNESSCOST);
			System.out.println("[RecomposingReplayAlgorithm] Exact value for model move costs: " + modelMoveCosts);
			return modelMoveCosts;
		}
		System.out.println("[RecomposingReplayAlgorithm] Exact value for model move costs: " + emptyTraceCosts);
		return emptyTraceCosts;
	}
	
	/*
	 * Estimate the minimal costs to get from the initial state to a final
	 * state,
	 */
	protected double getEmptyTraceCostPlanB(PluginContext context, XLog log, AcceptingPetriNet net,
			RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters) {
		if (emptyTraceCosts < 0.0) {
			double minModelMoveCosts = net.getNet().getTransitions().size() * parameters.getMoveOnModelCosts();
			Set<SyncReplayResult> all = new HashSet<SyncReplayResult>(workspace.alignments);
			all.addAll(workspace.pseudoAlignments);
			all.addAll(workspace.openAlignments);
			for (SyncReplayResult alignment : all) {
				double modelMoveCosts = 0.0;
				for (StepTypes move : alignment.getStepTypes()) {
					switch (move) {
						case LMGOOD :
							// Fall through.
						case MREAL :
							modelMoveCosts += parameters.getMoveOnModelCosts();
							break;
						default :
					}
				}
				if (modelMoveCosts < minModelMoveCosts) {
					minModelMoveCosts = modelMoveCosts;
				}
			}
			System.out.println("[RecomposingReplayAlgorithm] Upper bound for model move costs: " + minModelMoveCosts);
			return minModelMoveCosts;
		}
		System.out.println("[RecomposingReplayAlgorithm] Upper bound for model move costs: " + emptyTraceCosts);
		return emptyTraceCosts;
	}

	protected IPNReplayParameter createReplayParameters(Collection<XEventClass> activities,
			XEventClass invisibleActivity, AcceptingPetriNet net) {
		IPNReplayParameter parameters = new CostBasedCompleteParam(activities, invisibleActivity, net.getNet()
				.getTransitions());
		parameters.setInitialMarking(net.getInitialMarking());
		Set<Marking> finalMarkings = net.getFinalMarkings();
		if (finalMarkings.isEmpty()) {
			finalMarkings = new HashSet<Marking>();
			finalMarkings.add(new Marking());
		}
		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
		return parameters;
	}
	
	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}
}
