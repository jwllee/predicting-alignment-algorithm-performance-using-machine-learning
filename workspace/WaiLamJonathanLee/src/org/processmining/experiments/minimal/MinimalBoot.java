package org.processmining.experiments.minimal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class MinimalBoot {
	private static final Logger LOGGER = Logger.getLogger( MinimalBoot.class.getName() );
	private static FileHandler fh = null;
	private static long startBoot = -1;
	
	@Plugin(name = "Boot Minimal", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		long endBoot = System.nanoTime();
		if (startBoot != -1) {
			long takenBoot = (endBoot - startBoot) / 1000000;
			LOGGER.log(Level.INFO, "Booting minimal boot main took " + takenBoot + " ms.");
		}
		
		long start = System.nanoTime();
		
		MinimalContext globalContext = new MinimalContext();
		CLIPluginContext context = globalContext.getMainPluginContext();
		
		String cname = "[" + getClass().getSimpleName() + "] ";
		
		int n = 1;
		if (commandlineArguments.size() != n) {
			
			System.out.println(cname + "Need " + n + " parameter. There are " + commandlineArguments.size() + ".");
			return null;
			
		}
		
		String configJSONFilePath = commandlineArguments.get(0);
		
		StringFileReader sfr = new StringFileReader();
		String configJSONString = sfr.readFileAsString(configJSONFilePath);
		
		MinimalParameters params = MinimalParameters.readMinimalParams(configJSONString);
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, 
				"Minimal plugin", null, null, params);
		
		long end = System.nanoTime();
		long taken = (end - start) / 1000000;
		LOGGER.log(Level.INFO, "Running Boot Minimal main took: " + taken + " ms.");
		
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		
		// set up the logger handler
		try {
			
			String logFp = args[0];
			fh = new FileHandler(logFp, false);
		
		} catch (SecurityException | IOException e) {
			
			e.printStackTrace();
		
		}
		
		fh.setFormatter(new SimpleFormatter());
		LOGGER.addHandler(fh);
		LOGGER.setLevel(Level.CONFIG);
		
		startBoot = System.nanoTime();
		
		String[] bootArgs = { args[1] };
		
		try {
			
			Boot.boot(MinimalBoot.class, CLIPluginContext.class, bootArgs);
			
		} catch (InvocationTargetException e) {
			
			throw e.getCause();
			
		}

	}
	
}
