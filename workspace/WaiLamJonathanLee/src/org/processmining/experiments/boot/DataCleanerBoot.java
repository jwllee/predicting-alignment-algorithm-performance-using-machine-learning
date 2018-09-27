package org.processmining.experiments.boot;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinetclassicalreductor.plugins.ReduceUsingMurataRulesPlugin;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.configurations.CleanDataConfigurations;
import org.processmining.experiments.configurations.CleanLogConfigurations;
import org.processmining.experiments.configurations.CleanPetriNetConfigurations;
import org.processmining.experiments.contexts.DataCleanerContext;
import org.processmining.experiments.parameters.ParameterReader;
import org.processmining.experiments.utils.LogCleaner;
import org.processmining.experiments.utils.PetriNetCleaner;
import org.processmining.experiments.utils.PetriNetReducer;
import org.processmining.experiments.utils.StringFileReader;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;

public class DataCleanerBoot {

	@Plugin(name = "Boot Data Cleaner", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {

		DataCleanerContext globalContext = new DataCleanerContext();
		CLIPluginContext context = globalContext.getMainPluginContext();

		if (commandlineArguments.size() != 1) {
		    System.out.println("Need one parameter. There are " + commandlineArguments.size());
		    return null;
		}
		
		String configJSONFilePath = commandlineArguments.get(0);
		
		StringFileReader sfr = new StringFileReader();
		String configJSONString = sfr.readFileAsString(configJSONFilePath);
		
		ParameterReader paramReader = new ParameterReader();
		CleanDataConfigurations cleanDataConfigs = paramReader.readCleanDataConfigs(configJSONString);

		CleanLogConfigurations cleanLogConfigs = new CleanLogConfigurations();
		cleanLogConfigs.classifierType = cleanDataConfigs.classifierType;
		cleanLogConfigs.logCleanPath = cleanDataConfigs.logCleanPath;
		cleanLogConfigs.logDirtyPath = cleanDataConfigs.logDirtyPath;
		
		
		CleanPetriNetConfigurations cleanPetriNetConfigs = new CleanPetriNetConfigurations();
		cleanPetriNetConfigs.netCleanPath = cleanDataConfigs.netCleanPath;
		cleanPetriNetConfigs.netDirtyPath = cleanDataConfigs.netDirtyPath;
		
		System.out.println(cleanLogConfigs.toString());
		System.out.println(cleanPetriNetConfigs.toString());
		
		File logFile = new File(cleanLogConfigs.logCleanPath);
        boolean hasCleanLog = logFile.exists() && logFile.isFile();
		if (!hasCleanLog) {
			LogCleaner logCleaner = new LogCleaner(cleanLogConfigs);
			logCleaner.cleanLog();
		}
		
		File netFile = new File(cleanPetriNetConfigs.netCleanPath);
		boolean hasCleanNet = netFile.exists() && netFile.isFile();
		if (!hasCleanNet) {
			ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();
			ExportAcceptingPetriNetPlugin netExporter = new ExportAcceptingPetriNetPlugin();
			PetriNetCleaner netCleaner = new PetriNetCleaner(cleanPetriNetConfigs, netImporter, netExporter);
			AcceptingPetriNet cleaned = netCleaner.cleanPNet(context);
			ReduceUsingMurataRulesPlugin netReducer = new ReduceUsingMurataRulesPlugin();
			PetriNetReducer netReducerUtils = new PetriNetReducer(cleaned, netReducer);
			AcceptingPetriNet reduced = netReducerUtils.reduceAPN(context);
			// export the net
			String outputNetPath = cleanPetriNetConfigs.netCleanPath;
			File outputNetFile = new File(outputNetPath);
			netExporter.export(context, reduced, outputNetFile);
		}
		
		// We're done. No need to linger.
		System.exit(0);

		return null;
	}

	public static void main(String[] args) throws Throwable {
		try {
			Boot.boot(DataCleanerBoot.class, CLIPluginContext.class, args);

		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
