package org.processmining.logalignment.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.framework.util.Pair;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.TraceAlignment;
import org.processmining.logalignment.models.impl.LogAlignmentFactory;
import org.processmining.logalignment.models.impl.TraceAlignmentFactory;
import org.processmining.logalignment.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

public class MergeLogAlignmentArrayIntoLogAlignmentAlgorithm {

	public LogAlignment apply(XLog log, LogAlignmentArray alignments, AcceptingPetriNetArray nets,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {

		LogAlignment logAlignment = LogAlignmentFactory.createLogAlignment();
		logAlignment.init();
		XEventClassifier classifier = parameters.getClassifier();
		ActivityClusterArray clusters = (new ConvertLogAlignmentsToActivityClusterArrayAlgorithm()).apply(alignments);
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		for (XTrace trace : log) {
			List<XEventClass> activities = new ArrayList<XEventClass>();
			for (XEvent event : trace) {
				activities.add(info.getEventClasses().getClassOf(event));
			}
			int nofClusters = clusters.getClusters().size();
			List<TraceAlignment> traceAlignments = new ArrayList<TraceAlignment>();
			for (int i = 0; i < nofClusters; i++) {
				logAlignment.addToCluster(clusters.getCluster(i));
				TraceAlignment traceAlignment = alignments.getAlignment(i).getAlignment(
						project(activities, clusters.getCluster(i)));
				if (traceAlignment == null) {
					/*
					 * Trace cannot be replayed successfully (no final marking
					 * can be reached). Return empty trace alignment.
					 */
					traceAlignment = TraceAlignmentFactory.createTraceAlignment();
					traceAlignment.init();
					traceAlignment.setReliable(false);
				}
				traceAlignments.add(traceAlignment);
			}
			logAlignment.putAlignment(activities,
					mergeTracesPrivate(activities, clusters, nets, traceAlignments, parameters));
		}
		return logAlignment;
	}

	@Deprecated
	private TraceAlignment mergeTracesPrivate(List<XEventClass> trace, ActivityClusterArray clusters,
			AcceptingPetriNetArray nets, List<TraceAlignment> alignments) {
		TraceAlignment alignment = TraceAlignmentFactory.createTraceAlignment();
		alignment.init();
		alignment.setLegalMoves(new ArrayList<Pair<StepTypes, Pair<XEventClass, String>>>(alignments.get(0)
				.getLegalMoves()));
		alignment.setCosts(alignments.get(0).getCosts());
		alignment.setMaxCosts(alignments.get(0).getMaxCosts());
		alignment.setReliable(alignments.get(0).isReliable());
		Set<XEventClass> cluster = new HashSet<XEventClass>(clusters.getCluster(0));
		Set<String> transitionIds = getTransitionIds(nets.getNet(0));
		Map<Pair<XEventClass, String>, Double> costs = new HashMap<Pair<XEventClass, String>, Double>();

		//				System.out.println("Start cluster: " + cluster);
		for (int i = 1; i < clusters.getClusters().size(); i++) {
			TraceAlignment otherAlignment = alignments.get(i);
			alignment.setCosts(alignment.getCosts() + otherAlignment.getCosts());
			alignment.setMaxCosts(alignment.getMaxCosts() + otherAlignment.getMaxCosts());
			alignment.setReliable(alignment.isReliable() && otherAlignment.isReliable());
			Set<XEventClass> otherCluster = clusters.getCluster(i);
			Set<String> otherTransitionIds = getTransitionIds(nets.getNet(i));
			alignment.setLegalMoves(mergeLegalMovesPrivate(alignment.getLegalMoves(), otherAlignment.getLegalMoves(),
					trace, cluster, otherCluster, transitionIds, otherTransitionIds, costs));
			cluster.addAll(otherCluster);
			transitionIds.addAll(otherTransitionIds);
		}
		return alignment;
	}

	@Deprecated
	private Set<String> getTransitionIds(AcceptingPetriNet net) {
		Set<String> transitionIds = new HashSet<String>();
		for (Transition transition : net.getNet().getTransitions()) {
			if (!transition.isInvisible()) {
				transitionIds.add(transition.getLabel());
			}
		}
		return transitionIds;
	}

	private List<XEventClass> project(List<XEventClass> trace, Set<XEventClass> cluster) {
		List<XEventClass> projectedTrace = new ArrayList<XEventClass>();
		for (XEventClass activity : trace) {
			if (cluster.contains(activity)) {
				projectedTrace.add(activity);
			}
		}
		return projectedTrace;
	}

	@Deprecated
	private List<Pair<StepTypes, Pair<XEventClass, String>>> mergeLegalMovesPrivate(
			List<Pair<StepTypes, Pair<XEventClass, String>>> legalMoves1,
			List<Pair<StepTypes, Pair<XEventClass, String>>> legalMoves2, List<XEventClass> trace,
			Set<XEventClass> activities1, Set<XEventClass> activities2, Set<String> transitionIds1,
			Set<String> transitionIds2, Map<Pair<XEventClass, String>, Double> costs) {

		int ctr1 = 0;
		int size1 = legalMoves1.size();
		int ctr2 = 0;
		int size2 = legalMoves2.size();
		int traceCtr = 0;
		int traceSize = trace.size();
		List<Pair<StepTypes, Pair<XEventClass, String>>> legalMoves = new ArrayList<Pair<StepTypes, Pair<XEventClass, String>>>();
		boolean progress = true;

		if (size1 == 0) {
			Set<XEventClass> activities = new HashSet<XEventClass>(activities1);
			activities.retainAll(trace);
			if (activities.isEmpty()) {
				// No activity needed to be replayed. Empty moves is okay.
				return legalMoves2;
			} else {
				// Propagate unability to replay.
				return legalMoves1;
			}
		}
		if (size2 == 0) {
			Set<XEventClass> activities = new HashSet<XEventClass>(activities2);
			activities.retainAll(trace);
			if (activities.isEmpty()) {
				// No activity needed to be replayed. Empty moves is okay.
				return legalMoves1;
			} else {
				// Propagate unability to replay.
				return legalMoves2;
			}
		}
		while (progress && (ctr1 < size1 || ctr2 < size2 || traceCtr < traceSize)) {
			progress = false;
			if (traceCtr < traceSize) {
				XEventClass activity = trace.get(traceCtr);
				boolean isActivity1 = activities1.contains(activity);
				boolean isActivity2 = activities2.contains(activity);
				if (!isActivity1 && !isActivity2) {
					traceCtr++;
					progress = true;
				} else if (isActivity1 && isActivity2 && ctr1 < size1 && ctr2 < size2
						&& activity.equals(legalMoves1.get(ctr1).getSecond().getFirst())
						&& activity.equals(legalMoves2.get(ctr2).getSecond().getFirst())) {
					if (legalMoves1.get(ctr1).getFirst() == StepTypes.LMGOOD) {
						legalMoves.add(legalMoves1.get(ctr1));
					} else if (legalMoves2.get(ctr2).getFirst() == StepTypes.LMGOOD) {
						legalMoves.add(legalMoves2.get(ctr2));
					} else if (!costs.containsKey(legalMoves1.get(ctr1))) {
						legalMoves.add(legalMoves2.get(ctr2));
					} else if (!costs.containsKey(legalMoves2.get(ctr2))) {
						legalMoves.add(legalMoves1.get(ctr1));
					} else if (costs.get(legalMoves1.get(ctr1)) <= costs.get(legalMoves2.get(ctr2))) {
						legalMoves.add(legalMoves1.get(ctr1));
					} else {
						legalMoves.add(legalMoves2.get(ctr2));
					}
					ctr1++;
					ctr2++;
					traceCtr++;
					progress = true;
				} else if (isActivity1 && !isActivity2 && ctr1 < size1
						&& activity.equals(legalMoves1.get(ctr1).getSecond().getFirst())) {
					legalMoves.add(legalMoves1.get(ctr1));
					ctr1++;
					traceCtr++;
					progress = true;
				} else if (!isActivity1 && isActivity2 && ctr2 < size2
						&& activity.equals(legalMoves2.get(ctr2).getSecond().getFirst())) {
					legalMoves.add(legalMoves2.get(ctr2));
					ctr2++;
					traceCtr++;
					progress = true;
				}
			}
			if (!progress) {
				boolean isTransition1 = false;
				boolean isTransition2 = false;
				boolean isModelMove1 = false;
				boolean isModelMove2 = false;
				if (ctr1 < size1) {
					isTransition1 = transitionIds2.contains(legalMoves1.get(ctr1).getSecond().getSecond());
					isModelMove1 = legalMoves1.get(ctr1).getFirst() == StepTypes.MREAL
							|| legalMoves1.get(ctr1).getFirst() == StepTypes.MINVI;
				}
				if (ctr2 < size2) {
					isTransition2 = transitionIds1.contains(legalMoves2.get(ctr2).getSecond().getSecond());
					isModelMove2 = legalMoves2.get(ctr2).getFirst() == StepTypes.MREAL
							|| legalMoves2.get(ctr2).getFirst() == StepTypes.MINVI;
				}
				if (isModelMove1 && isModelMove2 && isTransition1 && isTransition2) {
					if (!costs.containsKey(legalMoves1.get(ctr1))) {
						legalMoves.add(legalMoves2.get(ctr2));
					} else if (!costs.containsKey(legalMoves2.get(ctr2))) {
						legalMoves.add(legalMoves1.get(ctr1));
					} else if (costs.get(legalMoves1.get(ctr1)) <= costs.get(legalMoves2.get(ctr2))) {
						legalMoves.add(legalMoves1.get(ctr1));
					} else {
						legalMoves.add(legalMoves2.get(ctr2));
					}
					ctr1++;
					ctr2++;
					progress = true;
				} else if (isModelMove1 && !isTransition1) {
					legalMoves.add(legalMoves1.get(ctr1));
					ctr1++;
					progress = true;
				} else if (isModelMove2 && !isTransition2) {
					legalMoves.add(legalMoves2.get(ctr2));
					ctr2++;
					progress = true;
				} else if (isModelMove1 && isTransition1) {
					//					System.out.println("[MergeLogAlignmentArrayIntoLogAlignmentPlugin] Skip " + legalMoves1.get(ctr1).getSecond().getSecond());
					ctr1++;
					progress = true;
				} else if (isModelMove2 && isTransition2) {
					;
					//					System.out.println("[MergeLogAlignmentArrayIntoLogAlignmentPlugin] Skip " + legalMoves2.get(ctr2).getSecond().getSecond());
					ctr2++;
					progress = true;
				}
			}
		}
		if (ctr1 != size1 || ctr2 != size2 || traceCtr != traceSize) {
			/*
			 * Something's wrong, bail out.
			 */
			System.err.println("Cannot merge alignments further. Dumping diagnostic information.");
			if (traceCtr < traceSize) {
				System.err.println("    Head of trace: " + trace.get(traceCtr));
			}
			if (ctr1 < size1) {
				System.err.println("    Head of alignment 1: " + legalMoves1.get(ctr1));
			}
			if (ctr2 < size2) {
				System.err.println("    Head of alignment 2: " + legalMoves2.get(ctr2));
			}
			System.err.println("    Activities 1: " + activities1);
			System.err.println("    Activities 2: " + activities2);
			System.err.println("    Transitions 1: " + transitionIds1);
			System.err.println("    Transitions 2: " + transitionIds2);
			System.err.println("    Costs: " + costs);
			System.err.println("    Legal Moves 1: " + legalMoves1);
			System.err.println("    Legal Moves 2: " + legalMoves2);
			System.err.println("    Legal Moves: " + legalMoves);
			System.err.println("    Trace: " + trace);
			return null;
		}
		return legalMoves;
	}

	public Map<String, Set<Integer>> getLabelToNetsMap(AcceptingPetriNetArray nets) {
		Map<String, Set<Integer>> map = new HashMap<String, Set<Integer>>();
		for (int i = 0; i < nets.getSize(); i++) {
			Petrinet net = nets.getNet(i).getNet();
			for (Transition transition : net.getTransitions()) {
				if (!transition.isInvisible()) {
					String label = transition.getLabel();
					Set<Integer> netIndices = map.get(label);
					if (netIndices == null) {
						netIndices = new HashSet<Integer>();
					}
					netIndices.add(i);
					map.put(label, netIndices);
				}
			}
		}
		return map;
	}

	private TraceAlignment mergeTracesPrivate(List<XEventClass> trace, ActivityClusterArray clusters,
			AcceptingPetriNetArray nets, List<TraceAlignment> alignments,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		/*
		 * Initialize the resulting merged alignment.
		 */
		TraceAlignment mergedAlignment = TraceAlignmentFactory.createTraceAlignment();
		mergedAlignment.init();
		/*
		 * So far, the result is still reliable.
		 */
		boolean isReliable = true;

		/*
		 * Initialize necessary counters.
		 */
		int traceSize = trace.size();
		int toDoCtr = traceSize;
		double millis = 0.0;
		double syncMoveNumerator = 0.0;
		double syncMoveDenominator = 0.0;
		for (TraceAlignment alignment : alignments) {
			toDoCtr += alignment.getLegalMoves().size();
			if (isReliable && !alignment.isReliable()) {
				System.out
						.println("[MergeLogAlignmentArrayIntoLogAlignmentAlgorithm] Some alignments are not reliable");
				isReliable = false;
			}
			millis += alignment.getMillis();
			if (syncMoveNumerator >= 0.0) {
				if (alignment.getDecomposedSyncMoveNumerator() >= 0.0) {
					syncMoveNumerator += alignment.getDecomposedSyncMoveNumerator();
				} else {
					syncMoveNumerator = -1.0; // Insufficient information.
				}
			}
			if (syncMoveDenominator >= 0.0) {
				if (alignment.getDecomposedSyncMoveDenominator() >= 0.0) {
					syncMoveDenominator += alignment.getDecomposedSyncMoveDenominator();
				} else {
					syncMoveDenominator = -1.0; // Insufficient information.
				}
			}
		}
		mergedAlignment.setMillis(millis);
		mergedAlignment.setDecomposedSyncMoveNumerator(syncMoveNumerator);
		mergedAlignment.setDecomposedSyncMoveDenominator(syncMoveDenominator);
		int traceCtr = 0;
		int[] alignmentCtrs = new int[alignments.size()];
		int[] alignmentSizes = new int[alignments.size()];
		for (int i = 0; i < alignments.size(); i++) {
			alignmentCtrs[i] = 0;
			alignmentSizes[i] = alignments.get(i).getLegalMoves().size();
		}

		int nofClusters = clusters.getClusters().size();

		/*
		 * Memorize which visible transition label appear in which subnets.
		 */
		Map<String, Set<Integer>> labelToNetsMap = getLabelToNetsMap(nets);

		/*
		 * Initialize items necessary to check whether all relevant alignments
		 * can replay the next activity in the trace (if any).
		 */
		XEventClass activity = null;
		Set<Pair<StepTypes, Pair<XEventClass, String>>> logMoves = new HashSet<Pair<StepTypes, Pair<XEventClass, String>>>();
		Set<Integer> activityClusters = new HashSet<Integer>();
		Set<Integer> activityClustersToGo = new HashSet<Integer>();
		int logMoveCosts = 0;

		if (traceCtr < traceSize) {
			boolean isEmpty = true;

			while (isEmpty && traceCtr < traceSize) {
				/*
				 * There is a next activity. Get it.
				 */
				activity = trace.get(traceCtr);
				for (int i = 0; i < nofClusters; i++) {
					if (clusters.getCluster(i).contains(activity)) {
						if (alignmentCtrs[i] < alignmentSizes[i]) {
							activityClusters.add(i);
						}
					}
				}
				if (activityClusters.isEmpty()) {
					/*
					 * Activity not covered by model. Insert log move.
					 */
					traceCtr++;
					toDoCtr--;
					mergedAlignment.addLegalMove(StepTypes.L, activity, "");
					/*
					 * HV 20180425: Fixed bug: Add costs for log moves on activities not covered by model.
					 */
					logMoveCosts += parameters.getMoveOnLogCosts();
				} else {
					isEmpty = false;
				}
			}
		}
		if (traceCtr < traceSize) {
			/*
			 * activityClusters hold all indices of subnets that need to align
			 * on the next activity.
			 */
			activityClustersToGo.addAll(activityClusters);
			for (int i = 0; i < nofClusters; i++) {
				if (clusters.getCluster(i).contains(activity)) {
					if (alignmentCtrs[i] < alignmentSizes[i]) {
						/*
						 * HV 20180418 Fixed bug: move should be log move or sync move, not model move.
						 */
						if (isMatchingLogMove(activity, alignments.get(i).getLegalMoves().get(alignmentCtrs[i]))) {
							activityClustersToGo.remove(i);
							logMoves.add(alignments.get(i).getLegalMoves().get(alignmentCtrs[i]));
						}
					}
				}
			}
			/*
			 * activityClusterToGo holds the indices of subnets that still need
			 * to align on the next activity. logMoves contains all possible
			 * (log or synchronous) moves for the next activity.
			 */
		}
		/*
		 * As long as there is something to do, do something.
		 */
		while (toDoCtr > 0) {
			/*
			 * Check whether the first three checks made progress.
			 */
			boolean progress = true;

			while (progress) {
				/*
				 * So far, no progress.
				 */
				progress = false;

				/*
				 * First, try to advance on the next activity.
				 */
				if (traceCtr < trace.size() && activityClustersToGo.isEmpty()) {
					/*
					 * Can synchronize on next activity.
					 */
					if (logMoves.size() > 1) {
						/*
						 * Conflict. Result is not reliable.
						 */
						//						isReliable = false;
						if (parameters.isAddConflictOnlyOnce()) {
							mergedAlignment.addConflictingActivity(activity);
						} else {
							for (Pair<StepTypes, Pair<XEventClass, String>> logMove : logMoves) {
								mergedAlignment.addConflictingActivity(logMove.getSecond().getFirst());
							}
						}
					}
					/*
					 * Advance, and note that we have made progress.
					 */
					progress = true;
					traceCtr++;
					toDoCtr--;
					for (int i : activityClusters) {
						alignmentCtrs[i]++;
						toDoCtr--;
					}
					/*
					 * Add a suitable legal move to the merged alignment.
					 */
					addLegalMove(mergedAlignment,
							getBestLogMove(logMoves, parameters, activityClusters, alignments, alignmentCtrs));
					/*
					 * Start looking for next activity, if there is one.
					 */
					if (traceCtr < traceSize) {
						/*
						 * As before
						 */
						activityClusters = new HashSet<Integer>();
						activityClustersToGo = new HashSet<Integer>();
						logMoves = new HashSet<Pair<StepTypes, Pair<XEventClass, String>>>();

						boolean isEmpty = true;

						while (isEmpty && traceCtr < traceSize) {
							activity = trace.get(traceCtr);
							for (int i = 0; i < nofClusters; i++) {
								if (clusters.getCluster(i).contains(activity)) {
									if (alignmentCtrs[i] < alignmentSizes[i]) {
										activityClusters.add(i);
									}
								}
							}
							if (activityClusters.isEmpty()) {
								traceCtr++;
								toDoCtr--;
								mergedAlignment.addLegalMove(StepTypes.L, activity, "");
								/*
								 * HV 20180425: Fixed bug: Add costs for log moves on activities not covered by model.
								 */
								logMoveCosts += parameters.getMoveOnLogCosts();
							} else {
								isEmpty = false;
							}
						}
					}
					if (traceCtr < traceSize) {
						activityClustersToGo.addAll(activityClusters);
						for (int i = 0; i < nofClusters; i++) {
							if (clusters.getCluster(i).contains(activity)) {
								if (alignmentCtrs[i] < alignmentSizes[i]) {
									/*
									 * HV 20180418 Fixed bug: move should be log move or sync move, not model move.
									 */
									if (isMatchingLogMove(activity, alignments.get(i).getLegalMoves().get(alignmentCtrs[i]))) {
										activityClustersToGo.remove(i);
										logMoves.add(alignments.get(i).getLegalMoves().get(alignmentCtrs[i]));
									}
								}
							}
						}
					} else {
						activity = null;
					}
				}
				if (!(traceCtr < trace.size() && activityClustersToGo.isEmpty())) {
					/*
					 * Second, if no activity move possible, try to advance
					 * invisible model moves.
					 */
					for (int i = 0; i < alignments.size(); i++) {
						while (alignmentCtrs[i] < alignmentSizes[i]
								&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getFirst() == StepTypes.MINVI) {
							Pair<StepTypes, Pair<XEventClass, String>> legalMove = alignments.get(i).getLegalMoves()
									.get(alignmentCtrs[i]);
							/*
							 * Add the inviisble model move to the merged
							 * alignment.
							 */
							addLegalMove(mergedAlignment, legalMove);
							/*
							 * Update.
							 */
							progress = true;
							alignmentCtrs[i]++;
							toDoCtr--;
							/*
							 * Check whether this alignment can now replay the
							 * next activity.
							 */
							/*
							 * HV 20180418 Fixed bug: move should be log move or sync move, not model move.
							 */
							if (alignmentCtrs[i] < alignmentSizes[i]
									&& activity != null
									&& isMatchingLogMove(activity, alignments.get(i).getLegalMoves().get(alignmentCtrs[i]))) {
								/*
								 * Yes, it can. Take note.
								 */
								activityClustersToGo.remove(i);
								logMoves.add(alignments.get(i).getLegalMoves().get(alignmentCtrs[i]));
							}
						}
						if (traceCtr < trace.size() && activityClustersToGo.isEmpty()) {
							/*
							 * First do activity move.
							 */
							break;
						}
					}
				}
				if (!(traceCtr < trace.size() && activityClustersToGo.isEmpty())) {
					/*
					 * Third, if no activity move possible, try to advance on
					 * non-conflicting visible model moves.
					 */
					for (String label : labelToNetsMap.keySet()) {
						int ctr = 0;
						/*
						 * Count how many alignments can now replay this
						 * transition using a visible model move.
						 */
						for (int i : labelToNetsMap.get(label)) {
							if (alignmentCtrs[i] < alignmentSizes[i]
									&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getFirst() == StepTypes.MREAL
									&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getSecond().getSecond()
											.equals(label)) {
								ctr++;
							}
						}
						/*
						 * Check whether count is sufficient, that is, whether
						 * all alignments can.
						 */
						if (ctr == labelToNetsMap.get(label).size()) {
							/*
							 * Yes it is, we can now do a visible model move on
							 * this label.
							 */
							int k = labelToNetsMap.get(label).iterator().next();
							Pair<StepTypes, Pair<XEventClass, String>> legalMove = alignments.get(k).getLegalMoves()
									.get(alignmentCtrs[k]);
							/*
							 * Add the visible model move to the merged
							 * alignment.
							 */
							addLegalMove(mergedAlignment, legalMove);

							/*
							 * Check whether the involved alignments can now
							 * replay the next activity. And take note that
							 * we've made progress.
							 */
							for (int i : labelToNetsMap.get(label)) {
								/*
								 * Progress.
								 */
								progress = true;
								alignmentCtrs[i]++;
								toDoCtr--;
								/*
								 * Check for next activity.
								 */
								/*
								 * HV 20180418 Fixed bug: move should be log move or sync move, not model move.
								 */
								if (alignmentCtrs[i] < alignmentSizes[i]
										&& activity != null
										&& isMatchingLogMove(activity, alignments.get(i).getLegalMoves().get(alignmentCtrs[i]))) {
									activityClustersToGo.remove(i);
									logMoves.add(alignments.get(i).getLegalMoves().get(alignmentCtrs[i]));
								}
							}
						}
						if (traceCtr < trace.size() && activityClustersToGo.isEmpty()) {
							break;
						}
					}
				}
			}
			if (toDoCtr > 0) {
				/*
				 * We're not done yet, but... No move on the next activity, no
				 * invisible model move, and no non-conflicting visible model
				 * move. Do the best conflicting visible model move.
				 */
				String bestLabel = null;
				double bestScore = 0.0;
				/*
				 * Select the 'best' label to do a conflicting visible model
				 * move on. We now select a label that has minimal 'support'.
				 */
				for (String label : labelToNetsMap.keySet()) {
					/*
					 * Count how many alignments can now do this visible model
					 * move.
					 */
					int ctr = 0;
					for (int i : labelToNetsMap.get(label)) {
						if (alignmentCtrs[i] < alignmentSizes[i]
								&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getFirst() == StepTypes.MREAL
								&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getSecond().getSecond()
										.equals(label)) {
							ctr++;
						}
					}
					if (ctr > 0) {
						/*
						 * Check whether this 'support' is better than the
						 * existing one.
						 */
						double score = 1.0 - (1.0 * ctr) / labelToNetsMap.get(label).size();
						if (score > bestScore) {
							/*
							 * Yes it, is, replace existing one by this one.
							 */
							bestScore = score;
							bestLabel = label;
						}
					}
				}
				if (bestLabel != null) {
					/*
					 * We now need to do the selected, best, conflicting model
					 * move.
					 */
					Set<Pair<StepTypes, Pair<XEventClass, String>>> modelMoves = new HashSet<Pair<StepTypes, Pair<XEventClass, String>>>();
					Set<Integer> indices = new HashSet<Integer>();
					for (int i : labelToNetsMap.get(bestLabel)) {
						if (alignmentCtrs[i] < alignmentSizes[i]
								&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getFirst() == StepTypes.MREAL
								&& alignments.get(i).getLegalMoves().get(alignmentCtrs[i]).getSecond().getSecond()
										.equals(bestLabel)) {
							/*
							 * This alignment can indeed do this move. Do it.
							 */
							modelMoves.add(alignments.get(i).getLegalMoves().get(alignmentCtrs[i]));
							indices.add(i);
							//							isReliable = false;
							/*
							 * Found a conflict. Register it.
							 */
							if (!parameters.isAddConflictOnlyOnce()) {
								for (Pair<StepTypes, Pair<XEventClass, String>> modelMove : modelMoves) {
									mergedAlignment.addConflictingActivity(new XEventClass(modelMove.getSecond()
											.getSecond(), 0));
								}
							}
							alignmentCtrs[i]++;
							toDoCtr--;
							/*
							 * Check for next activity.
							 */
							/*
							 * HV 20180418 Fixed bug: move should be log move or sync move, not model move.
							 */
							if (alignmentCtrs[i] < alignmentSizes[i]
									&& activity != null
									&& isMatchingLogMove(activity, alignments.get(i).getLegalMoves().get(alignmentCtrs[i]))) {
								activityClustersToGo.remove(i);
								logMoves.add(alignments.get(i).getLegalMoves().get(alignmentCtrs[i]));
							}
						}
					}
					if (parameters.isAddConflictOnlyOnce()) {
						mergedAlignment.addConflictingActivity(new XEventClass(bestLabel, 0));
					}
					/*
					 * Add a suitable conflicting visible model move.
					 */
					addLegalMove(mergedAlignment,
							getBestVisibleModelMove(modelMoves, parameters, indices, alignments, alignmentCtrs));
				} else {
					/*
					 * Panic. No rule to apply. Should not happen...
					 */
					System.err
							.println("[MergedLogALignmentArrayIntoLogAlignmentAlgorithm] No rule to apply. Merge failed. Bailing out.");
					for (int i = 0; i < alignments.size(); i++) {
						if (traceCtr < trace.size()) {
							System.err.println("[MergedLogALignmentArrayIntoLogAlignmentAlgorithm] Trace is not done.");
						}
						if (alignmentCtrs[i] < alignments.get(i).getSize()) {
							System.err.println("[MergedLogALignmentArrayIntoLogAlignmentAlgorithm] Alignment at index "
									+ i + " is not done.");
						}
					}
					toDoCtr = -1;
				}
			}
		}
		/*
		 * Set whether this merged alignment is reliable.
		 */
		mergedAlignment.setReliable(isReliable);

		/*
		 * Set the costs for the merged alignment by accumulating the costs of
		 * the (sub)alignments.
		 * 
		 * Make sure to add the log move costs for activities not covered by any subalignment.
		 */
		double costs = logMoveCosts;
		double maxCosts = logMoveCosts;
		for (TraceAlignment alignment : alignments) {
			costs += alignment.getCosts();
			maxCosts += alignment.getMaxCosts();
		}
		mergedAlignment.setCosts(costs);
		mergedAlignment.setMaxCosts(maxCosts);

		// adding the state information from alignment computation to merged alignment
		double stateCount = 0.0;
		double queuedStates = 0.0;
		double traversedArcs = 0.0;
		for (TraceAlignment alignment: alignments) {
			stateCount += alignment.getStateCount();
			queuedStates += alignment.getQueuedStates();
			traversedArcs += alignment.getTraversedArcs();
		}
		
//		System.out.printf("[%s] No. of generated states: %.2f, "
//				+ "No. of queued states: %.2f, "
//				+ "No. of traversed arcs: %.2f%n", getClass().getSimpleName(), 
//				stateCount, queuedStates, traversedArcs);
		
		mergedAlignment.setStateCount(stateCount);
		mergedAlignment.setQueuedStates(queuedStates);
		mergedAlignment.setTraversedArcs(traversedArcs);

		/*
		 * Return the merged alignment.
		 */
		return mergedAlignment;
	}

	private void addLegalMove(TraceAlignment alignment, Pair<StepTypes, Pair<XEventClass, String>> legalMove) {
		/*
		 * Adds the given legal move to the given alignment.
		 */
		alignment.addLegalMove(legalMove.getFirst(), legalMove.getSecond().getFirst(), legalMove.getSecond()
				.getSecond());
	}

	private Pair<StepTypes, Pair<XEventClass, String>> getBestLogMove(
			Set<Pair<StepTypes, Pair<XEventClass, String>>> logMoves,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters, Set<Integer> activityClusters,
			List<TraceAlignment> alignments, int[] alignmentCtrs) {
		/*
		 * Get a suitable log move.
		 */
		switch (parameters.getStrategy()) {
			case MergeLogAlignmentArrayIntoLogAlignmentParameters.LOWESTCOST : {
				/*
				 * A suitable move is a move with minimal costs. Either the move
				 * is a synchronous move (LMGOOD) or a log move (L). Any
				 * synchronous move has minimal costs. Assumption 1: All
				 * synchronous moves have identical costs Assumption 2: A log
				 * move has more costs than a synchronous moves.
				 */
				for (Pair<StepTypes, Pair<XEventClass, String>> lm : logMoves) {
					if (lm.getFirst() == StepTypes.LMGOOD) {
						return lm;
					}
				}
				break;
			}
			case MergeLogAlignmentArrayIntoLogAlignmentParameters.HIGHESTCOST : {
				/*
				 * A suitable move is a move with maximal costs. Either the move
				 * is a synchronous move (LMGOOD) or a log move (L). Any log
				 * move has maximal costs. Assumption 1: All log moves have
				 * identical costs Assumption 2: A log move has more costs than
				 * a synchronous moves.
				 */
				for (Pair<StepTypes, Pair<XEventClass, String>> lm : logMoves) {
					if (lm.getFirst() == StepTypes.L) {
						return lm;
					}
				}
				break;
			}
			case MergeLogAlignmentArrayIntoLogAlignmentParameters.RANDOM : {
				/*
				 * A suitable move is a random move. First, select an index in
				 * the activityClusters.
				 */
				int i = (new Random()).nextInt(activityClusters.size());
				List<Integer> list = new ArrayList<Integer>(activityClusters);
				/*
				 * Replace the index by the index in the alignments.
				 */
				i = list.get(i);
				/*
				 * Get the next move from this randomly chosen alignment. Need
				 * -1 as we've already advanced the counter.
				 */
				return alignments.get(i).getLegalMoves().get(alignmentCtrs[i] - 1);
			}
			default : {

			}
		}
		/*
		 * A suitable move is the first move.
		 */
		return logMoves.iterator().next();
	}

	private Pair<StepTypes, Pair<XEventClass, String>> getBestVisibleModelMove(
			Set<Pair<StepTypes, Pair<XEventClass, String>>> modelMoves,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters, Set<Integer> activityClusters,
			List<TraceAlignment> alignments, int[] alignmentCtrs) {
		/*
		 * Get a suitable log move.
		 */
		switch (parameters.getStrategy()) {
			case MergeLogAlignmentArrayIntoLogAlignmentParameters.LOWESTCOST : {
				/*
				 * A suitable move is a move with minimal costs. Either the move
				 * is a synchronous move (LMGOOD) or a visible model move
				 * (MREAL). Any synchronous move has minimal costs. Assumption
				 * 1: All synchronous moves have identical costs Assumption 2: A
				 * visible model move has more costs than a synchronous moves.
				 */
				for (Pair<StepTypes, Pair<XEventClass, String>> modelMove : modelMoves) {
					if (modelMove.getFirst() == StepTypes.LMGOOD) {
						return modelMove;
					}
				}
				break;
			}
			case MergeLogAlignmentArrayIntoLogAlignmentParameters.HIGHESTCOST : {
				/*
				 * A suitable move is a move with maximal costs. Either the move
				 * is a synchronous move (LMGOOD) or a visible model move
				 * (MREAL). Any visible model move has maximal costs. Assumption
				 * 1: All visible model moves have identical costs Assumption 2:
				 * A visible model move has more costs than a synchronous moves.
				 */
				for (Pair<StepTypes, Pair<XEventClass, String>> modelMove : modelMoves) {
					if (modelMove.getFirst() == StepTypes.MREAL) {
						return modelMove;
					}
				}
				break;
			}
			case MergeLogAlignmentArrayIntoLogAlignmentParameters.RANDOM : {
				/*
				 * A suitable move is a random move. First, select an index in
				 * the activityClusters.
				 */
				int i = (new Random()).nextInt(activityClusters.size());
				List<Integer> list = new ArrayList<Integer>(activityClusters);
				/*
				 * Replace the index by the index in the alignments.
				 */
				i = list.get(i);
				/*
				 * Get the next move from this randomly chosen alignment. Need
				 * -1 as we've already advanced the counter.
				 */
				return alignments.get(i).getLegalMoves().get(alignmentCtrs[i] - 1);
			}
			default : {

			}
		}
		/*
		 * A suitable move is the first move.
		 */
		return modelMoves.iterator().next();
	}
	
	/*
	 * HV 20180418 Fixed bug: check whether matching log move or sync move, not model move.
	 */
	private boolean isMatchingLogMove(XEventClass activity, Pair<StepTypes, Pair<XEventClass, String>> step) {
		switch (step.getFirst()) {
			case LMGOOD:
				/* Fall thru */
			case L:
				return activity.equals(step.getSecond().getFirst());
			default:
		}
		return false;
	}
}
