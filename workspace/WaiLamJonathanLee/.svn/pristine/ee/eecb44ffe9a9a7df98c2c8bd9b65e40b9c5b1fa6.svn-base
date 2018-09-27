package org.processmining.pnrepresultconversion.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.pnrepresultconversion.parameters.PNRepResultConversionParameters;
import org.processmining.pnrepresultconversion.utils.XAlignedLogBuilder;
import org.processmining.xesalignmentextension.XAlignmentExtension;

public class PNRepResultConversionAlgorithm {
	
	private XConceptExtension conceptInstance;
	private XEventClassifier eventClassifier;
	private XAlignmentExtension alignmentInstance;
	
	private Map<Transition, String> localIdCache;

	public XLog apply(PluginContext context, PNRepResult logAlignment,
			XLog log, PNRepResultConversionParameters parameters) {
		eventClassifier = parameters.getEventClassifier();
		conceptInstance = XConceptExtension.instance();
		alignmentInstance = XAlignmentExtension.instance();
		localIdCache = new HashMap<>();
		
		XAlignedLogBuilder builder = XAlignedLogBuilder.newInstance();
		builder.startAlignedLog(log.toString());
		
		double avgTraceFitness = 0;
		int nofTraces = 0;
		
		long start = System.currentTimeMillis();
		for (SyncReplayResult alignment: logAlignment) {
			double traceFitness = tryGetFitness(alignment);
			avgTraceFitness += traceFitness * alignment.getTraceIndex().size();
			nofTraces += alignment.getTraceIndex().size();
			addAlignment(alignment, log, builder);
		}
		long end = System.currentTimeMillis();
		System.out.println("PNRepResultConversionAlgorithm] Took " + (end - start) + " millis for conversion.");
		
		if (nofTraces > 0) {
			avgTraceFitness /= nofTraces;
		}
		
		System.out.println("[PNRepResultConversionAlgorithm] Average log fitness: " + avgTraceFitness);
		
		XLog alignedLog = builder.build();
		alignmentInstance.assignFitness(alignedLog, avgTraceFitness);
		return alignedLog;
	}
	
	private void addAlignment(SyncReplayResult alignment, XLog log,
			XAlignedLogBuilder builder) {
		for (int index: alignment.getTraceIndex()) {
			XTrace trace = log.get(index);
			String caseId = conceptInstance.extractName(trace);
			addCaseAlignment(caseId, trace, alignment, builder);
		}
	}
	
	/**
	 * Try to get the trace fitness, might need to try different attribute keys
	 * depending on which alignment plugin was used to compute the trace fitness
	 * @param alignment
	 * @return trace fitness
	 */
	private double tryGetFitness(SyncReplayResult alignment) {
		Set<String> attributeKeys = alignment.getInfo().keySet();
		if (attributeKeys.contains(PNRepResult.TRACEFITNESS)) {
			return alignment.getInfo().get(PNRepResult.TRACEFITNESS);
		} else if (attributeKeys.contains("Raw Fitness Perc Lo")) {
			// Be safe and return the lower bound on fitness
			return alignment.getInfo().get("Raw Fitness Perc Lo");
		} else {
			// certain lower bound
			return 0;
		}
	}
	
	private void addCaseAlignment(String caseId, XTrace trace,
			SyncReplayResult alignment, XAlignedLogBuilder builder) {
		// try to get trace fitness
		double traceFitness = tryGetFitness(alignment);
		boolean isReliable = alignment.isReliable();
		builder.addAlignment(caseId, traceFitness);
		builder.addAttribute("isReliable", isReliable);
		for (String attribute: alignment.getInfo().keySet()) {
			double value = alignment.getInfo().get(attribute);
			builder.addAttribute(attribute, value);
		}
		
		// iterate through the steps with the trace
		int currTraceInd = 0;
		List<Object> nodes = alignment.getNodeInstance();
		List<StepTypes> steps = alignment.getStepTypes();
		Transition trans;
		XEvent event;
		String logMove;
		// iterate through the node instances and trace
		// supposedly node instance is a transition if 
		// it is a synchronous move, model move or invisible move
		// otherwise it is a XEventClass
		// Check {@link AbstractPetrinetReplayer#recordToResult()} for more details
		for (int i = 0; i < nodes.size(); i++) {
			Object node = nodes.get(i);
			StepTypes stepType = steps.get(i);
			switch (stepType) {
				case LMGOOD: // synchronous move
					// correspond to the current event in trace
					event = trace.get(currTraceInd);
					// need to advance the currTraceInd
					currTraceInd += 1;
					assert node instanceof Transition;
					trans = (Transition) node;
					addSynchronousMove(stepType.toString(), event, trans, builder);
					break;
				case MINVI: // invisible move
					assert node instanceof Transition;
					trans = (Transition) node;
					addInvisibleMove(stepType.toString(), trans, builder);
					break;
				case MREAL: // model move
					assert node instanceof Transition;
					trans = (Transition) node;
					addModelMove(stepType.toString(), trans, builder);
					break;
				case L: // log move
					logMove = node.toString();
					event = trace.get(currTraceInd);
					currTraceInd += 1;
					addLogMove(stepType.toString(), logMove, event, builder);
					break;
				default:
					throw new IllegalStateException("Do not recognize step type: " + stepType);
			}
		}
	}
	
	private void addLogMove(String name, String logMove, XEvent event, 
			XAlignedLogBuilder builder) {
//		System.out.println("[PNRepResultConversionAlgorithm] Adding log move: " + logMove);
		String eventClassId = eventClassifier.getClassIdentity(event);
		builder.addLogMove(name, logMove, eventClassId);
		for (Entry<String, XAttribute> entry: event.getAttributes().entrySet()) {
			builder.addAttribute(entry.getValue());
		}
	}
	
	private void addModelMove(String name, Transition transition,
			XAlignedLogBuilder builder) {
		String modelMove = transition.getLabel();
//		System.out.println("[PNRepResultConversionAlgorithm] Adding model move: " + modelMove);
		String activityId = tryGetLocalIdFromCache(transition);
		builder.addModelMove(name, modelMove, activityId, true);
	}
	
	private void addInvisibleMove(String name, Transition transition,
			XAlignedLogBuilder builder) {
		String modelMove = transition.getLabel();
//		System.out.println("[PNRepResultConversionAlgorithm] Adding invisible move: " + modelMove);
		String activityId = tryGetLocalIdFromCache(transition);
		builder.addModelMove(name, modelMove, activityId, false);
	}
	
	
	private void addSynchronousMove(String name, XEvent event, 
			Transition transition, XAlignedLogBuilder builder) {
		// log move equals model move
		String logMove = transition.getLabel();
		String modelMove = logMove;
//		System.out.println("[PNRepResultConversionAlgorithm] Adding synchronous move: " + logMove);
		String eventClassId = eventClassifier.getClassIdentity(event);
		String activityId = tryGetLocalIdFromCache(transition);
		builder.addSynchronousMove(name, logMove, modelMove, 
				eventClassId, activityId);
		for (Entry<String, XAttribute> entry: event.getAttributes().entrySet()) {
			builder.addAttribute(entry.getValue());
		}
	}

	private String tryGetLocalIdFromCache(Transition transition) {
		String localId = localIdCache.get(transition);
		if (localId == null) {
			localId = transition.getLocalID().localNodeID().toString();
			localIdCache.put(transition, localId);
		}
		return localId;
	}
}
