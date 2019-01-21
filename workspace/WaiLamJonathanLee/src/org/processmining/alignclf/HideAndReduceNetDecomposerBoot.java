package org.processmining.alignclf;

import java.lang.reflect.InvocationTargetException;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.HTMLToString;


public class HideAndReduceNetDecomposerBoot {
	public static final String DECOMPOSITION_FILEPATH = "decomposition_fp";
	public static final String NET_FILEPATH = "net_fp";
	public static final String OUT_FILEPATH = "out_fp";
	
	@Plugin(name = "Boot Get Hide and Reduce Decomposition", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		
//		String JSONFilePath = commandlineArguments.get(0);
//		String JSONString = readFileAsString(JSONFilePath);
//		
//		JsonElement jelement = new JsonParser().parse(JSONString);
//		JsonObject jobject = jelement.getAsJsonObject();
//		
//		// check if there is unsplittable activity file
//		String decompositionFilePath = jobject.get(DECOMPOSITION_FILEPATH).getAsString();
//		String netFilePath = jobject.get(NET_FILEPATH).getAsString();
//		String outFilePath = jobject.get(OUT_FILEPATH).getAsString();
	
		String decompositionFilePath = commandlineArguments.get(0);
		String netFilePath = commandlineArguments.get(1);
		String outFilePath = commandlineArguments.get(2);
		
		HideAndReduceNetDecomposerContext globalContext = new HideAndReduceNetDecomposerContext();
		CLIPluginContext context = globalContext.getMainPluginContext();
		
		String pluginName = "Hide and Reduce Net Decomposer";
		HideAndReduceNetDecomposerParameters params = new HideAndReduceNetDecomposerParameters(netFilePath, decompositionFilePath, outFilePath);
		
		HTMLToString report = context.tryToFindOrConstructFirstNamedObject(HTMLToString.class, pluginName, 
				null, null, params);
		
		System.exit(0);
		
		return null;
	}
	
	public static void main(String[] args) throws Throwable {
		try {
		
			Boot.boot(HideAndReduceNetDecomposerBoot.class, CLIPluginContext.class, args);
		
		} catch (InvocationTargetException e) {
			
			throw e.getCause();
		
		}
	}
}
