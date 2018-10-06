package org.processmining.logalignment.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.models.LogAlignment;

@Plugin(name = "LAL export (Log Alignment)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Log Alignment", "File" }, userAccessible = true)
@UIExportPlugin(description = "Log Alignment", extension = "lal")
public class ExportLogAlignmentPlugin {

	@PluginVariant(variantLabel = "LAL export (Log Alignment)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, LogAlignment alignment, File file) throws IOException {
		alignment.exportToFile(context, file);
	}
}
