package org.processmining.decomposedreplayer.experiments.boot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.decomposedreplayer.experiments.contexts.TestRecomposingReplayWithMergeStrategyContext;
import org.processmining.decomposedreplayer.experiments.parameters.TestRecomposingReplayWithMergeStrategyParameters;
import org.processmining.decomposedreplayer.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestRecomposingReplayWithMergeStrategyBoot {
	
	private static final Logger LOGGER = Logger.getLogger(TestRecomposingReplayWithMergeStrategyBoot.class.getName());
	private static FileHandler fh = null;
	private static long startBoot = -1;
	private static int oneMil = 1000000;
	
	@Plugin(name = "Boot Recomposed Replay", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		long endBoot = System.nanoTime();
		if (startBoot != -1) {
			long takenBoot = (endBoot - startBoot) / oneMil;
			LOGGER.log(Level.INFO, "Booting recomposing replay boot took " + takenBoot + " ms.");	
		}
		
		long start = System.nanoTime();
		
		TestRecomposingReplayWithMergeStrategyContext globalContext = new TestRecomposingReplayWithMergeStrategyContext();
		CLIPluginContext context = globalContext.getMainPluginContext();

		int N = 2;
		if (commandlineArguments.size() != N) {
			LOGGER.info("cwd: " + System.getProperty("user.dir"));
			LOGGER.info("Need " + N + " parameters. There are " + commandlineArguments.size());
			return null;
		}
		
		String configJSONFilePath = commandlineArguments.get(1);
        
        StringFileReader sfr = new StringFileReader();
        String configJSONString = sfr.readFileAsString(configJSONFilePath);
        TestRecomposingReplayWithMergeStrategyParameters params = TestRecomposingReplayWithMergeStrategyParameters.readParams(configJSONString);
        
        LOGGER.info(params.toString());
        
		// get the initial decomposition set
		StringFileReader reader = new StringFileReader();
		List<String> initialDecompActList = reader.readFileAsStringList(params.initDecompFile);
		Set<String> initialDecompActSet = new HashSet<>();
		for (String act: initialDecompActList) {
		    initialDecompActSet.add(act);
		}
		params.initialDecompositionSet = initialDecompActSet;
		
		String pluginName = "Evaluate Recomposing Replay with Merge";
		
		LOGGER.info("[" + getClass().getSimpleName() + "] Trying to find plugin with name: " + pluginName);
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, pluginName, 
				null, null, params);
		
		OutputStream outputStream = new FileOutputStream(params.outFile, true);
		Writer outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(report.toHTMLString(false));
		outputStreamWriter.close();
		
		long end = System.nanoTime();
		long taken = (end - start) / oneMil;
		LOGGER.info("Running boot main took: " + taken + " ms.");

		// We're done. No need to linger.
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		
		LOGGER.log(Level.INFO, "Arguments: " + args[0] + ", " + args[1]);
		
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
		
			Boot.boot(TestRecomposingReplayWithMergeStrategyBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			
			throw e.getCause();
		
		}
	}
	
}
