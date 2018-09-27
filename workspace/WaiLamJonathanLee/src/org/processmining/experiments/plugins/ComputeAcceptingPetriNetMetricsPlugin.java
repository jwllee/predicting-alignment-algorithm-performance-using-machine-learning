package org.processmining.experiments.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.experiments.metrics.AcceptingPetriNetMetric;
import org.processmining.experiments.metrics.AcceptingPetriNetMetricManager;
import org.processmining.experiments.parameters.ComputeAcceptingPetriNetMetricsParameters;
import org.processmining.experiments.utils.StringFileWriter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;


@Plugin(name = "Compute Petri Net Metrics",
		categories = {PluginCategory.Analytics }, 
		parameterLabels = { "Parameters" },
		returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class ComputeAcceptingPetriNetMetricsPlugin implements HTMLToString {

	private StringBuffer buf = new StringBuffer();
	private static ImportAcceptingPetriNetPlugin apnImporter = new ImportAcceptingPetriNetPlugin();
	private static AcceptingPetriNetMetricManager metricManager = AcceptingPetriNetMetricManager.getInstance();
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Jonathan Lee", email = "walee@uc.cl", pack = "JonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, 
			ComputeAcceptingPetriNetMetricsParameters parameters) {
		
		AcceptingPetriNet apn;
		
		try {
			
			System.out.println("[" + ComputeAcceptingPetriNetMetricsPlugin.class.getSimpleName() + "] Importing " + parameters.netPath);
			apn = (AcceptingPetriNet) apnImporter.importFile(context, parameters.netPath);
			
			return new ComputeAcceptingPetriNetMetricsPlugin(context, apn, parameters);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	public ComputeAcceptingPetriNetMetricsPlugin(final PluginContext context, final AcceptingPetriNet apn, 
			final ComputeAcceptingPetriNetMetricsParameters parameters) {
		// compute the metrics
		String header = "ModelPath";
		String data = parameters.netPath;
		
		for (AcceptingPetriNetMetric metric: metricManager.getMetrics()) {
			header += ", " + metric.getName();
			
			double value = metric.compute(context, apn);
			data += ", " + value;
		}
		
		// output results
		File outFile = new File(parameters.outFile);
		StringFileWriter fileWriter = new StringFileWriter();
		List<String> toWrite = new ArrayList<String>();
		if (!outFile.exists()) {
			toWrite.add(header);
		}
		toWrite.add(data);
		fileWriter.writeStringListToFile(toWrite, parameters.outFile);
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		// To a comma separated string (csv)
		StringBuffer buffer = new StringBuffer();
		System.out.println(buf);
		buffer.append(buf);
		buffer.append("\n");
		return buffer.toString();
	}

}
