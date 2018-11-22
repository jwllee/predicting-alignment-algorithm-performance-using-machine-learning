package org.processmining.decomposedreplayer.algorithms.replay;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.utils.ConflictUtils;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.TraceReplayTask.TraceReplayResult;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public abstract class AbstractRecomposingReplayAlgorithm implements RecomposingReplayAlgorithm {
	
	private int iteration;

	protected ConflictUtils conflictUtils = new ConflictUtils();

	protected double emptyTraceCosts = -1;

	public final void recomposeReplay() {
		
		initialDecompose();
		
		iteration = 0;
		
		while (iteration <= 0 || !stopReplay()) {
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
			
//			DecomposedReplayParameters pars = new DecomposedReplayParameters(log, net,
//					workspace.parameters.getClassifier());
//			pars.setPercentage(0);
//			DecomposedReplayConfiguration config = new DecomposedGenericFilterReplayConfiguration();
//			pars.setUnsplittableActivities(workspace.parameters.getUnsplittableActivities());
//			pars.setMoveOnLogCosts(parameters.getMoveOnLogCosts());
//			pars.setMoveOnModelCosts(parameters.getMoveOnModelCosts());
//			pars.setMapping(parameters.getMapping());
//			
//			// new alignment package
//			pars.setAlgorithmType(Type.ASTAR);
//			pars.setMoveSort(parameters.isMoveSort());
//			pars.setQueueSort(parameters.isQueueSort());
//			pars.setPreferExact(parameters.isPreferExact());
//			pars.setnThreads(parameters.getnThreads());
//			pars.setUseInt(false);
//			pars.setDebug(null);
//			pars.setTimeoutPerTraceInSecs(parameters.getTimeoutPerTraceInSecs());
//			pars.setMaximumNumberOfStates(parameters.getMaximumNumberOfStates());
//			pars.setCostUpperBound(parameters.getCostUpperBound());
//			pars.setPartiallyOrderEvents(parameters.isPartiallyOrderEvents());
//			pars.setPreProcessUsingPlaceBasedConstraints(parameters.isPreProcessUsingPlaceBasedConstraints());
//			pars.setInitialSplits(0);
//			pars.setPrintAlignments(false);
//			
//			PNRepResult logAlignment = config.apply(context, emptyTraceLog, net, pars, null);
			
			int nThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
			Marking initMarking = net.getInitialMarking();
			Set<Marking> finalMarkings = net.getFinalMarkings();
			Marking finalMarking = net.getFinalMarkings().iterator().next();
			TransEvClassMapping mapping = parameters.getMapping();
			XEventClasses classes = log.getInfo(parameters.getClassifier()).getEventClasses();
			
			if (finalMarkings.size() != 1) {
				System.out.println(String.format("[%s] Final marking set size: %d", getClass().getSimpleName(), finalMarkings.size()));
				System.exit(1);
			}
			
			ReplayerParameters params = new ReplayerParameters.Default(nThreads, Debug.NONE);
			Replayer replayer = new Replayer(params, net.getNet(), initMarking, finalMarking, classes, mapping, false);
			
			ExecutorService service = Executors.newFixedThreadPool(nThreads);

			TraceReplayTask tr = new TraceReplayTask(replayer, params, parameters.getTimeoutPerTraceInSecs(), 
					parameters.getMaximumNumberOfStates(), 0l);
			
			long start = System.nanoTime();
			Future<TraceReplayTask> result = service.submit(tr);
			TraceReplayTask traceReplay;
			
			// get the max model cost
			double modelMoveCosts = 0.0;
			
			try {
				traceReplay = result.get();
				long end = System.nanoTime();
				long taken = (end - start) / 1000000;
				System.out.println(String.format("[%s] Empty trace took %d ms to compute", getClass().getSimpleName(), taken));
				
				if (traceReplay.getResult() == TraceReplayResult.SUCCESS) {
					modelMoveCosts = traceReplay.getSuccesfulResult().getInfo().get(PNRepResult.RAWFITNESSCOST);
				} else if (traceReplay.getResult() == TraceReplayResult.DUPLICATE) {
					modelMoveCosts = 0.0;
					System.out.println(String.format("[%s] Empty trace replay result code: duplicate", getClass().getSimpleName()));
					System.exit(1);
				} else {
					modelMoveCosts = 0.0;
					System.out.println(String.format("[%s] Empty trace replay result code: unsuccessful", getClass().getSimpleName()));
					System.exit(1);
				}
			} catch (Exception e) {
				System.out.println(String.format("[%s] Problem with computing empty trace cost.", getClass().getSimpleName()));
				e.printStackTrace();
			} finally {
				service.shutdown();
			}
			
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
