package org.processmining.experiments.boot;

import java.lang.reflect.InvocationTargetException;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.contexts.ComputeAcceptingPetriNetMetricsContext;
import org.processmining.experiments.parameters.ComputeAcceptingPetriNetMetricsParameters;
import org.processmining.experiments.parameters.ParameterReader;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class ComputeAcceptingPetriNetMetricsBoot {

	@Plugin(name = "Boot Compute Petri Net Metrics", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		
		ComputeAcceptingPetriNetMetricsContext globalContext = new ComputeAcceptingPetriNetMetricsContext();
		CLIPluginContext context = globalContext.getMainPluginContext();
		
		if (commandlineArguments.size() != 1) {
			System.out.println("[" + getClass().getSimpleName() + "] Need one parameter. There are " + commandlineArguments.size() + ".");
			return null;
		} 
		
		String configJSONFilePath = commandlineArguments.get(0);
		
		StringFileReader sfr = new StringFileReader();
		String configJSONString = sfr.readFileAsString(configJSONFilePath);
		
		ParameterReader paramReader = new ParameterReader();
		ComputeAcceptingPetriNetMetricsParameters params = paramReader.readComputePetriNetMetricsParams(configJSONString);
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, 
				"Compute Petri Net Metrics", null, null, params);
		
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		try {
			Boot.boot(ComputeAcceptingPetriNetMetricsBoot.class, CLIPluginContext.class, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
