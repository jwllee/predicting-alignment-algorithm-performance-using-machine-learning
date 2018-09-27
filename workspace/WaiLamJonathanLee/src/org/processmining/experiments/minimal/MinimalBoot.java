package org.processmining.experiments.minimal;

import java.lang.reflect.InvocationTargetException;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class MinimalBoot {

	@Plugin(name = "Boot Minimal", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		
		MinimalContext globalContext = new MinimalContext();
		CLIPluginContext context = globalContext.getMainPluginContext();
		
		String cname = "[" + getClass().getSimpleName() + "] ";
		
		if (commandlineArguments.size() != 1) {
			
			System.out.println(cname + "Need one parameter. There are " + commandlineArguments.size() + ".");
			return null;
			
		}
		
		String configJSONFilePath = commandlineArguments.get(0);
		
		StringFileReader sfr = new StringFileReader();
		String configJSONString = sfr.readFileAsString(configJSONFilePath);
		
		MinimalParameters params = MinimalParameters.readMinimalParams(configJSONString);
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, 
				"Minimal plugin", null, null, params);
		
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		
		try {
			
			Boot.boot(MinimalBoot.class, CLIPluginContext.class, args);
			
		} catch (InvocationTargetException e) {
			
			throw e.getCause();
			
		}
	}
	
}
