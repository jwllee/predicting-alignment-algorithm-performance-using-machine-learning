package org.processmining.experiments.plugins;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.plugins.RecomposingReplayPlugin;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.experiments.parameters.TestRecomposingReplayParameters;
import org.processmining.experiments.utils.LogImporter;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

@Plugin(name = "Evaluate Recomposing Replay", parameterLabels = { "Parameters"  }, returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class TestRecomposingReplayPlugin implements HTMLToString {

	private StringBuffer buf = new StringBuffer();
	private RecomposingReplayPlugin replayer = new RecomposingReplayPlugin();
	private static ImportPlugin logImporter = new OpenLogFilePlugin();
	private static ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Jonathan Lee", email = "walee@uc.cl", pack = "JonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestRecomposingReplayParameters parameters) {
		
		XLog log;
		AcceptingPetriNet apn;
		try {
			/*
			 * Import the event log.
			 */
			System.out.println("Importing " + parameters.logPath);
			log = LogImporter.importFile(parameters.logPath);
			/*
			 * Import the Accepting Petri net discovered for the corresponding noise-free event log.
			 */
			System.out.println("Importing " + parameters.modelPath);
			apn = (AcceptingPetriNet) netImporter.importFile(context, parameters.modelPath);
			return new TestRecomposingReplayPlugin(context, log, apn, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public TestRecomposingReplayPlugin(final PluginContext context, final XLog log, final AcceptingPetriNet apn,
			final TestRecomposingReplayParameters parameters) {
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
			replayParameters.setPreferBorderTransitions(parameters.preferBorderTransitions);
			
			// set unsplittable activity list
			Set<XEventClass> unsplittableActivities = new HashSet<>();
			final XLogInfo info = XLogInfoFactory.createLogInfo(log, log.getClassifiers().get(0));
			unsplittableActivities.addAll(info.getEventClasses().getClasses());
			Set<XEventClass> initialDecompositionActivities = new HashSet<>();
			for (XEventClass activity: info.getEventClasses().getClasses()) {
				if (parameters.initialDecompositionSet.contains(activity.getId())) {
					// add initial decomposition activity
					initialDecompositionActivities.add(activity);
					System.out.println("Add initial decomposition activity: " + activity.getId());
				}
			}
			
			if (!initialDecompositionActivities.isEmpty()) {
				unsplittableActivities.removeAll(initialDecompositionActivities);
				replayParameters.setUnsplittableActivities(unsplittableActivities);
			} 
			
			
			System.out.println("GlobalDuration: " + replayParameters.getGlobalDuration());
			System.out.println("LocalDuration: " + replayParameters.getLocalDuration());
			System.out.println("MoveOnLogCosts: " + replayParameters.getMoveOnLogCosts());
			System.out.println("MoveOnModelCosts: " + replayParameters.getMoveOnModelCosts());
			System.out.println("IntervalRelative: " + replayParameters.getIntervalRelative());
			System.out.println("IntervalAbsolute: " + replayParameters.getIntervalAbsolute());
			System.out.println("MaxConflicts: " + replayParameters.getMaxConflicts());
			System.out.println("AlignmentPercentage: " + replayParameters.getAlignmentPercentage());
			System.out.println("NofIterations: " + replayParameters.getNofIterations());
			
			RecomposingReplayWorkspace workspace = new RecomposingReplayWorkspace(log, replayParameters);
			
			long time = -System.currentTimeMillis();
			PNRepResult repResult = replayer.apply(context, log, apn, workspace, replayParameters);
			time += System.currentTimeMillis();
			
			System.out.println("Finished recomposing replay in " + time + " millis.");
			
			boolean isReliable = true;
			int numOfTraces = repResult.size();
			int numOfAligned = workspace.alignments.size();
			int numOfToAlign = workspace.openAlignments.size();
			int numOfRejected = workspace.pseudoAlignments.size();

			double sumLoCosts = 0;
			double sumHiCosts = 0;
			double sumMaxCosts = 0;
			int sumWeight = 0;
			
			for (SyncReplayResult alignment : repResult) {
				if (alignment.isReliable()) {
					int nofTraces = alignment.getTraceIndex().size();
					double costLo = alignment.getInfo().get("Raw Fitness Cost Lo");
					double costHi = alignment.getInfo().get("Raw Fitness Cost Hi");
					double maxCosts = alignment.getInfo().get("Raw Fitness Cost Max");
					sumLoCosts += nofTraces * costLo;
					sumMaxCosts += nofTraces * maxCosts;
					sumHiCosts += nofTraces * costHi;
					sumWeight += nofTraces;
				}
			}
			
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
			// recompose and log creation strategies
			buf.append("na, na, ");
			// whether there is preference for border transitions
			buf.append(parameters.preferBorderTransitions + ", ");
			buf.append(replayParameters.isAddConflictOnlyOnce() + ", ");
			buf.append(replayParameters.isUseHideAndReduceAbstraction() + ", ");
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
			buf.append(numOfAligned + ", ");
			buf.append(numOfToAlign + ", ");
			buf.append(numOfRejected + ", ");
			buf.append(numOfTraces + ", ");
			// get number of recomposition steps, need to minus 1 because of implementation
			buf.append(workspace.nofIterations - 1 + ", ");
			buf.append(time + "");
		} catch (Exception e) {
			System.out.println("There is an exception: " + e.getMessage());
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.iteration + ", ");
			buf.append("na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na");
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
