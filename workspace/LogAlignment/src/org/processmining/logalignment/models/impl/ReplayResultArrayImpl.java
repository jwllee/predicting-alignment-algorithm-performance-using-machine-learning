package org.processmining.logalignment.models.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.processmining.basicutils.models.impl.ObjectArrayImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class ReplayResultArrayImpl extends ObjectArrayImpl<PNRepResult> implements ReplayResultArray {

	@Deprecated
	public int addReplay(PNRepResult replay) {
		return addElement(replay);
	}

	@Deprecated
	public int removeReplay(PNRepResult replay) {
		return removeElement(replay);
	}

	@Deprecated
	public void addReplay(int index, PNRepResult replay) {
		addElement(index, replay);
	}

	@Deprecated
	public void removeReplay(int index) {
		removeReplay(index);
	}

	@Deprecated
	public PNRepResult getReplay(int index) {
		return getElement(index);
	}

	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception {
//		importFromStream(context, input, parent, ???);		
	}

	public void exportToFile(PluginContext context, File file) throws IOException {
//		Writer fileWriter = new FileWriter(file);
//		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
//		int n = 1;
//		for (PNRepResult replay: list) {
//			String fileName = file.getName();
//			File dir = file.getParentFile();
//			String prefix = fileName.substring(0, fileName.indexOf("."));
//			File netFile = File.createTempFile(prefix + "." + n + ".", ".rep", dir);
//			csvWriter.write(netFile.getName());
//			csvWriter.endRecord();
//			System.out.println("Exporting Replay Result to " + netFile.getName());
//			// TODO: Implement export of PNRepResult.
////			replay.exportToFile(context, netFile);
//			n++;
//		}
//		csvWriter.close();
	}

}
