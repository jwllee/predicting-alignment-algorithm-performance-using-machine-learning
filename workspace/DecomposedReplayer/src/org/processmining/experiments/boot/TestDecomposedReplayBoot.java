package org.processmining.experiments.boot;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.contexts.TestDecomposedReplayContext;
import org.processmining.experiments.parameters.ParameterReader;
import org.processmining.experiments.parameters.TestDecomposedReplayParameters;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;

public class TestDecomposedReplayBoot {

	@Plugin(name = "Boot Decomposed Replay", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {

		TestDecomposedReplayContext globalContext = new TestDecomposedReplayContext();
		CLIPluginContext context = globalContext.getMainPluginContext();

		if (commandlineArguments.size() != 1) {
		    System.out.println("Need one parameter. There are " + commandlineArguments.size());
		    return null;
		}
		
		String configJSONFilePath = commandlineArguments.get(0);
		
		StringFileReader sfr = new StringFileReader();
		String configJSONString = sfr.readFileAsString(configJSONFilePath);
		
		ParameterReader paramReader = new ParameterReader();
		TestDecomposedReplayParameters params = paramReader.readDReplayParams(configJSONString);

		System.out.println(params.toString());
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class,
				"Evaluate Decomposed Replay", null, null, params);

		OutputStream outputStream = new FileOutputStream(params.outFile, true);
		Writer outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(report.toHTMLString(false));
		outputStreamWriter.close();

		// We're done. No need to linger.
		System.exit(0);

		return report;
	}

	public static void main(String[] args) throws Throwable {
		try {
			Boot.boot(TestDecomposedReplayBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
