package org.processmining.decomposedreplayer.configurations.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinetdecomposer.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;
import org.processmining.acceptingpetrinetdecomposer.plugins.DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionReplaceNoReduceStrategy;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.activityclusterarraycreator.parameters.ModifyActivityClusterArrayParameters;
import org.processmining.activityclusterarraycreator.plugins.ConvertCausalActivityGraphToActivityClusterArrayPlugin;
import org.processmining.activityclusterarraycreator.plugins.ModifyActivityClusterArrayPlugin;
import org.processmining.activityclusterarrayextractor.parameters.ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters;
import org.processmining.activityclusterarrayextractor.plugins.ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.causalactivitygraphcreator.plugins.ConvertCausalActivityMatrixToCausalActivityGraphPlugin;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixcreator.creators.impl.MatrixPNCreator;
import org.processmining.causalactivitymatrixcreator.parameters.CreateFromAcceptingPetriNetParameters;
import org.processmining.causalactivitymatrixcreator.plugins.CreateFromAcceptingPetriNetPlugin;
import org.processmining.causalactivitymatrixminer.miners.impl.HAFMidiMatrixMiner;
import org.processmining.causalactivitymatrixminer.parameters.DiscoverFromEventLogParameters;
import org.processmining.causalactivitymatrixminer.plugins.DiscoverFromEventLogPlugin;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.algorithms.ConvertLogAlignmentToReplayResultAlgorithm;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.ConvertLogAlignmentToReplayResultParameters;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.logalignment.parameters.CreateReplayCostFactorFromActivityClusterArrayParameters;
import org.processmining.logalignment.parameters.FilterLogAlignmentParameters;
import org.processmining.logalignment.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;
import org.processmining.logalignment.plugins.ConvertLogAlignmentToReplayResultPlugin;
import org.processmining.logalignment.plugins.ConvertReplayResultToLogAlignmentPlugin;
import org.processmining.logalignment.plugins.CreateReplayCostFactorFromActivityClusterArrayPlugin;
import org.processmining.logalignment.plugins.FilterLogAlignmentPlugin;
import org.processmining.logalignment.plugins.MergeLogAlignmentArrayIntoLogAlignmentPlugin;
import org.processmining.logalignment.plugins.ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin;
import org.processmining.logdecomposer.filters.impl.DecompositionInFilter;
import org.processmining.logdecomposer.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;
import org.processmining.logdecomposer.plugins.DecomposeEventLogUsingActivityClusterArrayPlugin;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

import nl.tue.alignment.Replayer;

public abstract class DecomposedAbstractReplayConfiguration implements DecomposedReplayConfiguration {

	protected String decompositionStrategy;

	public DecomposedAbstractReplayConfiguration() {
		this.decompositionStrategy = DecompositionReplaceNoReduceStrategy.NAME;
	}

	public DecomposedAbstractReplayConfiguration(String decompositionStrategy) {
		this.decompositionStrategy = decompositionStrategy;
	}


	public CausalActivityMatrix getMatrix(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		return parameters.isDiscoverMatrixFromNet() ? getMatrixNet(context, net, parameters)
				: getMatrixLog(context, log, parameters);
	}

	public CausalActivityMatrix getMatrixNet(PluginContext context, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		CreateFromAcceptingPetriNetPlugin plugin = new CreateFromAcceptingPetriNetPlugin();
		CreateFromAcceptingPetriNetParameters params = new CreateFromAcceptingPetriNetParameters(net);
		params.setClassifier(parameters.getClassifier());
		params.setCreator(MatrixPNCreator.NAME);
		return plugin.run(context, net, params);
	}

	public CausalActivityMatrix getMatrixLog(PluginContext context, XLog log, DecomposedReplayParameters parameters) {
		DiscoverFromEventLogPlugin plugin = new DiscoverFromEventLogPlugin();
		DiscoverFromEventLogParameters params = new DiscoverFromEventLogParameters(log);
		params.setClassifier(parameters.getClassifier());
		params.setMiner(new HAFMidiMatrixMiner().getName());
		return plugin.run(context, log, params);
	}

	public CausalActivityGraph getGraph(PluginContext context, CausalActivityMatrix matrix,
			DecomposedReplayParameters parameters) {
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin plugin = new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters params = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		params.setZeroValue(0.5);
		params.setConcurrencyRatio(0.0);
		//		params.setIncludeThreshold(0.005);
		return plugin.run(context, matrix, params);
	}

	public ActivityClusterArray getClusters(PluginContext context, CausalActivityGraph graph,
			DecomposedReplayParameters parameters) {
		ConvertCausalActivityGraphToActivityClusterArrayPlugin plugin = new ConvertCausalActivityGraphToActivityClusterArrayPlugin();
		ConvertCausalActivityGraphToActivityClusterArrayParameters params = new ConvertCausalActivityGraphToActivityClusterArrayParameters(
				graph);
		params.setCheckBackwardArc(false);
		params.setIncludeAll(true);
		return plugin.run(context, graph, params);
	}

	public ActivityClusterArray getClusters(PluginContext context, AcceptingPetriNetArray nets, CausalActivityMatrix matrix, 
			DecomposedReplayParameters parameters) {
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin plugin = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin();
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters params = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters(nets, new HashSet<XEventClass>(matrix.getActivities()));
		params.setMapping(parameters.getMapping());
		params.setIOAware(true);
		return plugin.run(context, nets, params);
	}

	public ActivityClusterArray getModifiedClusters(PluginContext context, CausalActivityGraph graph, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		ModifyActivityClusterArrayPlugin plugin = new ModifyActivityClusterArrayPlugin();
		ModifyActivityClusterArrayParameters params = new ModifyActivityClusterArrayParameters(clusters);
		params.setNrOfClusters((clusters.getClusters().size() * parameters.getPercentage()) / 100);
		return plugin.run(context, clusters, graph, params);
	}
	
	public ReplayCostFactor getFactor(PluginContext context, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		CreateReplayCostFactorFromActivityClusterArrayPlugin plugin = new CreateReplayCostFactorFromActivityClusterArrayPlugin();
		CreateReplayCostFactorFromActivityClusterArrayParameters params = new CreateReplayCostFactorFromActivityClusterArrayParameters();
		return plugin.run(context, clusters, params);
	}

	public EventLogArray getLogs(PluginContext context, XLog log, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		DecomposeEventLogUsingActivityClusterArrayPlugin plugin = new DecomposeEventLogUsingActivityClusterArrayPlugin();
		DecomposeEventLogUsingActivityClusterArrayParameters params = new DecomposeEventLogUsingActivityClusterArrayParameters(
				log);
		params.setClassifier(parameters.getClassifier());
		params.setRemoveEmptyTraces(false);
		params.setFilter(DecompositionInFilter.NAME);
		params.setAddStartEndEvents(false);
		return plugin.run(context, log, clusters, params);
	}

	public AcceptingPetriNetArray getNets(PluginContext context, AcceptingPetriNet net, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters, String strategy) {
		DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin plugin = new DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin();
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters params = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(
				net, clusters, parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		params.setIncludeClusterTransitions(true);
		params.setStrategy(strategy);
		params.setPercentage(parameters.getPercentage());
		params.setUnsplittableActivities(new HashSet<XEventClass>(parameters.getUnsplittableActivities()));
		return plugin.run(context, net, clusters, params);
	}

	public ReplayResultArray getResults(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayCostFactor factor, ActivityClusterArray clusters, DecomposedReplayParameters parameters) {
		ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin plugin = new ReplayEventLogArrayOnAcceptingPetriNetArrayPlugin();
		ReplayEventLogArrayOnAcceptingPetriNetArrayParameters params = new ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(
				logs, nets);
		params.setClassifier(parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		params.setMoveOnLogCosts(parameters.getMoveOnLogCosts());
		params.setMoveOnModelCosts(parameters.getMoveOnModelCosts());
		params.setDeadline(parameters.getDeadline());
		
		// new Alignment package
		params.setAlgorithmType(parameters.getAlgorithmType());
		params.setMoveSort(parameters.isMoveSort());
		params.setQueueSort(parameters.isQueueSort());
		params.setPreferExact(parameters.isPreferExact());
		params.setnThreads(parameters.getnThreads());
		params.setUseInt(parameters.isUseInt());
		params.setDebug(parameters.getDebug());
		params.setOutputDir(parameters.getOutputDir());
		params.setTimeoutPerTraceInSecs(parameters.getTimeoutPerTraceInSecs());
		params.setMaximumNumberOfStates(parameters.getMaximumNumberOfStates());
		params.setCostUpperBound(parameters.getCostUpperBound());
		params.setPartiallyOrderEvents(parameters.isPartiallyOrderEvents());
		params.setPreProcessUsingPlaceBasedConstraints(parameters.isPreProcessUsingPlaceBasedConstraints());
		params.setInitialSplits(parameters.getInitialSplits());

		if (parameters.isPreferBorderTransitions()) {
			return plugin.run(context, logs, nets, clusters, factor, params);
		}
		return plugin.run(context, logs, nets, factor, params);
	}

	public LogAlignmentArray getAlignments(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray results, ReplayCostFactor factor, DecomposedReplayParameters parameters) {
		ConvertReplayResultToLogAlignmentPlugin plugin = new ConvertReplayResultToLogAlignmentPlugin();
		ConvertReplayResultToLogAlignmentParameters params = new ConvertReplayResultToLogAlignmentParameters(logs,
				nets);
		params.setClassifier(parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		return plugin.run(context, logs, nets, results, factor, params);
	}

	public LogAlignmentArray getFilteredAlignments(PluginContext context, LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, DecomposedReplayParameters parameters) {
		FilterLogAlignmentPlugin plugin = new FilterLogAlignmentPlugin();
		FilterLogAlignmentParameters params = new FilterLogAlignmentParameters();
		return plugin.run(context, alignments, nets, params);
	}

	public LogAlignment getAlignment(PluginContext context, LogAlignmentArray alignments, XLog log,
			AcceptingPetriNetArray nets, DecomposedReplayParameters parameters) {
		MergeLogAlignmentArrayIntoLogAlignmentPlugin plugin = new MergeLogAlignmentArrayIntoLogAlignmentPlugin();
		MergeLogAlignmentArrayIntoLogAlignmentParameters params = new MergeLogAlignmentArrayIntoLogAlignmentParameters(
				log, alignments);
		params.setClassifier(parameters.getClassifier());
		params.setPseudoUnreliable(true);
		params.setAddConflictOnlyOnce(parameters.isAddConflictOnlyOnce());
		return plugin.run(context, log, alignments, nets, params);
	}

	public PNRepResult getResult(PluginContext context, LogAlignment alignment, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		ConvertLogAlignmentToReplayResultPlugin plugin = new ConvertLogAlignmentToReplayResultPlugin();
		ConvertLogAlignmentToReplayResultParameters params = new ConvertLogAlignmentToReplayResultParameters(log, net);
		params.setClassifier(parameters.getClassifier());
		params.setMapping(parameters.getMapping());
		return plugin.run(context, alignment, net, log, params);
	}

	protected ActivityClusterArray combineClusters(ActivityClusterArray clusters) {
		ActivityClusterArray combinedClusters = ActivityClusterArrayFactory.createActivityClusterArray();
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			activities.addAll(cluster);
		}
		combinedClusters.init("", activities);
		combinedClusters.addCluster(activities);
		ActivityClusterArray results = combinedClusters;
		return results;
	}
	
	private void printMarking(Marking marking, PrintStream stream) {
		Iterator<Place> it = marking.iterator();
		if (it.hasNext()) {
			stream.print(it.next().toString());
			while (it.hasNext()) {
				stream.print(it.next().toString());
			}
			stream.println();
		} else {
			stream.println("empty");
		}
	}
	
	public void printNetArray(PluginContext context, AcceptingPetriNetArray nets, String outDir) {
		for (int i = 0; i < nets.getSize(); ++i) {
			String outFp = outDir + File.separator + i + ".txt";
			File file = new File(outFp);
			PrintStream stream = null;
			
			try {
				stream = new PrintStream(file);
				stream.println("InitialMarking");
				printMarking(nets.getNet(i).getInitialMarking(), stream);
				
				Iterator<Marking> it = nets.getNet(i).getFinalMarkings().iterator();
				stream.println("FinalMarkings");
				int tmp = 0;
				if (it.hasNext()) {
					stream.println("Final marking " + tmp);
					printMarking(it.next(), stream);
				}
				
				stream.println("Source,Target,SourceType,TargetType,IsInvisible");
				for (PetrinetEdge e: nets.getNet(i).getNet().getEdges()) {
					stream.print(e.getSource().toString() + "," + e.getTarget().toString());
//					System.out.println("Source is transition: " + (e.getSource() instanceof Transition));
//					System.out.println("Target is transition: " + (e.getTarget() instanceof Transition));
					if (e.getSource() instanceof Transition) {
						stream.println(",transition,place," + ((Transition) e.getSource()).isInvisible());
					} else {
						stream.println(",place,transition," + ((Transition) e.getTarget()).isInvisible());
					}
				}
				
			} catch (IOException ioe) {
				System.out.println("Cannot write to " + outFp );
				ioe.printStackTrace();
			} finally {
				if (stream != null)
					stream.close();
			}
		}
	}
	
	public void printLogAlignmentArray(PluginContext context, LogAlignmentArray alignments, String outDir,
			EventLogArray logs, AcceptingPetriNetArray nets, XEventClassifier classifier, TransEvClassMapping mapping) {
		// not actually used, just for creating the submapping
		Set<XEventClass> activities = new HashSet<XEventClass>();
		XEventClass invisibleActivity = XUtils.INVISIBLEACTIVITY;
		
		for (int i = 0; i < alignments.getSize(); ++i) {
			LogAlignment alignment = alignments.getAlignment(i);
			XLog log = logs.getLog(i);
			AcceptingPetriNet apn = nets.getNet(i);
			
			String logOutDir = outDir + File.separator + "subalign-" + i;
			File dir = new File(logOutDir);
			if (!dir.isDirectory()) {
				dir.mkdirs();
			}
			
			// create submapping between sublog and subnet
			TransEvClassMapping subMapping = TransEvClassMappingUtils.getInstance().getMapping(
					apn.getNet(), activities, classifier);
			for (Transition subTransition : apn.getNet().getTransitions()) {
				subMapping.put(subTransition, invisibleActivity);
				for (Transition transition : mapping.keySet()) {
					if (!mapping.get(transition).equals(invisibleActivity)) {
						if (!subTransition.isInvisible() && transition.getLabel().equals(subTransition.getLabel())) {
							subMapping.put(subTransition, mapping.get(transition));
							continue;
						}
					}
				}
			}
			
			printLogAlignment(context, alignment, logOutDir, log, apn, classifier, subMapping);
		}
	}
	
	private void printLogAlignment(PluginContext context, LogAlignment alignment, String outDir, XLog log, 
			AcceptingPetriNet net, XEventClassifier classifier, TransEvClassMapping mapping) {
		ConvertLogAlignmentToReplayResultAlgorithm algorithm = new ConvertLogAlignmentToReplayResultAlgorithm();
		ConvertLogAlignmentToReplayResultParameters params = new ConvertLogAlignmentToReplayResultParameters(log, net);
		params.setClassifier(classifier);
		params.setMapping(mapping);
		
		PNRepResult result = algorithm.apply(context, alignment, net, log, params);
		
		int cnt = 0;
		for (SyncReplayResult traceAlignment: result) {
			// set up print stream
			String fp = outDir + File.separator + cnt + ".csv";
			File file = new File(fp);
			PrintStream stream = null;
			
			try {
				stream = new PrintStream(file);
				
				// print exitcode
				stream.println("Exitcode");
				if (traceAlignment.getInfo().containsKey(Replayer.TRACEEXITCODE)) {
					stream.println(traceAlignment.getInfo().get(Replayer.TRACEEXITCODE));
				} else {
					stream.println(traceAlignment.isReliable());
				}
				
				List<String> caseIds = getCaseIds(traceAlignment, log);

				// print representative caseid
				stream.println("Representative caseId");
				stream.println(caseIds.get(0));
				
				// print list of caseids
				stream.println("CaseIds");
				stream.print(caseIds.get(0));
				for (int i = 1; i < caseIds.size(); ++i) {
					stream.print("," + caseIds.get(i));
				}
				
				// print moves	
				List<String> moves = toMoves(traceAlignment, mapping);
				stream.println("\nMove type,Log,Model");
				for (int i = 0; i < moves.size(); ++i) {
					stream.println(moves.get(i));
				}
				
			} catch (IOException ioe) {
				System.out.println("Cannot write to " + fp );
				ioe.printStackTrace();
			} finally {
				if (stream != null)
					stream.close();
			}
			
			++cnt;
		}
	}
	
	private List<String> getCaseIds(SyncReplayResult alignment, XLog log) {
		List<String> caseIds = new ArrayList<>();
		for (int index: alignment.getTraceIndex()) {
			XTrace trace = log.get(index);
			String caseId = trace.getAttributes().get("concept:name").toString();
			caseIds.add(caseId);
		}
		return caseIds;
	}

	private boolean isMove(StepTypes stepType) {
		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L || stepType == StepTypes.MREAL
				|| stepType == StepTypes.MINVI;
	}
	
	private List<String> toMoves(SyncReplayResult result, TransEvClassMapping mapping) {
		
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
}
