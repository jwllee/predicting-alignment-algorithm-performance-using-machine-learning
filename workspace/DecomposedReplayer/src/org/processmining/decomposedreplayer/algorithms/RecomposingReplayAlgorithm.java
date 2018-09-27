package org.processmining.decomposedreplayer.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterHundredReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericReplaceReduceReplayConfiguration;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.algorithms.ConvertLogAlignmentToReplayResultAlgorithm;
import org.processmining.logalignment.utils.AlignmentInfoLabels;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class RecomposingReplayAlgorithm {

	private double emptyTraceCosts = -1.0;
	private long millis;

	public PNRepResult apply(PluginContext context, XLog log, AcceptingPetriNet net,
			RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters) {

		millis = -System.currentTimeMillis();
		while (true) {
			workspace.nofIterations++; // Signal a new iteration.

			parameters.displayMessage(context, "Traces to align: " + workspace.log.size() + ".");
			/*
			 * Do the decomposed replay, using the unsplittable activities in
			 * the parameters to guide the decomposition. Basically, this is a
			 * maximal decomposition after which all unsplittable activities
			 * have been merged back.
			 */
			DecomposedReplayParameters pars = new DecomposedReplayParameters(log, net,
					workspace.parameters.getClassifier());
			DecomposedReplayConfiguration config = new DecomposedGenericFilterHundredReplayConfiguration();
			if (parameters.isUseHideAndReduceAbstraction()) {
				config = new DecomposedGenericReplaceReduceReplayConfiguration();
			}
			pars.setUnsplittableActivities(workspace.parameters.getUnsplittableActivities());
			pars.setMoveOnLogCosts(parameters.getMoveOnLogCosts());
			pars.setMoveOnModelCosts(parameters.getMoveOnModelCosts());
			pars.setTryConnections(false);
			pars.setAddConflictOnlyOnce(parameters.isAddConflictOnlyOnce());
			pars.setPreferBorderTransitions(parameters.isPreferBorderTransitions());

			/*
			 * The next part is basically a hack. For some of the constructed
			 * sublogs, not all activities may be covered anymore by that
			 * sublog. As a result, the corresponding transitions will be set to
			 * invisible. This is clearly not desirable, as it allows for
			 * optimal alignments which are not optimal in the original setting.
			 * For this reason, we set such transition to move-on-model.
			 */
			//		for (Transition transition : net.getNet().getTransitions()) {
			//			if (pars.getMapping().get(transition).equals(XUtils.INVISIBLEACTIVITY) &&
			//					!parameters.getMapping().get(transition).equals(XUtils.INVISIBLEACTIVITY)) {
			//				pars.getMapping().put(transition, XUtils.MOVEONMODELACTIVITY);
			//			}
			//		}
			pars.setMapping(parameters.getMapping());

			PNRepResult logAlignment = config.apply(context, workspace.log, net, pars, null);

			Map<XEventClass, Double> logConflicts = new HashMap<XEventClass, Double>();

			for (SyncReplayResult openAlignment : workspace.openAlignments) {
				addConflicts(logConflicts, getConflicts(openAlignment));
			}

			/*
			 * Check the result of the decomposed replay.
			 */
			for (SyncReplayResult traceAlignment : logAlignment) {
				/*
				 * Restore trace indices in the original log.
				 */
				SortedSet<Integer> traceIndices = new TreeSet<Integer>();
				for (int index : traceAlignment.getTraceIndex()) {
					traceIndices.add(workspace.indexMap.get(index));
				}
				traceAlignment.setTraceIndex(traceIndices);

				Map<XEventClass, Double> traceConflicts = getConflicts(traceAlignment);
				if (!traceAlignment.isReliable()) {
					/*
					 * This trace has failed to replay reliably. Assumption is
					 * that this will fail again in the next repay. Therefore,
					 * reject.
					 */
					workspace.pseudoAlignments.add(traceAlignment);
				} else if (traceConflicts.isEmpty()) {
					/*
					 * We have TBA, hence an alignment.
					 */
					workspace.alignments.add(traceAlignment);
				} else if (workspace.parameters.reject(traceAlignment, traceConflicts)) {
					/*
					 * This trace has been rejected, hence a pseudo-alignment.
					 */
					workspace.pseudoAlignments.add(traceAlignment);
				} else {
					addConflicts(logConflicts, traceConflicts);
					/*
					 * This trace remains open.
					 */
					workspace.openAlignments.add(traceAlignment);
				}
			}

			System.out.println("[RecomposingReplayAlgorithm] accepted=" + workspace.alignments.size() + ", rejected="
					+ workspace.pseudoAlignments.size() + ", toalign=" + workspace.openAlignments.size() + ".");

			Set<SyncReplayResult> allTraceAlignments = new HashSet<SyncReplayResult>(workspace.alignments);
			allTraceAlignments.addAll(workspace.pseudoAlignments);
			allTraceAlignments.addAll(workspace.openAlignments);

			if (workspace.openAlignments.size() == 0) {
				/*
				 * No open traces remain. We're done.
				 */
				return wrap(context, allTraceAlignments, log, net, workspace, parameters);
			}

			if (workspace.parameters.stop(workspace,
					getFitnessInterval(context, allTraceAlignments, log, net, workspace, parameters))) {
				/*
				 * Stop. We're done, but need to take the open alignments into
				 * account.
				 */
				return wrap(context, allTraceAlignments, log, net, workspace, parameters);
			}

			Set<XEventClass> scapegoatActivities = new HashSet<XEventClass>();
			/*
			 * Create a new set of activities-to-merge based on the conflicts.
			 */
			Double max = 0.0;
			System.out.println("[RecomposingReplayAlgorithm] Conflicts: " + logConflicts);
			for (XEventClass activity : logConflicts.keySet()) {
				if (logConflicts.get(activity) > max) {
					max = logConflicts.get(activity);
				}
			}
			for (XEventClass activity : logConflicts.keySet()) {
				if (logConflicts.get(activity) == max) {
					scapegoatActivities.add(activity);
					context.getProgress().inc();
				}
			}
			workspace.parameters.getUnsplittableActivities().addAll(scapegoatActivities);
			System.out.println("[RecomposingReplayAlgorithm] Joining current subnets on activity set "
					+ scapegoatActivities);

			/*
			 * Create a new log from the open alignments.
			 */
			workspace.log = workspace.factory.createLog();
			/*
			 * The new log will need the same classifier. Do not bother about
			 * attributes.
			 */
			workspace.log.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
			workspace.log.getClassifiers().addAll(log.getClassifiers());
			Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
			Set<SyncReplayResult> openAlignments = new HashSet<SyncReplayResult>();
			for (SyncReplayResult openAlignment : workspace.openAlignments) {
				Set<XEventClass> conflictingActivities = getConflicts(openAlignment).keySet();
				conflictingActivities.retainAll(scapegoatActivities);
				if (!conflictingActivities.isEmpty()) {
					for (int index : openAlignment.getTraceIndex()) {
						/*
						 * Remember the index to the trace in the original log.
						 */
						indexMap.put(workspace.log.size(), index);
						workspace.log.add(log.get(index));
					}
				} else {
					openAlignments.add(openAlignment);
				}
			}
			workspace.openAlignments = openAlignments;
			workspace.indexMap = indexMap;
		}
	}

	private Map<XEventClass, Double> getConflicts(SyncReplayResult result) {
		Map<XEventClass, Double> conflicts = new HashMap<XEventClass, Double>();
		for (String label : result.getInfo().keySet()) {
			if (label.startsWith(ConvertLogAlignmentToReplayResultAlgorithm.CONFLICTPREFIX)
					&& result.getInfo().get(label) > 0.0) {
				conflicts.put(
						new XEventClass(label.substring(ConvertLogAlignmentToReplayResultAlgorithm.CONFLICTPREFIX
								.length()), 0), result.getInfo().get(label));
			}
		}
		return conflicts;
	}

	private void addConflicts(Map<XEventClass, Double> logConflicts, Map<XEventClass, Double> traceConflicts) {
		for (XEventClass activity : traceConflicts.keySet()) {
			logConflicts.put(activity, traceConflicts.get(activity)
					+ (logConflicts.containsKey(activity) ? logConflicts.get(activity) : 0.0));
		}
	}

	private PNRepResult wrapOld(PluginContext context, Set<SyncReplayResult> alignments, XLog log,
			AcceptingPetriNet net, RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters) {
		PNRepResult result = new PNRepResultImpl(alignments);
		IPNReplayAlgorithm replayer = new PetrinetReplayerWithILP();
		Set<XEventClass> activities = new HashSet<XEventClass>();
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		context.getProgress().setValue(info.getEventClasses().size());
		activities.addAll(info.getEventClasses().getClasses());
		activities.add(XUtils.MOVEONMODELACTIVITY);
		IPNReplayParameter replayerParameters = createReplayParameters(activities, XUtils.INVISIBLEACTIVITY, net);
		context.addConnection(new PNRepResultAllRequiredParamConnection("DaC recomposed alignment", net.getNet(), log,
				parameters.getMapping(), replayer, replayerParameters, result));

		emptyTraceCosts = getEmptyTraceCost(context, log, net, workspace, parameters);

		double sumLoCo = 0, sumHiCo = 0, sumLoFi = 0, sumHiFi = 0;
		int sumWeight = 0;
		for (SyncReplayResult alignment : result) {
			double loCo = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
			double hiCo = 0;
			double maxLoCo, maxHiCo;
			if (parameters.isPlanB()) {
				maxLoCo = log.get(alignment.getTraceIndex().first()).size() * parameters.getMoveOnLogCosts();
				maxHiCo = emptyTraceCosts + maxLoCo;
			} else {
				maxLoCo = emptyTraceCosts + log.get(alignment.getTraceIndex().first()).size()
						* parameters.getMoveOnLogCosts();
				maxHiCo = maxLoCo;
			}
			double loFi = 0, hiFi = 1 - loCo / maxHiCo;
			if (alignment.isReliable() && getConflicts(alignment).isEmpty()) {
				hiCo = loCo;
				loFi = 1 - loCo / maxLoCo;
			} else {
				hiCo = maxHiCo;
			}
			alignment.getInfo().put("Raw Fitness Cost Lo", loCo);
			alignment.getInfo().put("Raw Fitness Cost Hi", hiCo);
			alignment.getInfo().put("Maximal Raw Fitness Cost Lo", maxLoCo);
			alignment.getInfo().put("Maximal Raw Fitness Cost Hi", maxHiCo);
			alignment.getInfo().put("Raw Fitness Perc Lo", 100 * loFi);
			alignment.getInfo().put("Raw Fitness Perc Hi", 100 * hiFi);
			int weight = alignment.getTraceIndex().size();
			sumLoCo += loCo * weight;
			sumHiCo += hiCo * weight;
			sumLoFi += loFi * weight;
			sumHiFi += hiFi * weight;
			sumWeight += weight;
		}
		System.out.println("[RecomposingReplayAlgorithm] Replay costs interval = [" + sumLoCo / sumWeight + ","
				+ sumHiCo / sumWeight + "].");
		System.out.println("[RecomposingReplayAlgorithm] Fitness interval = [" + sumLoFi / sumWeight + "," + sumHiFi
				/ sumWeight + "].");
		millis += System.currentTimeMillis();
		System.out.println("[RecomposingReplayAlgorithm] Recomposing replay algorithm took " + millis
				+ " milliseconds.");
		return result;
	}

	private PNRepResult wrap(PluginContext context, Set<SyncReplayResult> alignments, XLog log, AcceptingPetriNet net,
			RecomposingReplayWorkspace workspace, RecomposingReplayParameters parameters) {
		PNRepResult result = new PNRepResultImpl(alignments);
		IPNReplayAlgorithm replayer = new PetrinetReplayerWithILP();
		Set<XEventClass> activities = new HashSet<XEventClass>();
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		context.getProgress().setValue(info.getEventClasses().size());
		activities.addAll(info.getEventClasses().getClasses());
		activities.add(XUtils.MOVEONMODELACTIVITY);
		IPNReplayParameter replayerParameters = createReplayParameters(activities, XUtils.INVISIBLEACTIVITY, net);
		context.addConnection(new PNRepResultAllRequiredParamConnection("DaC recomposed alignment", net.getNet(), log,
				parameters.getMapping(), replayer, replayerParameters, result));

		emptyTraceCosts = getEmptyTraceCost(context, log, net, workspace, parameters);
		result.getInfo().put("Min Model-Move Cost", emptyTraceCosts);
		Object rawFitnessCost = result.getInfo().get(PNRepResult.RAWFITNESSCOST);
		Object maxMoveLogCost = result.getInfo().get(PNRepResult.MAXMOVELOGCOST);
		if (rawFitnessCost != null && rawFitnessCost instanceof Double && maxMoveLogCost != null && maxMoveLogCost instanceof Double) {
			result.getInfo().put("Log Fitness", 1.0 - ((Double) rawFitnessCost/(emptyTraceCosts + (Double) maxMoveLogCost)));
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
				if (getConflicts(alignment).isEmpty()) {
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
		millis += System.currentTimeMillis();
		System.out.println("[RecomposingReplayAlgorithm] Recomposing replay algorithm took " + millis
				+ " milliseconds.");
		return result;
	}

	private Pair<Double, Double> getFitnessIntervalOld(PluginContext context, Set<SyncReplayResult> alignments,
			XLog log, AcceptingPetriNet net, RecomposingReplayWorkspace workspace,
			RecomposingReplayParameters parameters) {
		emptyTraceCosts = getEmptyTraceCost(context, log, net, workspace, parameters);
		double sumLoFi = 0, sumHiFi = 0;
		int sumWeight = 0;
		for (SyncReplayResult alignment : alignments) {
			double loCo = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
			double hiCo = 0;
			double maxLoCo, maxHiCo;
			if (parameters.isPlanB()) {
				maxLoCo = log.get(alignment.getTraceIndex().first()).size() * parameters.getMoveOnLogCosts();
				maxHiCo = emptyTraceCosts + maxLoCo;
			} else {
				maxLoCo = emptyTraceCosts + log.get(alignment.getTraceIndex().first()).size()
						* parameters.getMoveOnLogCosts();
				maxHiCo = maxLoCo;
			}
			double loFi = 0, hiFi = 1 - loCo / maxHiCo;
			if (getConflicts(alignment).isEmpty()) {
				hiCo = loCo;
				loFi = 1 - loCo / maxLoCo;
			} else {
				hiCo = maxHiCo;
			}
			int weight = alignment.getTraceIndex().size();
			sumLoFi += loFi * weight;
			sumHiFi += hiFi * weight;
			sumWeight += weight;
		}
		return new Pair<Double, Double>(sumLoFi / sumWeight, sumHiFi / sumWeight);
	}

	private Pair<Double, Double> getFitnessInterval(PluginContext context, Set<SyncReplayResult> alignments, XLog log,
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
			if (getConflicts(alignment).isEmpty()) {
				sumHiCosts += nofTraces * costs;
			} else {
				sumHiCosts += nofTraces * maxCosts;
			}
		}
		return new Pair<Double, Double>(1.0 - sumHiCosts / sumMaxCosts, 1.0 - sumLoCosts / sumMaxCosts);
	}

	private IPNReplayParameter createReplayParameters(Collection<XEventClass> activities,
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

	private double getEmptyTraceCost(PluginContext context, XLog log, AcceptingPetriNet net,
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
	private double getEmptyTraceCostPlanB(PluginContext context, XLog log, AcceptingPetriNet net,
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
}
