package org.processmining.logalignment.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logalignment.models.LogAlignmentArray;

@Plugin(name = "LALA export (Log Alignment Array)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Log Alignment Array", "File" }, userAccessible = true)
@UIExportPlugin(description = "Log Alignment Array", extension = "lala")
public class ExportLogAlignmentArrayPlugin {

	@PluginVariant(variantLabel = "LALA export (Log Alignment Array)", requiredParameterLabels = { 0, 1 })
	public void export(PluginContext context, LogAlignmentArray alignments, File file) throws IOException {
		alignments.exportToFile(context, file);
	}
}
