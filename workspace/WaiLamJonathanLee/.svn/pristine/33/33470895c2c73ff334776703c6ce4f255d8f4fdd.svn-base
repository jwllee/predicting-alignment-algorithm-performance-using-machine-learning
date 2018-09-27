package org.processmining.experiments.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetArrayPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.experiments.parameters.TestSharedActivitiesParameters;
import org.processmining.experiments.utils.AcceptingPetriNetArrayUtils;
import org.processmining.experiments.utils.StringFileWriter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;

@Plugin(name = "Test get shared activities", parameterLabels = { "Parameters"  }, returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class TestSharedActivitiesPlugin implements HTMLToString {

	private StringBuffer buf = new StringBuffer();
	private static ImportAcceptingPetriNetArrayPlugin netArrayImporter = new ImportAcceptingPetriNetArrayPlugin();
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl", pack = "WaiLamJonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestSharedActivitiesParameters parameters) {
		AcceptingPetriNetArray netArray;
		File netArrayFile;
		
		try {
			
			netArrayFile = new File(parameters.netArrayFilePath);
			
			System.out.println("[TestSharedActivitiesPlugin] Importing net array from: " + 
					parameters.netArrayFilePath);
			
			netArray = (AcceptingPetriNetArray) netArrayImporter.importFile(context, netArrayFile);
			
			return new TestSharedActivitiesPlugin(context, netArray, parameters);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	public TestSharedActivitiesPlugin(final PluginContext context,
			final AcceptingPetriNetArray netArray, final TestSharedActivitiesParameters parameters) {
		
		AcceptingPetriNetArrayUtils netArrayUtils = new AcceptingPetriNetArrayUtils();
		
		System.out.println("[TestSharedActivitiesPlugin] Finding shared activities...");
		
		List<XEventClass> sharedActs = netArrayUtils.getSharedActivities(netArray);
		List<String> sharedActLabels = new ArrayList<>();
		
		for (XEventClass act: sharedActs) 
			sharedActLabels.add(act.getId());
		
		System.out.println("[TestSharedActivitiesPlugin] Found " + sharedActLabels.size() + 
				" shared activities.");
		
		StringFileWriter writer = new StringFileWriter();
		writer.writeStringListToFile(sharedActLabels, parameters.sharedActsFilePath);
		
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
