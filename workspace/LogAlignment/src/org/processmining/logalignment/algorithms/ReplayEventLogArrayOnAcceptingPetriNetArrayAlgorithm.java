package org.processmining.logalignment.algorithms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.models.impl.ReplayResultArrayFactory;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters.Type;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerMovePreferenceAwareWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

import nl.tue.alignment.Progress;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.algorithms.ReplayAlgorithm;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
import nl.tue.astar.AStarException;
import nl.tue.astar.AStarThread.Canceller;

public class ReplayEventLogArrayOnAcceptingPetriNetArrayAlgorithm implements Canceller {

	private long deadline = 0;
	private boolean isCancelled = false;

	public ReplayResultArray apply(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor factor, ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		return apply(context, logs, nets, null, factor, parameters);
	}
	
	private ReplayerParameters getAlignParams(ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters, int logSize) {
		ReplayerParameters alignParams;
		
		boolean moveSort = parameters.isMoveSort();
		boolean queueSort = parameters.isQueueSort();
		boolean preferExact = parameters.isPreferExact();
		boolean useInt = parameters.isUseInt();
		int threads = parameters.getnThreads();
		Debug debug = parameters.getDebug();
		int maxNumberOfStates = parameters.getMaximumNumberOfStates();
		boolean partialOrder = parameters.isPartiallyOrderEvents();
		int costUpperBound = parameters.getCostUpperBound() > 0 ? parameters.getCostUpperBound() : Integer.MAX_VALUE;

		// False advertisement since millis = secs * 1000
		// but check out Replayer.computePNRepResult, millis * 10.0 / (log.size() + 1) to get
		// trace timeout. Very strange... +1 is for the empty trace but why * 10.0???
		// +1 to account for the empty trace
		int timeout = (logSize + 1) * parameters.getTimeoutPerTraceInSecs() * 1000 / 10;
		Type algoType = parameters.getAlgorithmType();
		
//		System.out.println(String.format("[%s] Log size: %d", getClass().getSimpleName(), logSize));
//		System.out.println(String.format("[%s] Algorithm type: %s", getClass().getSimpleName(), algoType));
//		System.out.println(String.format("[%s] No. of threads: %d", getClass().getSimpleName(), threads));
		
		switch(algoType) {
			case ASTAR:
				alignParams = new ReplayerParameters.AStar(moveSort, queueSort, preferExact, threads,
						useInt, debug, timeout, maxNumberOfStates, costUpperBound, partialOrder);
				break;
			case INC0:
				alignParams = new ReplayerParameters.IncementalAStar(moveSort, threads, useInt, debug, timeout,
						maxNumberOfStates, costUpperBound, partialOrder, 0);
				break;
			case INC3:
				alignParams = new ReplayerParameters.IncementalAStar(moveSort, threads, useInt, debug, timeout,
						maxNumberOfStates, costUpperBound, partialOrder, 3);
				break;
			case INC:
				alignParams = new ReplayerParameters.IncementalAStar(moveSort, threads, useInt, debug, timeout,
						maxNumberOfStates, costUpperBound, partialOrder, parameters.getInitialSplits());
				break;
			case INC_PLUS:
				alignParams = new ReplayerParameters.IncementalAStar(moveSort, threads, useInt, debug, timeout,
						maxNumberOfStates, costUpperBound, partialOrder, parameters.isPreProcessUsingPlaceBasedConstraints());
				break;
			default:
				alignParams = null;
				System.out.println(String.format("[%s] Do not recognize algorithm type: %d", getClass().getSimpleName(), parameters.getAlgorithmType()));
				System.exit(1);
		}
		
		return alignParams;
	}

	public ReplayResultArray apply(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ActivityClusterArray clusters, ReplayCostFactor factor,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		ReplayResultArray replays = ReplayResultArrayFactory.createReplayResultArray();

		replays.init();
		isCancelled = false;
		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
		Set<XEventClass> activities = new HashSet<XEventClass>();

		/*
		 * Count how many times every activity appears in some cluster. Needed
		 * for sync-move fitness metric.
		 * 
		 * We need the clusters to count in how many clusters an activity is.
		 */
		Map<XEventClass, Integer> counts = new HashMap<XEventClass, Integer>();
		if (clusters != null) {
			for (Set<XEventClass> cluster : clusters.getClusters()) {
				for (XEventClass activity : cluster) {
					int count = (counts.containsKey(activity) ? counts.get(activity) : 0);
					counts.put(activity, count + 1);
				}
			}
		}
		
		String statsDirpath = parameters.getOutputDir() + File.separator + "stats";
		File statsDir = new File(statsDirpath);
		if (!statsDir.isDirectory()) {
			statsDir.mkdirs();
		}

		for (int index = 0; index < size; index++) {
			PNRepResult replay = null;
			addLocalTransitions(parameters.getMapping(), nets.getNet(index));
			try {
				XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), parameters.getClassifier());
				XEventClass invisibleActivity = XUtils.INVISIBLEACTIVITY;
				Collection<XEventClass> eventClasses = new HashSet<XEventClass>(info.getEventClasses().getClasses());
				eventClasses.addAll(parameters.getMapping().values());
				CostBasedCompleteParam replayParameters = createReplayParameters(eventClasses, invisibleActivity,
						nets.getNet(index), parameters);
				if (factor != null) {
					Map<XEventClass, Integer> logMoveCosts = replayParameters.getMapEvClass2Cost();
					for (XEventClass activity : logMoveCosts.keySet()) {
						logMoveCosts.put(activity, logMoveCosts.get(activity) * factor.getCostFactor(activity));
					}
					Map<Transition, Integer> modelMoveCosts = replayParameters.getMapTrans2Cost();
					for (Transition transition : modelMoveCosts.keySet()) {
						if (!transition.isInvisible()) {
							modelMoveCosts.put(
									transition,
									modelMoveCosts.get(transition)
											* factor.getCostFactor(parameters.getMapping().get(transition)));
						}
					}
				}

				TransEvClassMapping subMapping = TransEvClassMappingUtils.getInstance().getMapping(
						nets.getNet(index).getNet(), activities, parameters.getClassifier());
				for (Transition subTransition : nets.getNet(index).getNet().getTransitions()) {
					subMapping.put(subTransition, invisibleActivity);
					for (Transition transition : parameters.getMapping().keySet()) {
						if (!parameters.getMapping().get(transition).equals(invisibleActivity)) {
							if (!subTransition.isInvisible() && transition.getLabel().equals(subTransition.getLabel())) {
								subMapping.put(subTransition, parameters.getMapping().get(transition));
								continue;
							}
						}
					}
				}

				System.out.println("[ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin] Replaying cluster "
						+ info.getEventClasses().getClasses());
				long now = -System.currentTimeMillis();
				setDeadline(parameters.getDeadline());
				if (clusters == null) {
					System.out.println("[ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin] No preferred transitions");
//					replay = (new PetrinetReplayerWithILP()).replayLog(context, nets.getNet(index).getNet(),
//							logs.getLog(index), subMapping, replayParameters);
					
					// Using new Alignment package
					try {
						
						ReplayerParameters alignParams = getAlignParams(parameters, logs.getLog(index).size());
						
						PrintStream stream;
						
						String statsFp = statsDirpath + File.separator + index + ".csv";
						
						if (alignParams.debug == Debug.STATS) {
							stream = new PrintStream(new File(statsFp));
						} else if (alignParams.debug == Debug.DOT) {
							stream = new PrintStream(new File(statsFp));
						} else {
							stream = System.out;
						}
						
						ReplayAlgorithm.Debug.setOutputStream(stream);
						
						Petrinet net = nets.getNet(index).getNet();
						Marking initialMarking = nets.getNet(index).getInitialMarking();
						Set<Marking> finalMarkings = nets.getNet(index).getFinalMarkings();
						Marking finalMarking = finalMarkings.iterator().next();
						
						XEventClasses classes = info.getEventClasses();
						TransEvClassMapping mapping = subMapping;
						
						Map<Transition, Integer> costMM = replayParameters.getMapTrans2Cost();
						Map<XEventClass, Integer> costLM = replayParameters.getMapEvClass2Cost();
						
						assert finalMarkings.size() == 1;
						
						if (finalMarkings.size() != 1) {
							System.out.println(String.format("[%s] Petri net has %d final markings.", getClass().getSimpleName(), finalMarkings.size()));
							System.exit(1);
						}
						
						Replayer replayer = new Replayer(alignParams, net, initialMarking, finalMarking, 
								classes, costMM, costLM, mapping, true);
						
						replay = replayer.computePNRepResult(Progress.INVISIBLE, logs.getLog(index));
					} catch (IOException ioe) {
						
						System.out.println(String.format("[%s] Problem with print stream: %s", getClass().getSimpleName(), parameters.getOutputDir()));
						ioe.printStackTrace();
					
					} catch (Exception e) {
						
						System.out.println("[%s] Problem with replayer. Defaulting to Arya replayer...");
						e.printStackTrace();
						
						replay = (new PetrinetReplayerWithILP()).replayLog(context, nets.getNet(index).getNet(),
								logs.getLog(index), subMapping, replayParameters);
								
					}
					
				} else {
					/*
					 * Create a set of border transitions for the cluster.
					 */
					Set<XEventClass> cluster = clusters.getCluster(index);
					/*
					 * Get the border activities from the cluster.
					 */
					Set<XEventClass> borderActivities = clusters.getInputs(cluster);
					borderActivities.addAll(clusters.getOutputs(cluster));	
					/*
					 * Get the border transitions from the mapping
					 */
					Set<Transition> borderTransitions = new HashSet<Transition>();
					for (Transition transition : subMapping.keySet()) {
						if (borderActivities.contains(subMapping.get(transition))) {
							borderTransitions.add(transition);
						}
					}
					System.out.println("[ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin] Preferred transitions: " + borderTransitions);
					/*
					 * Call the replayer, having the border transitions as preferred transitions.
					 */
					replay = (new PetrinetReplayerMovePreferenceAwareWithILP()).replayLog(context,
							nets.getNet(index).getNet(), logs.getLog(index), subMapping, replayParameters, borderTransitions);
				}

				/*
				 * Add the sync-move fitness metric.
				 */
//				if (clusters != null) {
//					for (SyncReplayResult alignment : replay) {
//						double numerator = 0.0;
//						double denominator = 0.0;
//						double count = 1.0;
//						int nofConformingMoves = 0;
//						int nofMoves = 0;
//						for (StepTypes stepType : alignment.getStepTypes()) {
//							switch (stepType) {
//								case LMGOOD :
//								case MREAL : {
//									Object object = alignment.getNodeInstance().get(nofMoves);
//									Transition transition = (Transition) object;
//									XEventClass activity = subMapping.get(transition);
//									if (counts.containsKey(activity)) {
//										count = 1.0 / counts.get(activity);
//									}
//									break;
//								}
//								case L : {
//									Object object = alignment.getNodeInstance().get(nofMoves);
//									XEventClass activity = (XEventClass) object;
//									if (counts.containsKey(activity)) {
//										count = 1.0 / counts.get(activity);
//									}
//									break;
//								}
//								default : {
//									count = 1.0;
//								}
//							}
//							denominator += count;
//							switch (stepType) {
//								case LMGOOD :
//								case MINVI : {
//									numerator += count;
//									nofConformingMoves++;
//									break;
//								}
//								default :
//							}
//							nofMoves++;
//						}
//						alignment.getInfo().put(AlignmentInfoLabels.DECOMPOSEDSYNCMOVENUMERATOR, numerator);
//						alignment.getInfo().put(AlignmentInfoLabels.DECOMPOSEDSYNCMOVEDENOMINATOR, denominator);
//						double syncMoveFitness = (nofMoves == 0 ? 1.0 : (1.0 * nofConformingMoves) / nofMoves);
//						alignment.getInfo().put(AlignmentInfoLabels.SYNCMOVEFITNESS, syncMoveFitness);
//					}
//				}

				now += System.currentTimeMillis();
				System.out.println("[ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin] Replay " + index + " took " + now
						+ " milliseconds, fitness is " + replay.getInfo().get(PNRepResult.TRACEFITNESS));
				int ctr = 0;
				for (SyncReplayResult traceResult : replay) {
					if (!traceResult.isReliable()) {
						ctr++;
					}
				}
				if (ctr > 0) {
					System.out.println("[ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin] " + ctr + " out of "
							+ replay.size() + " trace alignments are not reliable.");
				}
			} catch (AStarException e) {
				e.printStackTrace();
			}
			replays.addReplay(replay);
		}
		return replays;
	}

	private void addLocalTransitions(TransEvClassMapping map, AcceptingPetriNet net) {
		Map<String, Transition> labelTransitionMap = new HashMap<String, Transition>();
		for (Transition transition : map.keySet()) {
			labelTransitionMap.put(transition.getLabel(), transition);
		}
		for (Transition transition : net.getNet().getTransitions()) {
			if (!map.containsKey(transition)) {
				map.put(transition, map.get(labelTransitionMap.get(transition.getLabel())));
			}
		}
	}

	private CostBasedCompleteParam createReplayParameters(Collection<XEventClass> activities,
			XEventClass invisibleActivity, AcceptingPetriNet net,
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters params) {
		CostBasedCompleteParam parameters = new CostBasedCompleteParam(activities, invisibleActivity, net.getNet()
				.getTransitions(), params.getMoveOnModelCosts(), params.getMoveOnLogCosts());
		parameters.setInitialMarking(net.getInitialMarking());
		parameters.setCanceller(this);
		Set<Marking> finalMarkings = net.getFinalMarkings();
		if (finalMarkings.isEmpty()) {
			finalMarkings = new HashSet<Marking>();
			finalMarkings.add(new Marking());
		}
		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
		return parameters;
	}

	private void setDeadline(long deadline) {
		this.deadline = (deadline > 0 ? System.currentTimeMillis() + deadline : deadline);
	}

	public boolean isCancelled() {
		if ((deadline > 0) && (System.currentTimeMillis() > deadline)) {
			System.out
					.println("[ReplayEventLogArrayOnAcceptingPetriNetArrayAlgorithm] Replay takes too long, cancelled");
			isCancelled = true;
		}
		return isCancelled;
	}
}
