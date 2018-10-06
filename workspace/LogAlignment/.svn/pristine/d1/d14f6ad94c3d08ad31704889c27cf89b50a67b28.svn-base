package org.processmining.logalignment.plugins;

import java.io.File;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.models.impl.ReplayResultArrayFactory;

@Plugin(name = "Import Replay Result Array from RRA file", parameterLabels = { "Filename" }, returnLabels = { "replay Result Array" }, returnTypes = { ReplayResultArray.class })
@UIImportPlugin(description = "RRA Replay Result Array files", extensions = { "rra" })
public class ImportReplayResultArrayPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("RRA files", "rra");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		ReplayResultArray replayResults = ReplayResultArrayFactory.createReplayResultArray();
		File file = getFile();
		String parent = (file == null ? null : file.getParent());
		replayResults.importFromStream(context, input, parent);
		return replayResults;
	}}
