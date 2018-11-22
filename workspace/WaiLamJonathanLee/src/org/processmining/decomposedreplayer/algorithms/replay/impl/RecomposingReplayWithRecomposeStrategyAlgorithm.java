package org.processmining.decomposedreplayer.algorithms.replay.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationInformation;
import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationStrategy;
import org.processmining.decomposedreplayer.algorithms.logcreation.impl.LogCreationStrategyFactory;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeInformation;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategy;
import org.processmining.decomposedreplayer.algorithms.recompose.RecomposeStrategyFactory;
import org.processmining.decomposedreplayer.algorithms.replay.AbstractRecomposingReplayAlgorithm;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterHundredReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericReplaceReduceReplayConfiguration;
import org.processmining.decomposedreplayer.experiments.utils.LogAlignmentJson;
import org.processmining.decomposedreplayer.experiments.utils.StepJson;
import org.processmining.decomposedreplayer.experiments.utils.TraceAlignmentJson;
import org.processmining.decomposedreplayer.models.stats.IterationStats;
import org.processmining.decomposedreplayer.models.stats.IterationStats.Statistic;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.providedobjects.ProvidedObjectManager;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.utils.AlignmentInfoLabels;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class RecomposingReplayWithRecomposeStrategyAlgorithm extends AbstractRecomposingReplayAlgorithm 
	implements RecomposeInformation, LogCreationInformation {
	
	private PluginContext context;
	private XLog log;
	private AcceptingPetriNet net;
	private RecomposingReplayWorkspace workspace;
	private RecomposingReplayParameters parameters;
	
	private RecomposeStrategy recomposeStrategy;
	private LogCreationStrategy logCreationStrategy;
	
	private Set<XEventClass> initialDecomposition;
	private List<ActivityClusterArray> clusterList; 
	private Set<XEventClass> recomposeActivities;
	
	private List<IterationStats> performanceStats;
	private List<LogAlignmentJson> logAlignments;
	
	private PNRepResult logAlignmentAtIteration;
	private Map<XEventClass, Double> logConflicts;
	
	private PNRepResult overallAlignments;
	
	public RecomposingReplayWithRecomposeStrategyAlgorithm(PluginContext context, XLog log, AcceptingPetriNet net, 
			RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters, 
			String recomposeStrategy, String logCreationStrategy) {
		this.context = context;
		this.net = net;
		this.log = log;
		this.workspace = workspace;
		this.parameters = parameters;
		clusterList = new ArrayList<>();
		recomposeActivities = new HashSet<>();
		setPerformanceStats(new LinkedList<IterationStats>());
		setLogAlignments(new ArrayList<LogAlignmentJson>());
		
		// using the default grouped single conflict recomposition strategy
		this.recomposeStrategy = RecomposeStrategyFactory.createRecomposeStrategy(recomposeStrategy);
		// using the default group by conflict log creation strategy
		this.logCreationStrategy = LogCreationStrategyFactory.createLogCreationStrategy(logCreationStrategy);
	}
	
	public PNRepResult apply() {
		double start = -System.currentTimeMillis();
		
		initialDecomposition = new HashSet<>(parameters.getUnsplittableActivities());
		
		recomposeReplay();
		
		double timeTaken = System.currentTimeMillis() + start;
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing replay algorithm took " + 
				timeTaken + " milliseconds.");
				
		return overallAlignments;
	}

	protected void initialDecompose() {
		System.out.println("[" + getClass().getSimpleName() + "] Initial decomposition.");
	}
	
	protected boolean stopReplay() {
		System.out.println("[" + getClass().getSimpleName() + "] Check to stop replay.");
		boolean stop = false;
		
		if (workspace.openAlignments.size() == 0) {
			// No open traces remain. We're done.
			stop = true;
			return stop;
		}

		Set<SyncReplayResult> allTraceAlignments = new HashSet<SyncReplayResult>(workspace.alignments);
		allTraceAlignments.addAll(workspace.pseudoAlignments);
		allTraceAlignments.addAll(workspace.openAlignments);
		
		if (workspace.parameters.stop(workspace,
				getFitnessInterval(context, allTraceAlignments, log, net, workspace, parameters))) {
			// Stop. We're done, but need to take the open alignments into account
			stop = true;
			return stop;
		}
		
		return stop;
	}
	
	private Set<XEventClass> getRecomposeActivities(Set<XEventClass> oldUnsplittables, Set<XEventClass> newUnsplittables) {
		Set<XEventClass> recomposeActivities = new HashSet<>(newUnsplittables);
		recomposeActivities.removeAll(oldUnsplittables);
		return recomposeActivities;
	}
	
	protected void recompose() {
		System.out.println("[" + getClass().getSimpleName() + "] Recomposing.");

		Set<XEventClass> unsplittables = recomposeStrategy.recompose(this);
		
		// get the new recompose activities
		recomposeActivities = getRecomposeActivities(workspace.parameters.getUnsplittableActivities(), unsplittables);
		
		// update the workspace
		workspace.parameters.setUnsplittableActivities(unsplittables);
		
//		printRecomposeInfo();
		
		// create a new log from the open alignments
		workspace.log = workspace.factory.createLog();
		// the new log will need the same classifier. Do not bother about attributes
		workspace.log.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		workspace.log.getClassifiers().addAll(log.getClassifiers());
		Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		Set<SyncReplayResult> openAlignments = new HashSet<SyncReplayResult>();
		
		logCreationStrategy.createLog(log, workspace.log, indexMap, workspace.openAlignments, 
				openAlignments, this);
		
		int nofTracesToAlign = workspace.openAlignments.size() - openAlignments.size();
		System.out.println("[" + getClass().getSimpleName() + "] Traces to align in iteration " + 
				getIteration() + ": " + nofTracesToAlign);
		
		workspace.openAlignments = openAlignments;
		workspace.indexMap = indexMap;
	}
	
	protected void decomposedReplay() {
		System.out.println("[" + getClass().getSimpleName() + "] Performing decomposed replay.");

		// each decomposed replay signals a new iteration
		workspace.nofIterations = getIteration();
		
		System.out.println("[" + getClass().getSimpleName() + "] Current iteration: " + workspace.nofIterations);
		
		System.out.println("[" + getClass().getSimpleName() + "] Cases to align: " + workspace.log.size() + ".");
		
		parameters.displayMessage(context, "Cases to align: " + workspace.log.size() + ".");
		
		XEventClassifier classifier = workspace.parameters.getClassifier();
		
		DecomposedReplayParameters pars = new DecomposedReplayParameters(log, net, classifier);
		DecomposedReplayConfiguration config = new DecomposedGenericFilterHundredReplayConfiguration();
		if (parameters.isUseHideAndReduceAbstraction()) {
			config = new DecomposedGenericReplaceReduceReplayConfiguration();
		}
		pars.setUnsplittableActivities(workspace.parameters.getUnsplittableActivities());
		pars.setMoveOnLogCosts(parameters.getMoveOnLogCosts());
		pars.setMoveOnModelCosts(parameters.getMoveOnModelCosts());
		pars.setTryConnections(false);
		
		pars.setMapping(parameters.getMapping());
		
		// new Alignment package
		String iterOutDirpath = parameters.getOutputDir() + File.separator + "iter-" + workspace.nofIterations;
		File iterOutDir = new File(iterOutDirpath);
		if (!iterOutDir.isDirectory()) {
			iterOutDir.mkdirs();
		}
		
		pars.setAlgorithmType(parameters.getAlgorithmType());
		pars.setMoveSort(parameters.isMoveSort());
		pars.setQueueSort(parameters.isQueueSort());
		pars.setPreferExact(parameters.isPreferExact());
		pars.setnThreads(parameters.getnThreads());
		pars.setUseInt(parameters.isUseInt());
		pars.setDebug(parameters.getDebug());
		pars.setOutputDir(iterOutDirpath);
		pars.setTimeoutPerTraceInSecs(parameters.getTimeoutPerTraceInSecs());
		pars.setMaximumNumberOfStates(parameters.getMaximumNumberOfStates());
		pars.setCostUpperBound(parameters.getCostUpperBound());
		pars.setPartiallyOrderEvents(parameters.isPartiallyOrderEvents());
		pars.setPreProcessUsingPlaceBasedConstraints(parameters.isPreProcessUsingPlaceBasedConstraints());
		pars.setInitialSplits(parameters.getInitialSplits());
		pars.setPrintAlignments(parameters.isPrintAlignments());
		
		System.out.println("[" + getClass().getSimpleName() + "] Algorithm type at iteration " + workspace.nofIterations + ": " + pars.getAlgorithmType());
		
		logAlignmentAtIteration = config.apply(context, workspace.log, net, pars, null);
	}
	
	protected void mergeIterationResults() {
		System.out.println("[" + getClass().getSimpleName() + "] Merging results at iteration.");

		logConflicts = new HashMap<XEventClass, Double>();
		
		for (SyncReplayResult openAlignment: workspace.openAlignments) {
			conflictUtils.addConflicts(logConflicts, conflictUtils.getConflicts(openAlignment));
		}
		
		// Check the result of the decomposed replay
		int nofLeftOutTraces = workspace.openAlignments.size();
		int valid = 0;
		int open = 0;
		int rejected = 0;
		for (SyncReplayResult traceAlignment: logAlignmentAtIteration) {
			
			// restore trace indices in the original log
			SortedSet<Integer> traceIndices = new TreeSet<Integer>();
			for (int index: traceAlignment.getTraceIndex()) 
				traceIndices.add(workspace.indexMap.get(index));
			traceAlignment.setTraceIndex(traceIndices);
			
			Map<XEventClass, Double> traceConflicts = conflictUtils.getConflicts(traceAlignment);
			if (!traceAlignment.isReliable()) {
				// this trace has failed to replay reliably. Assumption is that 
				// this will fail again in the next replay. Therefore, reject it.
				workspace.pseudoAlignments.add(traceAlignment);
				rejected++;
			} else if (traceConflicts.isEmpty()) {
				// we have TBA, hence an alignment
				workspace.alignments.add(traceAlignment);
				valid++;
			} else if (workspace.parameters.reject(traceAlignment, traceConflicts)) {
				// this trace has been rejected, hence a pseudo-alignment.
				workspace.pseudoAlignments.add(traceAlignment);
				rejected++;
			} else {
				conflictUtils.addConflicts(logConflicts, traceConflicts);
				open++;
				
//				System.out.println("[" + getClass().getSimpleName() + "] Log conflicts with new sub-alignments: " + traceConflicts);
				
				// this trace remains open
				workspace.openAlignments.add(traceAlignment);
			}
			
		}

		IterationStats stats = new IterationStats(getIteration());
		stats.map.put(Statistic.N_EXCLUDED_TRACE, nofLeftOutTraces);
		stats.map.put(Statistic.N_TRACE_ALIGNED, logAlignmentAtIteration.size());
		stats.map.put(Statistic.N_ALIGNMENT_VALID, valid);
		stats.map.put(Statistic.N_ALIGNMENT_OPEN, open);
		stats.map.put(Statistic.N_ALIGNMENT_REJECTED, rejected);
		
		if (getIteration() > 1) {
			stats.map.put(Statistic.N_SUBNET, getLastActivityClusters().getClusters().size());
			stats.map.put(Statistic.N_RECOMPOSE_ACTIVITY, recomposeActivities.size());
			stats.map.put(Statistic.N_BORDER_ACTIVITY, getBorderActivities().size());
		}
		
		// add costs info
		addCostToIterationStats(stats, workspace.alignments, workspace.pseudoAlignments, 
				workspace.openAlignments);
		
		performanceStats.add(stats);
		
		LogAlignmentJson logAlignmentIter = makeLogAlignmentJson(workspace.alignments, 
				workspace.pseudoAlignments, workspace.openAlignments);
		logAlignments.add(logAlignmentIter);
		
		System.out.println("[RecomposingReplayAlgorithm] accepted=" + workspace.alignments.size() + ", rejected="
				+ workspace.pseudoAlignments.size() + ", toalign=" + workspace.openAlignments.size() + ".");

	}
	
	private void addCostToIterationStats(IterationStats stats, Set<SyncReplayResult> alignments, 
			Set<SyncReplayResult> pseudoAlignments, Set<SyncReplayResult> openAlignments) {
		// this costs sum is guaranteed a lower bound
		double sumCosts = 0;
		for (SyncReplayResult alignment: alignments) {
			double costs = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
			int nofTraces = alignment.getTraceIndex().size();
			sumCosts += costs * nofTraces;
		}
		stats.map.put(Statistic.LOWER_BOUND_COST, sumCosts);
	}
	
	private LogAlignmentJson makeLogAlignmentJson(Set<SyncReplayResult> alignments, 
			Set<SyncReplayResult> pseudoAlignments, Set<SyncReplayResult> openAlignments) {
		LogAlignmentJson logAlignment = new LogAlignmentJson();
		int id = 0;
		
		for (SyncReplayResult alignment: alignments) {
			TraceAlignmentJson traceAlignment = makeTraceAlignmentJson(id + "", alignment);
			logAlignment.addTraceAlignment(traceAlignment);
			++id;
		}
		
		for (SyncReplayResult alignment: pseudoAlignments) {
			TraceAlignmentJson traceAlignment = makeTraceAlignmentJson(id + "", alignment);
			logAlignment.addTraceAlignment(traceAlignment);
			++id;
		}
		
		for (SyncReplayResult alignment: openAlignments) {
			TraceAlignmentJson traceAlignment = makeTraceAlignmentJson(id + "", alignment);
			logAlignment.addTraceAlignment(traceAlignment);
			++id;
		}
		
		return logAlignment;
	}
	
	private boolean isMove(StepTypes stepType) {
		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L || stepType == StepTypes.MREAL
				|| stepType == StepTypes.MINVI;
	}
	
	private TraceAlignmentJson makeTraceAlignmentJson(String id, SyncReplayResult alignment) {
		Set<Integer> traceIndexes = alignment.getTraceIndex();
		String nomove = ">>";
		XEventClass dummy = parameters.getMapping().getDummyEventClass();
		List<String> trace = new LinkedList<>();
		List<StepJson> steps = new LinkedList<>();
		for (int i = 0; i < alignment.getStepTypes().size(); i++) {
			StepTypes stepType = alignment.getStepTypes().get(i);
			Object nodeInstance = alignment.getNodeInstance().get(i);
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
					eventClass = parameters.getMapping().getOrDefault(nodeInstance, dummy);
				} else {
					System.err.println("Unknown node instance: " + nodeInstance);
				}
				// add to trace if it's log move or synchronous move
				if (!eventClass.equals(dummy))
					trace.add(eventClass.getId());
				// make step json
				String log = eventClass.equals(dummy) ? nomove : eventClass.getId();
				String model = transitionId == null ? nomove : transitionId;
				String type = stepType.toString();
				StepJson step = new StepJson(log, model, type);
				steps.add(step);
			}
		}
		TraceAlignmentJson traceAlignment = new TraceAlignmentJson(id);
		traceAlignment.setReliable(alignment.isReliable());
		traceAlignment.setSteps(steps);
		double costs = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
		traceAlignment.getStats().put("fitness_cost", costs);
		return traceAlignment;
	}
	
	protected void wrapUp() {
		System.out.println("[" + getClass().getSimpleName() + "] Wrapping up.");

		// all the alignments include: finished ones, open ones, and pseudo ones
		Set<SyncReplayResult> alignments = new HashSet<SyncReplayResult>(workspace.alignments);
		alignments.addAll(workspace.pseudoAlignments);
		alignments.addAll(workspace.openAlignments);
		
		overallAlignments = new PNRepResultImpl(alignments);
		IPNReplayAlgorithm replayer = new PetrinetReplayerWithILP();
		Set<XEventClass> activities = new HashSet<XEventClass>();
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		context.getProgress().setValue(info.getEventClasses().size());
		activities.addAll(info.getEventClasses().getClasses());
		activities.add(XUtils.MOVEONMODELACTIVITY);
		IPNReplayParameter replayerParameters = createReplayParameters(activities, XUtils.INVISIBLEACTIVITY, net);
		context.addConnection(new PNRepResultAllRequiredParamConnection("DaC recomposed alignment", net.getNet(), log,
				parameters.getMapping(), replayer, replayerParameters, overallAlignments));

		emptyTraceCosts = getEmptyTraceCost(context, log, net, workspace, parameters);
		overallAlignments.getInfo().put("Min Model-Move Cost", emptyTraceCosts);
		Object rawFitnessCost = overallAlignments.getInfo().get(PNRepResult.RAWFITNESSCOST);
		Object maxMoveLogCost = overallAlignments.getInfo().get(PNRepResult.MAXMOVELOGCOST);
		if (rawFitnessCost != null && rawFitnessCost instanceof Double && maxMoveLogCost != null && maxMoveLogCost instanceof Double) {
			overallAlignments.getInfo().put("Log Fitness", 1.0 - ((Double) rawFitnessCost/(emptyTraceCosts + (Double) maxMoveLogCost)));
		}
		double sumLoCosts = 0;
		double sumHiCosts = 0;
		double sumMaxCosts = 0;
		double sumLoFitness = 0;
		double sumHiFitness = 0;
		for (SyncReplayResult alignment : alignments) {
//			if (alignment.isReliable()) {
				double costs = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
				double maxCosts = emptyTraceCosts + log.get(alignment.getTraceIndex().first()).size()
						* parameters.getMoveOnLogCosts();
				double fitness = 1.0 - costs / maxCosts;
				int nofTraces = alignment.getTraceIndex().size();
				alignment.getInfo().put("Raw Fitness Cost Lo", costs);
				alignment.getInfo().put("Raw Fitness Cost Max", maxCosts);
				alignment.getInfo().put("Raw Fitness Perc Hi", fitness);
				sumLoCosts += nofTraces * costs;
				sumHiFitness += nofTraces * fitness; 
				sumMaxCosts += nofTraces * maxCosts;
				if (workspace.alignments.contains(alignment)) {
					sumHiCosts += nofTraces * costs;
					sumLoFitness += nofTraces * fitness;
					alignment.getInfo().put("Raw Fitness Cost Hi", costs);
					alignment.getInfo().put("Raw Fitness Perc Lo", fitness);
				} else {
					sumHiCosts += nofTraces * maxCosts;
					alignment.getInfo().put("Raw Fitness Cost Hi", maxCosts);
					alignment.getInfo().put("Raw Fitness Perc Lo", 0.0);
				}
				
				/*
				 * Computation of sync-move fitness metric for a trace. 
				 * The sync-move metric for a trace is the average over all traces. Arya takes care of that.
				 */
				int nofConformingMoves = 0;
				int nofMoves = 0;
				for (StepTypes stepType : alignment.getStepTypes()) {
					switch (stepType) {
						case LMGOOD:
						case MINVI:
							nofConformingMoves++;
							break;
						default:
					}
					nofMoves++;
				}
				double syncMoveFitness = (nofMoves == 0 ? 1.0 : (1.0 * nofConformingMoves)/nofMoves);
				double numerator = -1.0;
				double denominator = -1.0;
				alignment.getInfo().put(AlignmentInfoLabels.SYNCMOVEFITNESS, syncMoveFitness);
				if (alignment.getInfo().containsKey(AlignmentInfoLabels.DECOMPOSEDSYNCMOVENUMERATOR)) {
					numerator = alignment.getInfo().get(AlignmentInfoLabels.DECOMPOSEDSYNCMOVENUMERATOR);
				}
				if (alignment.getInfo().containsKey(AlignmentInfoLabels.DECOMPOSEDSYNCMOVEDENOMINATOR)) {
					denominator = alignment.getInfo().get(AlignmentInfoLabels.DECOMPOSEDSYNCMOVEDENOMINATOR);
				}
				if (numerator >= 0.0 && denominator >= 0.0) {
					double decomposedSyncMoveFitness = (denominator == 0.0 ? 1.0 : numerator / denominator);
					alignment.getInfo().put(AlignmentInfoLabels.DECOMPOSEDSYNCMOVEFITNESS, decomposedSyncMoveFitness);
				}
//			}
		}
		System.out.println("[RecomposingReplayAlgorithm] Replay costs interval = [" + sumLoCosts / log.size() + ","
				+ sumHiCosts / log.size() + "].");
		System.out.println("[RecomposingReplayAlgorithm] Fitness interval = [" + (1.0 - sumHiCosts / sumMaxCosts) + ","
				+ (1.0 - sumLoCosts / sumMaxCosts) + "].");
		System.out.println("[RecomposingReplayAlgorithm] Average trace fitness interval = [" + sumLoFitness / log.size() + ","
				+ sumHiFitness / log.size() + "].");
	}

	public Map<XEventClass, Double> getLogConflicts() {
		return logConflicts;
	}

	public PluginContext getContext() {
		return context;
	}

	public Set<XEventClass> getUnsplittables() {
		return workspace.parameters.getUnsplittableActivities();
	}

	public Set<SyncReplayResult> getOpenAlignments() {
		return workspace.openAlignments;
	}
	
	public Set<XEventClass> getInitialDecomposition() {
		return initialDecomposition;
	}

	public ActivityClusterArray getLastActivityClusters() {
		if (getIteration() == 1)
			throw new IllegalArgumentException("[" + getClass().getSimpleName() + "] No last activity cluster at iteration 1!");
		
		// no need to locate cluster from provided objects again
		if (getIteration() == clusterList.size() + 1) 
			return clusterList.get(clusterList.size() - 1);
		
		ProvidedObjectManager manager = context.getProvidedObjectManager();
		List<ProvidedObjectID> providedObjectIDs = manager.getProvidedObjects();
		// get the latest activity clusters
		ActivityClusterArray clusters = null;
		for (int index = providedObjectIDs.size() - 1; index >= 0; index--) {
			ProvidedObjectID id = providedObjectIDs.get(index);
			try {
				if (manager.getProvidedObjectLabel(id).equals("Clusters (generic)")) {
						clusters = (ActivityClusterArray) manager.getProvidedObjectObject(id, false);
						// just need to take care to skip the empty trace computation activity cluster in the first iteration
						if (!clusterList.isEmpty()) {
							break;
						}
				}
			} catch (ProvidedObjectDeletedException e) {
				e.printStackTrace();
			}
		}
		
		if (clusters == null)
			throw new IllegalStateException("[" + getClass().getSimpleName() + "] Cannot get previous activity cluster array!");
		
		System.out.println("Adding cluster " + clusterList.size() + "\n" + getBorderActivities(clusters));
		clusterList.add(clusters);
		System.out.println("[" + getClass().getSimpleName() + "] Number of clusters: " + clusterList.size());
		return clusterList.get(clusterList.size() - 1);
	}

	public Set<XEventClass> getRecomposeActivities() {
		return recomposeActivities;
	}
	
	private Set<XEventClass> getBorderActivities(ActivityClusterArray clusters) {
		Set<XEventClass> borderActivities = new HashSet<>();
		Set<XEventClass> seen = new HashSet<>();
		for (Set<XEventClass> cluster: clusters.getClusters()) {
			Set<XEventClass> candidates = new HashSet<>(cluster);
			candidates.retainAll(seen);
			borderActivities.addAll(candidates);
			seen.addAll(cluster);
		}
		
		// remove invisible
		if (borderActivities.contains(XUtils.INVISIBLEACTIVITY))
			borderActivities.remove(XUtils.INVISIBLEACTIVITY);
		
		return borderActivities;
	}

	public Set<XEventClass> getBorderActivities() {
		ActivityClusterArray clusters = getLastActivityClusters();
		Set<XEventClass> borderActivities = getBorderActivities(clusters);
		// remove the ones that we are recomposing on
		borderActivities.removeAll(recomposeActivities);
		return borderActivities;
	}

	public List<IterationStats> getPerformanceStats() {
		return performanceStats;
	}

	public void setPerformanceStats(List<IterationStats> performanceStats) {
		this.performanceStats = performanceStats;
	}

	public List<LogAlignmentJson> getLogAlignments() {
		return logAlignments;
	}

	public void setLogAlignments(List<LogAlignmentJson> logAlignments) {
		this.logAlignments = logAlignments;
	}
}
