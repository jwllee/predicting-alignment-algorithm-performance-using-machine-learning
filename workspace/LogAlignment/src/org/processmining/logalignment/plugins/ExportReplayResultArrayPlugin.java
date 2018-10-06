package org.processmining.logalignment.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.models.ReplayResultArray;

@Plugin(name = "RRA export (Replay Result Array)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"replay Result Array", "File" }, userAccessible = true)
@UIExportPlugin(description = "replay Result Array", extension = "rra")
public class ExportReplayResultArrayPlugin {

	@PluginVariant(variantLabel = "RRA export (Replay Result Array)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, ReplayResultArray replayResults, File file) throws IOException {
		replayResults.exportToFile(context, file);
	}

}
