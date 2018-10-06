package org.processmining.logalignment.plugins;

import java.io.File;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.impl.LogAlignmentArrayFactory;

@Plugin(name = "Import Log Alignment Array from LALA file", parameterLabels = { "Filename" }, returnLabels = { "Log Alignment Array" }, returnTypes = { LogAlignmentArray.class })
@UIImportPlugin(description = "LALA Log ALignment Array files", extensions = { "lala" })
public class ImportLogAlignmentArrayPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("LALA files", "lala");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		LogAlignmentArray alignments = LogAlignmentArrayFactory.createLogAlignmentArray();
		File file = getFile();
		String parent = (file == null ? null : file.getParent());
		alignments.importFromStream(context, input, parent);
		return alignments;
	}
}
