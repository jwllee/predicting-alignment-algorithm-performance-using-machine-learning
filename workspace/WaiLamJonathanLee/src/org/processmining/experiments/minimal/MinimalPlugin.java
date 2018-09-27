package org.processmining.experiments.minimal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.experiments.utils.StringFileWriter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;

@Plugin(name = "Minimal plugin",
		categories = { PluginCategory.Analytics },
		parameterLabels = { "Parameters" },
		returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class MinimalPlugin implements HTMLToString {

	private StringBuffer buf = new StringBuffer();
	private static ImportAcceptingPetriNetPlugin apnImporter = new ImportAcceptingPetriNetPlugin();
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Jonathan Lee", email = "walee@uc.cl", pack = "JonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, MinimalParameters parameters) {
		
		AcceptingPetriNet apn;
		XLog xlog = null;
		
		File xlogFile = new File(parameters.logPath);
		XUniversalParser parser = new XUniversalParser();
				
		String cname = "[" + MinimalPlugin.class.getSimpleName() + "] ";
		
		try {
			
			System.out.println(cname + "Importing " + parameters.netPath);
			apn = (AcceptingPetriNet) apnImporter.importFile(context, parameters.netPath);
			
			System.out.println(cname + "Importing " + parameters.logPath);
			
			if (parser.canParse(xlogFile))
				xlog = parser.parse(xlogFile).iterator().next();
			else
				System.out.println(cname + "Cannot import log file: " + parameters.logPath);
		
			return new MinimalPlugin(context, apn, xlog, parameters);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	public MinimalPlugin(final PluginContext context, final AcceptingPetriNet apn,
			final XLog xlog, final MinimalParameters parameters) {
		
		File outFile = new File(parameters.outFile);
		StringFileWriter fileWriter = new StringFileWriter();
		List<String> toWrite = new ArrayList<String>();
		
		if (!outFile.exists())
			toWrite.add("Hello world!");
		
		fileWriter.writeStringListToFile(toWrite, parameters.outFile);
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		return "";
	}

}
