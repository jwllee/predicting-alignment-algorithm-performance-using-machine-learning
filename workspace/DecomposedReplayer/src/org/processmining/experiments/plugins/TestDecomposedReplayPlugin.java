package org.processmining.experiments.plugins;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedNotDecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.decomposedreplayer.plugins.DecomposedReplayPlugin;
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

	private StringBuffer buf = new StringBuffer();
	private DecomposedReplayPlugin replayer = new DecomposedReplayPlugin();
	private static ImportPlugin logImporter = new OpenLogFilePlugin();
	private static ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "EricVerbeek")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestDecomposedReplayParameters parameters) {
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
			return new TestDecomposedReplayPlugin(context, log, apn, parameters);
		} catch (Exception e) {
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
			long time = -System.currentTimeMillis();
			PNRepResult repResult = replayer.apply(context, log, apn, replayParameters);
			time += System.currentTimeMillis();
			
			// Save all the fitness cost per alignment in one csv
			String dirname = "./test_results";
			File dir = new File(dirname);
			if (!(dir.exists() || dir.isDirectory())) 
				dir.mkdirs();
			String filepath = dirname + "/per_alignment_results_monolithic.csv";
			List<String> repResultPerAlignment = new LinkedList<>();
			String header = "Raw Fitness Cost, Case Id";
			repResultPerAlignment.add(header);
			
			// print out the state information
			double logStateCount = 0.0;
			double logQueuedStates = 0.0;
			double logTraversedArcs = 0.0;
			double avgLogStateCount = 0.0;
			double avgLogQueuedStates = 0.0;
			double avgLogTraversedArcs = 0.0;
			int logSize = 0;
			
			for (SyncReplayResult alignment: repResult) {
				String fitnessCost = alignment.getInfo().get(PNRepResult.RAWFITNESSCOST) + "";
				String caseIds = "";
				for (int index: alignment.getTraceIndex()) {
					XTrace trace = log.get(index);
					String caseId = trace.getAttributes().get("concept:name").toString();
					if (caseIds.equals(""))
						caseIds = caseIds + caseId;
					else
						caseIds = caseIds + "; " + caseId;
				}
				String row = fitnessCost + ", " + caseIds;
				repResultPerAlignment.add(row);

				int nofTraces = alignment.getTraceIndex().size();
				logSize += nofTraces;
				double stateCount = alignment.getInfo().get(PNRepResult.NUMSTATEGENERATED);
				double queuedStates = alignment.getInfo().get(PNRepResult.QUEUEDSTATE);
				double traversedArcs = alignment.getInfo().get(PNRepResult.TRAVERSEDARCS);
				
				logStateCount += stateCount * nofTraces;
				logQueuedStates += queuedStates * nofTraces;
				logTraversedArcs += traversedArcs * nofTraces;
				
				// get the caseid of the first associated trace
				int index = alignment.getTraceIndex().first();
				XTrace xtrace = log.get(index);
				String caseId = xtrace.getAttributes().get("concept:name").toString();
				
				System.out.printf("[%s] Alignment of caseid %s: "
						+ "No. of generated states: %.2f, "
						+ "No. of queued states: %.2f, "
						+ "No. of traversed arcs: %.2f%n", 
						getClass().getSimpleName(), caseId, stateCount, queuedStates, traversedArcs);
			}
			
			if (logSize > 0) {
				avgLogStateCount = logStateCount / logSize;
				avgLogQueuedStates = logQueuedStates / logSize;
				avgLogTraversedArcs = logTraversedArcs / logSize;
			}
			
			System.out.printf("[%s] (Log level) No. of generated states: %.2f, "
					+ "No. of queued states: %.2f, No. of traversed arcs: %.2f%n", 
					getClass().getSimpleName(), logStateCount, logQueuedStates, logTraversedArcs);
			
			StringFileWriter writer = new StringFileWriter();
			writer.writeStringListToFile(repResultPerAlignment, filepath);
			
			double costs = (Double) repResult.getInfo().get(PNRepResult.RAWFITNESSCOST);
			boolean isReliable = true;
			int numOfReliable = 0;
			for (SyncReplayResult result : repResult) {
				if (result.isReliable() && !result.getStepTypes().isEmpty()) {
					numOfReliable += 1;
				}
			}
			
			// print alignments as csv
			TransEvClassMapping mapping = replayParameters.getMapping();
			// make a directory for all the alignments
			String[] outPathSplit = parameters.outFile.split(File.separator);
			String outdir = outPathSplit[0];
			String toCopy;
			for (int i = 1; i < outPathSplit.length; i++) {
				toCopy = outPathSplit[i];
				if (i == outPathSplit.length - 1)
					toCopy = toCopy.replace("results", "stats");
				if (i != outPathSplit.length - 1)
					// only add to the out directory if it is not the final filename
					outdir += (File.separator + toCopy);
			}
			
			outdir += (File.separator + parameters.iteration);
			boolean madeDir = new File(outdir).mkdirs();
			System.out.printf("[%s] Created directory for alignments at %s: %b", 
					getClass().getSimpleName(), outdir, madeDir);
			String alignmentFilePath;
			int i = 0;
			for (SyncReplayResult alignment: repResult) {
				alignmentFilePath = String.format("%s%s%s%d.csv", 
						outdir, File.separator, "alignments", i);
				ReplayResultCsvWriter.writeReplayResultToCsv(alignment, alignmentFilePath, mapping);
				i += 1;
			}

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
			buf.append(costs + ", ");
			buf.append(costs + ", ");
			buf.append("na, na, ");
			buf.append(numOfReliable + ", ");
			// number of to align and rejected: na
			buf.append("na, na, ");
			// total number of traces in log
			buf.append(repResult.size() + ", ");
			// number of recomposition steps: na
			buf.append("na, ");
			buf.append(time + ", ");
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
					+ "na, na, na, na, na, na, na, na, na, na, na, na");
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
