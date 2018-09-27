package org.processmining.experiments.boot;

import java.lang.reflect.InvocationTargetException;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.contexts.TestSharedActivitiesContext;
import org.processmining.experiments.parameters.ParameterReader;
import org.processmining.experiments.parameters.TestSharedActivitiesParameters;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestSharedActivitiesBoot {

	@Plugin(name = "Boot Get Shared Activities", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		
		TestSharedActivitiesContext globalContext = new TestSharedActivitiesContext();
		CLIPluginContext context = globalContext.getMainPluginContext();
		
		if (commandlineArguments.size() != 1) {
			
			System.out.println("[TestSharedActivitiesBoot] Need one parameter. There are "
					+ commandlineArguments.size());
			return null;
			
		}
		
		String configJsonFilePath = commandlineArguments.get(0);
		
		// Build a parameter instance using the JSON configuration file
		StringFileReader sfr = new StringFileReader();
		String configJsonString = sfr.readFileAsString(configJsonFilePath);		
		ParameterReader paramReader = new ParameterReader();
		TestSharedActivitiesParameters parameters = paramReader.readSharedActsParams(configJsonString);
		
		System.out.println(parameters.toString());
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, 
				"Test get shared activities", null, null, parameters);
		
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		try {
			Boot.boot(TestSharedActivitiesBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
