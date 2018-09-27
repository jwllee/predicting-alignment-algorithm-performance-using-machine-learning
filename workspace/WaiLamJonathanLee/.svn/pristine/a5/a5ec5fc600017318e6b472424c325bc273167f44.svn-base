package org.processmining.experiments.boot;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.contexts.TestAcceptingPetriNetDecompositionContext;
import org.processmining.experiments.parameters.TestAcceptingPetriNetDecompositionParameters;
import org.processmining.experiments.utils.JsonUtils;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestAcceptingPetriNetDecompositionBoot {

	@Plugin(name = "Boot Accepting Petri Net Decomposition", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
	
		TestAcceptingPetriNetDecompositionContext globalContext = new TestAcceptingPetriNetDecompositionContext();
		CLIPluginContext context = globalContext.getMainPluginContext();
		
		if (commandlineArguments.size() != 1) {
			
			System.out.println("Need one parameter. There are " + commandlineArguments.size());
			return null;
			
		}
		
		String configJsonFilePath = commandlineArguments.get(0);
		
		// Read the parameters from the JSON file
		StringFileReader sfr = new StringFileReader();
		String configJsonString = sfr.readFileAsString(configJsonFilePath);
		JsonUtils jsonUtils = new JsonUtils();
		Map<String, String> configsMap = jsonUtils.readJsonStringAsStringMap(configJsonString);
		
		System.out.println("[TestAcceptingPetriNetDecompositionBoot] Parameters: " + configsMap.toString());
		
		// Build the parameter instance using the information from JSON string map
		TestAcceptingPetriNetDecompositionParameters parameters = new TestAcceptingPetriNetDecompositionParameters();
		parameters.netFilePath = configsMap.remove("netFilePath");
		parameters.classifierType = configsMap.remove("classifierType");
		parameters.decompositionStrategy = configsMap.remove("decompositionStrategy");
		parameters.netArrayFilePath = configsMap.remove("netArrayFilePath");
		if (configsMap.remove("includeClusterTransitions").equals("true")) 
			parameters.includeClusterTransitions = true;
		else
			parameters.includeClusterTransitions = false;
		parameters.unsplittableFilePath = configsMap.remove("unsplittableFilePath");
		parameters.optionalConfigs.putAll(configsMap);

		// get the unsplittable activities
		List<String> unsplittableActLabelList = sfr.readFileAsStringList(parameters.unsplittableFilePath);
		Set<String> unsplittableActLabelSet = new HashSet<>();
		unsplittableActLabelSet.addAll(unsplittableActLabelList);
		parameters.unsplittableActLabels = unsplittableActLabelSet;
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class,
				"Test decompose accepting petri net", null, null, parameters);
		
		System.exit(0);
		
		return null;
		
	}
	
	public static void main(String[] args) throws Throwable {
		try {
			Boot.boot(TestAcceptingPetriNetDecompositionBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
