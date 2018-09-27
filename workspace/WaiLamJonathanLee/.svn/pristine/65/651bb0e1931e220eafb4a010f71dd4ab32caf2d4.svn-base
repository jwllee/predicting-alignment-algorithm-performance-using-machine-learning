package org.processmining.decomposedreplayer.experiments.boot;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.decomposedreplayer.experiments.contexts.TestRecomposingReplayWithMergeStrategyContext;
import org.processmining.decomposedreplayer.experiments.parameters.ParameterReader;
import org.processmining.decomposedreplayer.experiments.parameters.TestRecomposingReplayWithMergeStrategyParameters;
import org.processmining.decomposedreplayer.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestRecomposingReplayWithMergeStrategyBoot {

	@Plugin(name = "Boot Recomposed Replay", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		
		TestRecomposingReplayWithMergeStrategyContext globalContext = new TestRecomposingReplayWithMergeStrategyContext();
		CLIPluginContext context = globalContext.getMainPluginContext();

		if (commandlineArguments.size() != 1) {
			System.out.println("Current working directory: " + System.getProperty("user.dir"));
		    System.out.println("Need one parameter. There are " + commandlineArguments.size());
			return null;
		}
		
		String configJSONFilePath = commandlineArguments.get(0);
        
        StringFileReader sfr = new StringFileReader();
        String configJSONString = sfr.readFileAsString(configJSONFilePath);
        
        ParameterReader paramReader = new ParameterReader();
        TestRecomposingReplayWithMergeStrategyParameters params = paramReader.readRReplayParams(configJSONString);
        
        System.out.println(params.toString());
        		
		// get the initial decomposition set
		StringFileReader reader = new StringFileReader();
		List<String> initialDecompActList = reader.readFileAsStringList(params.initDecompFile);
		Set<String> initialDecompActSet = new HashSet<>();
		for (String act: initialDecompActList) {
		    initialDecompActSet.add(act);
		}
		params.initialDecompositionSet = initialDecompActSet;
		
		String pluginName = "Evaluate Recomposing Replay with Merge";
		System.out.println("[" + getClass().getSimpleName() + "] Trying to find plugin with name: " + pluginName);
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, pluginName, 
				null, null, params);
		
		OutputStream outputStream = new FileOutputStream(params.outFile, true);
		Writer outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(report.toHTMLString(false));
		outputStreamWriter.close();

		// We're done. No need to linger.
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		
		System.out.println("Arguments: " + args[0]);
		
		try {
			Boot.boot(TestRecomposingReplayWithMergeStrategyBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
