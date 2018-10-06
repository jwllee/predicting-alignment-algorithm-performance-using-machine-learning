package org.processmining.logalignment.plugins;

import java.io.File;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.impl.LogAlignmentFactory;

@Plugin(name = "Import Log Alignment from LAL file", parameterLabels = { "Filename" }, returnLabels = { "Log Alignment" }, returnTypes = { LogAlignment.class })
@UIImportPlugin(description = "LAL Log Alignment files", extensions = { "lal" })
public class ImportLogAlignmentPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("LAL files", "lal");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		LogAlignment alignment = LogAlignmentFactory.createLogAlignment();
		File file = getFile();
		String parent = (file == null ? null : file.getParent());
		alignment.importFromStream(context, input, parent);
		return alignment;
	}
}
