package org.processmining.decomposedreplayer.experiments.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.algorithms.replay.impl.RecomposingReplayWithRecomposeStrategyAlgorithm;
import org.processmining.decomposedreplayer.experiments.boot.TestRecomposingReplayWithMergeStrategyBoot;
import org.processmining.decomposedreplayer.experiments.parameters.TestRecomposingReplayWithMergeStrategyParameters;
import org.processmining.decomposedreplayer.experiments.utils.LogImporter;
import org.processmining.decomposedreplayer.models.stats.IterationStats;
import org.processmining.decomposedreplayer.models.stats.IterationStats.Statistic;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters.Type;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

@Plugin(name = "Evaluate Recomposing Replay with Merge", 
		categories = { PluginCategory.ConformanceChecking }, 
		parameterLabels = { "Parameters" }, 
		returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class TestRecomposingReplayWithMergeStrategyPlugin implements HTMLToString {

	private static final Logger LOGGER = Logger.getLogger(TestRecomposingReplayWithMergeStrategyBoot.class.getName());
	private StringBuffer buf = new StringBuffer();
	private static ImportPlugin logImporter = new OpenLogFilePlugin();
	private static ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Jonathan Lee", email = "walee@uc.cl", pack = "JonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestRecomposingReplayWithMergeStrategyParameters parameters) {
		LOGGER.info("At test plugin...");
		long start = System.nanoTime();
		
		XLog log;
		AcceptingPetriNet apn;
		try {
			/*
			 * Import the event log.
			 */
			LOGGER.info("Importing " + parameters.logPath);
			log = LogImporter.importFile(parameters.logPath);
			/*
			 * Import the Accepting Petri net discovered for the corresponding noise-free event log.
			 */
			LOGGER.info("Importing " + parameters.modelPath);
			apn = (AcceptingPetriNet) netImporter.importFile(context, parameters.modelPath);
		
			TestRecomposingReplayWithMergeStrategyPlugin plugin = new TestRecomposingReplayWithMergeStrategyPlugin(context, log, apn, parameters);
			
			long end = System.nanoTime();
			long taken = (end - start) / 1000000;
			LOGGER.info("Running test recomposing plugin took " + taken + " ms.");
			
			return plugin;
			
		} catch (Exception e) {
			
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
		
		}
		
		return null;
	}
	
	private static List<String> getCaseIds(SyncReplayResult alignment, XLog log) {
		List<String> caseIds = new ArrayList<>();
		for (int index: alignment.getTraceIndex()) {
			XTrace trace = log.get(index);
			String caseId = trace.getAttributes().get("concept:name").toString();
			caseIds.add(caseId);
		}
		return caseIds;
	}

	private static boolean isMove(StepTypes stepType) {
		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L || stepType == StepTypes.MREAL
				|| stepType == StepTypes.MINVI;
	}
	
	private static List<String> toMoves(SyncReplayResult result, TransEvClassMapping mapping) {
		
		List<String> moves = new ArrayList<>();
		String move = "";
		
		for (int i = 0; i < result.getStepTypes().size(); ++i) {
			StepTypes stepType = result.getStepTypes().get(i);
			Object nodeInstance = result.getNodeInstance().get(i);
			
			if (isMove(stepType)) {
				XEventClass eventClass = null;
				
				if (nodeInstance instanceof XEventClass) {
					eventClass = (XEventClass) nodeInstance;
				} else if (nodeInstance instanceof Transition) {
					eventClass = mapping.get(nodeInstance);
				} else {
					System.err.println("Unknown node instance: " + nodeInstance);
				}
				
				// add as a string
				if (stepType == StepTypes.LMGOOD) {
					move = "LMGOOD" + "," + eventClass.getId() + "," + eventClass.getId();
				} else if (stepType == StepTypes.L) {
					move = "L" + "," + eventClass.getId() + ",>>";
				} else if (stepType == StepTypes.MREAL) {
					move = "MREAL" + "," + ">>," + eventClass.getId();
				} else if (stepType == StepTypes.MINVI) {
					move = "MINVI,>>,invis";
				}
				
				moves.add(move);
			}
		}
		
		return moves;
		
	}
	
	private static void printCaseIds(Set<SyncReplayResult> alignments, XLog log, String fp) {
		// valid alignments
		File file = new File(fp);
		PrintStream stream = null;
		
		try {
			stream = new PrintStream(file);
			stream.println("CaseIds");
			
			for (SyncReplayResult alignment: alignments) {
				// print list of caseids
				List<String> caseIds = TestRecomposingReplayWithMergeStrategyPlugin.getCaseIds(alignment, log);
				for (int j = 0; j < caseIds.size(); ++j) {
					stream.println(caseIds.get(j));
				}
			}
			
		} catch (IOException ioe) {
			System.out.println("Cannot write to " + fp);
			ioe.printStackTrace();
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	public TestRecomposingReplayWithMergeStrategyPlugin(final PluginContext context, final XLog log, final AcceptingPetriNet apn,
			final TestRecomposingReplayWithMergeStrategyParameters parameters) {
		try {
			RecomposingReplayParameters replayParameters = new RecomposingReplayParameters(log, apn);
			
			replayParameters.setGlobalDuration(parameters.globalDuration);
			replayParameters.setLocalDuration(parameters.localDuration);
			replayParameters.setMoveOnLogCosts(parameters.moveOnLogCosts);
			replayParameters.setMoveOnModelCosts(parameters.moveOnModelCosts);
			replayParameters.setIntervalRelative(parameters.intervalRelative);
			replayParameters.setIntervalAbsolute(parameters.intervalAbsolute);
			replayParameters.setMaxConflicts(parameters.maxConflicts);
			replayParameters.setAlignmentPercentage(parameters.alignmentPercentage);
			replayParameters.setNofIterations(parameters.nofIterations);
			replayParameters.setPlanB(false);
			
			switch (parameters.algorithmType) {
				case TestRecomposingReplayWithMergeStrategyParameters.ASTAR:
					replayParameters.setAlgorithmType(Type.ASTAR);
					break;
				case TestRecomposingReplayWithMergeStrategyParameters.INC0:
					replayParameters.setAlgorithmType(Type.INC0);
					break;
				case TestRecomposingReplayWithMergeStrategyParameters.INC3:
					replayParameters.setAlgorithmType(Type.INC3);
					break;
				case TestRecomposingReplayWithMergeStrategyParameters.INC:
					replayParameters.setAlgorithmType(Type.INC);
					break;
				case TestRecomposingReplayWithMergeStrategyParameters.INC_PLUS:
					replayParameters.setAlgorithmType(Type.INC_PLUS);
					break;
				default:
					System.out.println("Do not recognize algorithm type: " + parameters.algorithmType);
					System.exit(1);;
			}
			
			switch (parameters.debug) {
				case TestRecomposingReplayWithMergeStrategyParameters.STATS:
					replayParameters.setDebug(Debug.STATS);
					break;
				case TestRecomposingReplayWithMergeStrategyParameters.DOT:
					replayParameters.setDebug(Debug.DOT);
					break;
				default:
					System.out.println("Do not recognize debug type: " + parameters.debug);
					System.exit(1);
			}
			
			replayParameters.setMoveSort(parameters.moveSort);
			replayParameters.setQueueSort(parameters.queueSort);
			replayParameters.setPreferExact(parameters.preferExact);
			replayParameters.setnThreads(parameters.nThreads);
			replayParameters.setUseInt(parameters.useInt);
			replayParameters.setOutputDir(parameters.resultDir);
			replayParameters.setTimeoutPerTraceInSecs(parameters.timeoutPerTraceInSecs);
			replayParameters.setMaximumNumberOfStates(parameters.maximumNumberOfStates);
			replayParameters.setCostUpperBound(parameters.costUpperBound);
			replayParameters.setPartiallyOrderEvents(parameters.partiallyOrderEvents);
			replayParameters.setPreProcessUsingPlaceBasedConstraints(parameters.preProcessUsingPlaceBasedConstraints);
			replayParameters.setInitialSplits(parameters.initialSplits);
			replayParameters.setPrintAlignments(parameters.printAlignments);

			if (parameters.maximumNumberOfStates < 0) 
				replayParameters.setMaximumNumberOfStates(Integer.MAX_VALUE);
			if (parameters.costUpperBound < 0)
				replayParameters.setCostUpperBound(Integer.MAX_VALUE);
			
			// set unsplittable activity list
			Set<XEventClass> unsplittableActivities = new HashSet<>();
			final XLogInfo info = XLogInfoFactory.createLogInfo(log, log.getClassifiers().get(0));
			unsplittableActivities.addAll(info.getEventClasses().getClasses());
			
			// it seems that XLogInfo creates all possible combinations of concept:name and 
			// lifecycle:transition, which means that in the case where there are no such combination
			// in the net, it will cause null exception when we try creating decompositions involving 
			// these non-existent activities
			// Solution: Check that all unsplittableActivities can be mapped to a transition
			Set<XEventClass> toRemove = new HashSet<>();
			for (XEventClass activity: unsplittableActivities) {
				if (!replayParameters.getMapping().containsValue(activity))
					toRemove.add(activity);
			}
			unsplittableActivities.removeAll(toRemove);
			
			Set<XEventClass> initialDecompositionActivities = new HashSet<>();
			for (XEventClass activity: info.getEventClasses().getClasses()) {
				if (parameters.initialDecompositionSet.contains(activity.getId())) {
					// add initial decomposition activity
					initialDecompositionActivities.add(activity);
					LOGGER.info("Add initial decomposition activity: " + activity.getId());
				}
			}
			
			if (!initialDecompositionActivities.isEmpty()) {
				unsplittableActivities.removeAll(initialDecompositionActivities);
				replayParameters.setUnsplittableActivities(unsplittableActivities);
			} 
			
			LOGGER.info(String.format("GlobalDuration: %s \nLocalDuration: %s \nMoveOnLogCosts: %s \nMoveOnModelCosts: %s "
					+ "\nIntervalRelative: %s \nIntervalAbsolute: %s \nMaxConflicts: %s \nAlignmentPercentage: %s "
					+ "\nNofIterations: %s", replayParameters.getGlobalDuration(), replayParameters.getLocalDuration(),
					replayParameters.getMoveOnLogCosts(), replayParameters.getMoveOnModelCosts(), 
					replayParameters.getIntervalRelative(), replayParameters.getIntervalAbsolute(),
					replayParameters.getMaxConflicts(), replayParameters.getAlignmentPercentage(),
					replayParameters.getNofIterations()));
			
			RecomposingReplayWorkspace workspace = new RecomposingReplayWorkspace(log, replayParameters);
			
			long time = -System.nanoTime();
			
			// create replayer and replay
			RecomposingReplayWithRecomposeStrategyAlgorithm replayer = new RecomposingReplayWithRecomposeStrategyAlgorithm(
					context, log, apn, workspace, replayParameters, parameters.recomposeStrategy, 
					parameters.logCreationStrategy);
			PNRepResult repResult = replayer.apply();
			time += System.nanoTime();
			time /= 1000000;
			
			LOGGER.info("Finished recomposing replay in " + time + " ms.");
			LOGGER.info(parameters.toString());
			
			
			//----------------------------Recording experiment results-----------------------------------//
			int nofTraces = repResult.size();
			int nofAligned = workspace.alignments.size();
			int nofToAlign = workspace.openAlignments.size();
			int nofRejected = workspace.pseudoAlignments.size();
			// getting the number of recomposition steps taken
			int nofRecompSteps = workspace.nofIterations;
			
			double sumLoCosts = 0;
			double sumHiCosts = 0;
			double sumMaxCosts = 0;
			int sumWeight = 0;
			
			// get the trace indices of the rejected alignments
			XConceptExtension extension = (XConceptExtension) XExtensionManager.instance().getByName("concept");
			for (SyncReplayResult rejected: workspace.pseudoAlignments) {

				// print out the indexes of the rejected alignments
				
				LOGGER.info("Rejected alignments trace indexes: " + rejected.getTraceIndex());
				for (int index: rejected.getTraceIndex()) {
					// print the trace concept:name
					XTrace trace = log.get(index);
					LOGGER.info("Rejected trace concept:name: " + trace.getAttributes().get("concept:name").toString());
				}

			}
			
			//-------------------------Write out statistics per recomposition iteration----------------------------------------------//
			
			// output the statistics per iteration
			String statsFp = "prom-iter-stats.csv";
			statsFp = parameters.resultDir + File.separator + statsFp;
			File statsFile = new File(statsFp);
			PrintStream streamStats = null;

			try {
				streamStats = new PrintStream(statsFile);
				
				if (!replayer.getPerformanceStats().isEmpty()) {
					Iterator<IterationStats> it = replayer.getPerformanceStats().iterator();
					List<Statistic> keys = new LinkedList<>();
					IterationStats stats = it.next();
					
					stats.writeToStream(streamStats, true, keys);
					
					while (it.hasNext()) {
						stats = it.next();
						stats.writeToStream(streamStats, false, keys);
					}
				}
			} catch (IOException ioe) {
				
				System.out.println("Cannot write to " + statsFp);
				ioe.printStackTrace();
			
			} finally {
			
				if (streamStats != null)
					streamStats.close();
			
			}
			
			//-------------------------Write the alignments to file and getting aggregate results--------------------------------------//
			// trace alignment folder
			String alignResultDir = parameters.resultDir + File.separator + "alignments";
			File dir = new File (alignResultDir);
			if (!(dir.exists() || dir.isDirectory()))
				dir.mkdirs();
			
			String validFp = parameters.resultDir + File.separator + "valid.csv";
			String toAlignFp = parameters.resultDir + File.separator + "to-align.csv";
			String rejectedFp = parameters.resultDir + File.separator + "rejected.csv";
			
			TestRecomposingReplayWithMergeStrategyPlugin.printCaseIds(workspace.alignments, log, validFp);
			TestRecomposingReplayWithMergeStrategyPlugin.printCaseIds(workspace.openAlignments, log, toAlignFp);
			TestRecomposingReplayWithMergeStrategyPlugin.printCaseIds(workspace.pseudoAlignments, log, rejectedFp);
			
//			// alignment level info
//			String alignInfoFp = "alignment-info.csv";
//			alignInfoFp = parameters.resultDir + File.separator + parameters.iteration + File.separator + alignInfoFp;
//			List<String> alignInfo = new LinkedList<>();
//			String header = "time, generated_state, queued_state, traversed_arc, fitness_cost_lo, fitness_cost_hi, trace_length, case_id";
//			alignInfo.add(header);
			
			// print out the state information
			double logStateCount = 0.0;
			double logQueuedStates = 0.0;
			double logTraversedArcs = 0.0;
			double avgLogStateCount = 0.0;
			double avgLogQueuedStates = 0.0;
			double avgLogTraversedArcs = 0.0;
			int logSize = 0;
			double totalAlignTime = 0;
			
			TransEvClassMapping mapping = replayParameters.getMapping();
			int i = 0;
			
			for (SyncReplayResult alignment : repResult) {
				double costLo = alignment.getInfo().get("Raw Fitness Cost Lo");
				double costHi = alignment.getInfo().get("Raw Fitness Cost Hi");
				double maxCosts = alignment.getInfo().get("Raw Fitness Cost Max");
				double stateCount = alignment.getInfo().get(PNRepResult.NUMSTATEGENERATED);
				double queuedStates = alignment.getInfo().get(PNRepResult.QUEUEDSTATE);
				double traversedArcs = alignment.getInfo().get(PNRepResult.TRAVERSEDARCS);
				double alignTime = alignment.getInfo().get(PNRepResult.TIME);
				int nofCases = alignment.getTraceIndex().size();
				int traceLength = 0;
				String caseId0 = "";
				
				// update aggregate statistics
				logSize += nofCases;
				sumLoCosts += nofCases * costLo;
				sumMaxCosts += nofCases * maxCosts;
				sumHiCosts += nofCases * costHi;
				sumWeight += nofCases;
				logStateCount += stateCount * nofCases;
				logQueuedStates += queuedStates * nofCases;
				logTraversedArcs += traversedArcs * nofCases;
				totalAlignTime += alignTime;
				
				// write out the alignment
				String alignmentFp = i + ".csv";
				alignmentFp = alignResultDir + File.separator + alignmentFp;
				i += 1;
				
				// get caseids
				List<String> caseIds = new LinkedList<>();
				// Representative caseid that was actually aligned and used by the related duplicated trace ids
				String repCaseId = ""; 
				Iterator<Integer> iterator = alignment.getTraceIndex().iterator();
				
				int index = iterator.next();
				XTrace trace = log.get(index);
				String caseId = trace.getAttributes().get("concept:name").toString();
				caseIds.add(caseId);
				repCaseId = caseId;
				
				while (iterator.hasNext()) {
					index = iterator.next();
					trace = log.get(index);
					caseId = trace.getAttributes().get("concept:name").toString();
					caseIds.add(caseId);
				}
				
				File file = new File(alignmentFp);
				PrintStream stream = null;
				
				try {
					stream = new PrintStream(file);
					
					// print exitcode
					stream.println("Exitcode");
					if (alignment.getInfo().containsKey(Replayer.TRACEEXITCODE)) {
						stream.println(alignment.getInfo().get(Replayer.TRACEEXITCODE));
					} else {
						stream.println(alignment.isReliable());
					}
					
					// print representative caseid
					stream.println("\nRepresentative caseId");
					stream.println(repCaseId);
					
					stream.println("\nCaseIds");
					stream.print(caseIds.get(0));
					for (int j = 1; j < caseIds.size(); ++j) {
						stream.print("," + caseIds.get(j));
					}
					
					// print moves
					List<String> moves = TestRecomposingReplayWithMergeStrategyPlugin.toMoves(alignment, mapping);
					stream.println("\nMove type,Log,Model");
					for (int j = 0; j < moves.size(); ++j) {
						stream.println(moves.get(j));
					}
					
				} catch (IOException ioe) {
					System.out.println("Cannot write to " + alignmentFp);
					ioe.printStackTrace();
				} finally {
					if (stream != null)
						stream.close();
				}
								
				// caseids related to the alignment				
//				String caseIds = "";
//				for (int index: alignment.getTraceIndex()) {
//					XTrace trace = log.get(index);
//					traceLength = trace.size();
//					String caseId = trace.getAttributes().get("concept:name").toString();
//					if (caseIds.equals(""))
//						caseIds = caseIds + caseId;
//					else
//						caseIds = caseIds + ";" + caseId;
//					
//					// get the first case id
//					if (caseId0.equals(""))
//						caseId0 = caseId;
//				}
				
				// alignment info
//				String row = alignTime + ", " + stateCount + ", " + queuedStates + ", " + 
//						traversedArcs + ", " + costLo + ", " + costHi + ", " + traceLength + ", " + caseIds;
//				alignInfo.add(row);
				
				// get the caseid of the first associated trace
//				LOGGER.info(String.format("Alignment of caseid %s: "
//						+ "No. of generated states: %.2f, "
//						+ "No. of queued states: %.2f, "
//						+ "No. of traversed arcs: %.2f%n", 
//						caseId0, stateCount, queuedStates, traversedArcs));
				
			}
			
//			writer.writeStringListToFile(alignInfo, alignInfoFp, true);
			
			if (logSize > 0) {
				avgLogStateCount = logStateCount / logSize;
				avgLogQueuedStates = logQueuedStates / logSize;
				avgLogTraversedArcs = logTraversedArcs / logSize;
			}
			
			LOGGER.info(String.format("(Log level average) No. of generated states: %.2f, "
					+ "No. of queued states: %.2f, No. of traversed arcs: %.2f%n", 
					logStateCount, logQueuedStates, logTraversedArcs));
			
			double costLo = sumLoCosts / sumWeight;
			double costHi = sumHiCosts / sumWeight;
			double percLo = (1.0 - sumHiCosts / sumMaxCosts);
			double percHi = (1.0 - sumLoCosts / sumMaxCosts);

			// Save results as a CSV format
			buf.append(parameters.iteration + ", ");
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.modelPath + ", ");
			// log and model names
			buf.append(parameters.log + ", ");
			buf.append(parameters.model + ", ");
			// not monolithic
			buf.append(false + ", ");
			// type of decomposition
			buf.append(parameters.decomposition + ", ");
			buf.append(parameters.recomposeStrategy + ", ");
			buf.append(parameters.logCreationStrategy + ", ");
			buf.append(parameters.preferBorderTransitions + ", ");
			buf.append(parameters.addConflictOnlyOnce + ", ");
			buf.append(parameters.useHideAndReduceAbstraction + ", ");
			buf.append(replayParameters.getGlobalDuration() + ", ");
			buf.append(replayParameters.getLocalDuration() + ", ");
			buf.append(replayParameters.getMoveOnLogCosts() + ", ");
			buf.append(replayParameters.getMoveOnModelCosts() + ", ");
			buf.append(replayParameters.getIntervalRelative() + ", ");
			buf.append(replayParameters.getIntervalAbsolute() + ", ");
			buf.append(replayParameters.getMaxConflicts() + ", ");
			buf.append(replayParameters.getAlignmentPercentage() + ", ");
			buf.append(replayParameters.getNofIterations() + ", ");
			buf.append(costLo + ", ");
			buf.append(costHi + ", ");
			buf.append(percLo + ", ");
			buf.append(percHi + ", ");
			buf.append(nofAligned + ", ");
			buf.append(nofToAlign + ", ");
			buf.append(nofRejected + ", ");
			buf.append(nofTraces + ", ");
			buf.append(nofRecompSteps + ", ");
			buf.append(time + ", ");
			buf.append(totalAlignTime + ", ");
			buf.append(logStateCount + ", ");
			buf.append(logQueuedStates + ", ");
			buf.append(logTraversedArcs + ", ");
			buf.append(avgLogStateCount + ", ");
			buf.append(avgLogQueuedStates + ", ");
			buf.append(avgLogTraversedArcs + "");
		} catch (Exception e) {
			System.out.println("There is an exception: " + e.getMessage());
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.iteration + ", ");
			buf.append("na, na, na, na, na, na, na, na, na, na, na, na, na, "
					+ "na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na");
		}
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		// to a comma separated string (csv) 
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		System.out.println(buf);
		buffer.append(buf);
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		buffer.append("\n");
		return buffer.toString();
	}
	
	
}
