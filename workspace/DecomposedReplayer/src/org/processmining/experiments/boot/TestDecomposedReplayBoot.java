package org.processmining.experiments.boot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.contexts.TestDecomposedReplayContext;
import org.processmining.experiments.parameters.TestDecomposedReplayParameters;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestDecomposedReplayBoot {
	
	private static final Logger LOGGER = Logger.getLogger(TestDecomposedReplayBoot.class.getName());
	private static FileHandler fh = null;
	private static long startBoot = -1;

	@Plugin(name = "Boot Decomposed Replay", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		long endBoot = System.nanoTime();
		if (startBoot != -1) {
			long takenBoot = (endBoot - startBoot) / 1000000;
			LOGGER.info("Booting monolithic replay boot took: " + takenBoot + " ms.");
		}
		
		long start = System.nanoTime();
		
		TestDecomposedReplayContext globalContext = new TestDecomposedReplayContext();
		CLIPluginContext context = globalContext.getMainPluginContext();

		int N = 2;
		if (commandlineArguments.size() != N) {
			LOGGER.info("cwd: " + System.getProperty("user.dir"));
		    LOGGER.info("Need " + N + " parameter. There are " + commandlineArguments.size());
		    return null;
		}
		
		String configJSONFilePath = commandlineArguments.get(1);
		
		StringFileReader sfr = new StringFileReader();
		String configJSONString = sfr.readFileAsString(configJSONFilePath);
		
		TestDecomposedReplayParameters params = TestDecomposedReplayParameters.readParams(configJSONString);

		LOGGER.info(params.toString());
		
		String pluginName = "Evaluate Decomposed Replay";
		
		LOGGER.info("[" + getClass().getSimpleName() + "] Trying to find plugin with name: " + pluginName);
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class,
				pluginName, null, null, params);

		OutputStream outputStream = new FileOutputStream(params.outFile, true);
		Writer outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(report.toHTMLString(false));
		outputStreamWriter.close();

		long end = System.nanoTime();
		long taken = (end - start) / 1000000;
		LOGGER.info("Running boot main took: " + taken + " ms.");
		
		// We're done. No need to linger.
		System.exit(0);

		return report;
	}

	public static void main(String[] args) throws Throwable {
		
		LOGGER.info("Arguments: " + args);
		
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
		
		try {
			
			Boot.boot(TestDecomposedReplayBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			
			throw e.getCause();
			
		}
	}
	
}
