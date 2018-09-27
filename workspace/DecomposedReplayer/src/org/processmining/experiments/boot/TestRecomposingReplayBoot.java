package org.processmining.experiments.boot;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.contexts.TestRecomposingReplayContext;
import org.processmining.experiments.parameters.ParameterReader;
import org.processmining.experiments.parameters.TestRecomposingReplayParameters;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestRecomposingReplayBoot {

	@Plugin(name = "Boot Recomposed Replay", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		
		TestRecomposingReplayContext globalContext = new TestRecomposingReplayContext();
		CLIPluginContext context = globalContext.getMainPluginContext();

		if (commandlineArguments.size() != 1) {
		    System.out.println("Need one parameter. There are " + commandlineArguments.size());
			return null;
		}
		
		String configJSONFilePath = commandlineArguments.get(0);
        
        StringFileReader sfr = new StringFileReader();
        String configJSONString = sfr.readFileAsString(configJSONFilePath);
        
        ParameterReader paramReader = new ParameterReader();
        TestRecomposingReplayParameters params = paramReader.readRReplayParams(configJSONString);
        
        System.out.println(params.toString());
        		
		// get the initial decomposition set
		StringFileReader reader = new StringFileReader();
		List<String> initialDecompActList = reader.readFileAsStringList(params.initDecompFile);
		Set<String> initialDecompActSet = new HashSet<>();
		for (String act: initialDecompActList) {
		    initialDecompActSet.add(act);
		}
		params.initialDecompositionSet = initialDecompActSet;
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class,
				"Evaluate Recomposing Replay", null, null, params);
		
		OutputStream outputStream = new FileOutputStream(params.outFile, true);
		Writer outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(report.toHTMLString(false));
		outputStreamWriter.close();

		// We're done. No need to linger.
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		try {
			Boot.boot(TestRecomposingReplayBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
