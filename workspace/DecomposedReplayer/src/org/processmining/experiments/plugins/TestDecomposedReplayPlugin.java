package org.processmining.experiments.plugins;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedNotDecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.decomposedreplayer.plugins.DecomposedReplayPlugin;
import org.processmining.experiments.boot.TestDecomposedReplayBoot;
import org.processmining.experiments.parameters.TestDecomposedReplayParameters;
import org.processmining.experiments.utils.LogImporter;
import org.processmining.experiments.utils.ReplayResultCsvWriter;
import org.processmining.experiments.utils.StringFileWriter;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

@Plugin(name = "Evaluate Decomposed Replay", parameterLabels = { "Parameters"  }, returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class TestDecomposedReplayPlugin implements HTMLToString {
	
	private static final Logger LOGGER = Logger.getLogger(TestDecomposedReplayBoot.class.getName());
	private StringBuffer buf = new StringBuffer();
	private DecomposedReplayPlugin replayer = new DecomposedReplayPlugin();
	private static ImportPlugin logImporter = new OpenLogFilePlugin();
	private static ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "EricVerbeek")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestDecomposedReplayParameters parameters) {
		LOGGER.info("At test plugin...");
		long start = System.nanoTime();
		
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
			/*
			 * Run the replay.
			 */
			
			TestDecomposedReplayPlugin plugin = new TestDecomposedReplayPlugin(context, log, apn, parameters);
			
			long end = System.nanoTime();
			long taken = (end - start) / 1000000;
			LOGGER.info("Running test monolithic plugin took " + taken + " ms.");
			
			return plugin;
			
		} catch (Exception e) {
			
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
			
		}
		return null;
	}

	public TestDecomposedReplayPlugin(final PluginContext context, final XLog log, final AcceptingPetriNet apn,
			final TestDecomposedReplayParameters parameters) {
		try {
			DecomposedReplayParameters replayParameters = new DecomposedReplayParameters(log, apn);
			replayParameters.setConfiguration(parameters.configuration);
			replayParameters.setMoveOnModelCosts(replayParameters.getMoveOnModelCosts());
			replayParameters.setMoveOnLogCosts(replayParameters.getMoveOnLogCosts());
			replayParameters.setDeadline(parameters.deadline);
			// prefered border transition not working currently
//			replayParameters.setPreferBorderTransitions(false);
			
			long time = System.nanoTime();
			PNRepResult repResult = replayer.apply(context, log, apn, replayParameters);
			time -= System.nanoTime();
			time /= 1000000;
			
			LOGGER.info("Finished monolithic replay in " + time + " ms.");
			LOGGER.info(parameters.toString());

			//-------------------------Write the alignments to file and getting aggregate results--------------------------------------//
			// trace alignment folder
			String alignResultDir = parameters.resultDir + File.separator + parameters.iteration + File.separator + "alignments";
			File dir = new File (alignResultDir);
			if (!(dir.exists() || dir.isDirectory()))
				dir.mkdirs();

			// alignment level info
			String alignInfoFp = "alignment-info.csv";
			alignInfoFp = parameters.resultDir + File.separator + parameters.iteration + File.separator + alignInfoFp;
			List<String> alignInfo = new LinkedList<>();
			String header = "time, generated_state, queued_state, traversed_arc, fitness_cost, trace_length, case_id";
			alignInfo.add(header);
			
			// print out the state information
			int nofReliable = 0;
			double logCost = 0;
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
			StringFileWriter writer = new StringFileWriter();

			for (SyncReplayResult alignment: repResult) {
				double fitnessCost = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
				double stateCount = alignment.getInfo().get(PNRepResult.NUMSTATEGENERATED);
				double queuedStates = alignment.getInfo().get(PNRepResult.QUEUEDSTATE);
				double traversedArcs = alignment.getInfo().get(PNRepResult.TRAVERSEDARCS);
				double alignTime = alignment.getInfo().get(PNRepResult.TIME);
				int nofCases = alignment.getTraceIndex().size();
				int traceLength = 0;
				String caseId0 = "";
				
				if (alignment.isReliable() && !alignment.getStepTypes().isEmpty()) {
					nofReliable += 1;
				}
				
				// update aggregate statistics
				logCost += fitnessCost;
				logSize += nofCases;
				logStateCount += stateCount * nofCases;
				logQueuedStates += queuedStates * nofCases;
				logTraversedArcs += traversedArcs * nofCases;
				totalAlignTime += alignTime;
				
				// write out the alignment
				String alignmentFp = "alignment-" + i + ".csv";
				alignmentFp = alignResultDir + File.separator + alignmentFp;
				
				ReplayResultCsvWriter.writeReplayResultToCsv(alignment, alignmentFp, mapping);
				
				// caseids related to the alignment
				String caseIds = "";
				for (int index: alignment.getTraceIndex()) {
					XTrace trace = log.get(index);
					String caseId = trace.getAttributes().get("concept:name").toString();
					if (caseIds.equals(""))
						caseIds = caseIds + caseId;
					else
						caseIds = caseIds + "; " + caseId;
					
					// first caseid
					if (caseId0.equals(""))
						caseId0 = caseId;
				}
				
				// alignment info
				String row = alignTime + ", " + stateCount + ", " + queuedStates + ", " + 
						traversedArcs + ", " + fitnessCost + ", " + traceLength + ", " + caseIds;
				alignInfo.add(row);
				
				LOGGER.info(String.format("Alignment of caseid %s: "
						+ "No. of generated states: %.2f, "
						+ "No. of queued states: %.2f, "
						+ "No. of traversed arcs: %.2f%n", 
						caseId0, stateCount, queuedStates, traversedArcs));
			}
			
			writer.writeStringListToFile(alignInfo, alignInfoFp, true);
			
			if (logSize > 0) {
				avgLogStateCount = logStateCount / logSize;
				avgLogQueuedStates = logQueuedStates / logSize;
				avgLogTraversedArcs = logTraversedArcs / logSize;
			}
			
			LOGGER.info(String.format("(Log level) No. of generated states: %.2f, "
					+ "No. of queued states: %.2f, No. of traversed arcs: %.2f%n", 
					logStateCount, logQueuedStates, logTraversedArcs));

			// Save results as a CSV format
			buf.append(parameters.iteration + ", ");
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.modelPath + ", ");
			// log and model names
			buf.append(parameters.log + ", ");
			buf.append(parameters.model + ", ");
			// if monolithic
			buf.append((parameters.configuration.equals(DecomposedNotDecomposedReplayConfiguration.NAME)) + ", ");
			// type of decomposition, na
			buf.append("na, ");
			// recompose and log creation strategy
			buf.append("na, na, ");
			// preference for border transitions, na
			buf.append("na, ");
			// add conflict only once and use hide and reduce abstraction
			buf.append("na, na, ");
			buf.append("na, na, na, na, na, na, na, na, na, ");
			buf.append(logCost + ", ");
			buf.append(logCost + ", ");
			buf.append("na, na, ");
			buf.append(nofReliable + ", ");
			// number of to align and rejected: na
			buf.append("na, na, ");
			// total number of traces in log
			buf.append(repResult.size() + ", ");
			// number of recomposition steps: na
			buf.append("na, ");
			buf.append(time + ", ");
			buf.append(totalAlignTime + ", ");
			buf.append(logStateCount + ", ");
			buf.append(logQueuedStates + ", ");
			buf.append(logTraversedArcs + ", ");
			buf.append(avgLogStateCount + ", ");
			buf.append(avgLogQueuedStates + ", ");
			buf.append(avgLogTraversedArcs + "");
		} catch (Exception e) {
			e.printStackTrace();
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.iteration + ", ");
			// if monolithic
			buf.append((parameters.configuration.equals(DecomposedNotDecomposedReplayConfiguration.NAME)) + ", ");
			buf.append("na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, "
					+ "na, na, na, na, na, na, na, na, na, na, na, na, na");
		}
	}
	

	public String toHTMLString(boolean includeHTMLTags) {
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
