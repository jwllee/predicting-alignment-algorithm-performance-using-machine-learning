package org.processmining.logalignment.models.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.plugins.ImportLogAlignmentPlugin;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class LogAlignmentArrayImpl implements LogAlignmentArray {

	private List<LogAlignment> logAlignments;
	
	protected LogAlignmentArrayImpl() {

	}
	
	public void init() {
		logAlignments = new ArrayList<LogAlignment>();
	}
	
	public int addAlignment(LogAlignment alignment) {
		logAlignments.add(alignment);
		return logAlignments.indexOf(alignment);
	}
	
	public int removeAlignment(LogAlignment alignment) {
		int index = logAlignments.indexOf(alignment);
		if (index >= 0) {
			logAlignments.remove(index);
		}
		return index;
	}
	
	public void addAlignment(int index, LogAlignment alignment) {
		logAlignments.add(index, alignment);
	}

	public void removeAlignment(int index) {
		logAlignments.remove(index);
	}

	public LogAlignment getAlignment(int index) {
		return logAlignments.get(index);
	}

	public int getSize() {
		return logAlignments.size();
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		for (int index = 0; index < logAlignments.size(); index++) {
			buffer.append("<h1>Log Alignment " + index + "/" + logAlignments.size() + "</h1>");
			buffer.append(logAlignments.get(index).toHTMLString(false));
		}
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}

	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception {
		Reader streamReader = new InputStreamReader(input);
		CsvReader csvReader = new CsvReader(streamReader);
		init();
		while (csvReader.readRecord()) {
			String fileName = csvReader.get(0);
			if (parent != null && fileName.indexOf(File.separator) == -1) {
				fileName = parent + File.separator + fileName;
			}
			System.out.println("Importing Log Alignment from " + fileName);
			File alignmentFile = new File(fileName);
			ImportLogAlignmentPlugin alignmentImporter = new ImportLogAlignmentPlugin();
			LogAlignment alignment = (LogAlignment) alignmentImporter.importFile(context, alignmentFile);
			logAlignments.add(alignment);
		}
		csvReader.close();
	}

	public void exportToFile(PluginContext context, File file) throws IOException {
		Writer fileWriter = new FileWriter(file);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		int n = 1;
		for (LogAlignment alignment: logAlignments) {
			String fileName = file.getName();
			File dir = file.getParentFile();
			String prefix = fileName.substring(0, fileName.indexOf("."));
			File netFile = File.createTempFile(prefix + "." + n + ".", ".lal", dir);
			csvWriter.write(netFile.getName());
			csvWriter.endRecord();
			System.out.println("Exporting Log Alignment to " + netFile.getName());
			alignment.exportToFile(context, netFile);
			n++;
		}
		csvWriter.close();
	}
}
