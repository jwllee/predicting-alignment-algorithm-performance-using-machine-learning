package org.processmining.logalignment.models.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.TraceAlignment;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class LogAlignmentImpl implements LogAlignment {

	private Set<XEventClass> cluster;
	private Map<List<XEventClass>, TraceAlignment> alignments;

	protected LogAlignmentImpl() {
	}
	
	public void init() {
		cluster = new HashSet<XEventClass>();
		alignments = new HashMap<List<XEventClass>, TraceAlignment>();
	}

	public Set<XEventClass> getCluster() {
		return cluster;
	}

	public void setCluster(Set<XEventClass> cluster) {
		this.cluster = cluster;
	}
	
	public void addToCluster(Set<XEventClass> cluster) {
		this.cluster.addAll(cluster);
	}
	
	public Map<List<XEventClass>, TraceAlignment> getAlignments() {
		return alignments;
	}
	
	public TraceAlignment getAlignment(List<XEventClass> trace) {
		return alignments.get(trace);
	}

	public void setAlignments(Map<List<XEventClass>, TraceAlignment> alignments) {
		this.alignments = alignments;
	}
	
	public void putAlignment(List<XEventClass> trace, TraceAlignment alignment) {
		alignments.put(trace, alignment);
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
			buffer.append("<h1>Log alignment</h1>");
		}
		buffer.append("<h2>Activities</h2>");
		buffer.append(cluster);
		buffer.append("<h2>Trace alignments</h2>");
		for (List<XEventClass> trace : alignments.keySet()) {
			buffer.append("<h3>" + trace + "</h3>");
			buffer.append(alignments.get(trace).toHTMLString(false));
		}
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}
	
	public void exportToFile(PluginContext context, File file) throws IOException {
		Writer fileWriter = new FileWriter(file);
		CsvWriter csvWriter = new CsvWriter(fileWriter, ',');
		for (XEventClass activity : cluster) {
			csvWriter.write(activity.getId());
		}
		csvWriter.endRecord();
		for (TraceAlignment alignment : alignments.values()) {
			alignment.exportToCSVFile(csvWriter);
		}
		csvWriter.close();
	}
	
	public void importFromStream(PluginContext context, InputStream input, String parent) throws IOException {
		Reader streamReader = new InputStreamReader(input);
		CsvReader csvReader = new CsvReader(streamReader);
		Map<String, XEventClass> map = new HashMap<String, XEventClass>();
		init();
		if (csvReader.readRecord()) {
			for (int i = 0; i < csvReader.getColumnCount(); i++) {
				String activityId = csvReader.get(i);
				XEventClass activity = new XEventClass(activityId, i);
				cluster.add(activity);
				map.put(activityId, activity);
			}
		}
		while (csvReader.readRecord()) {
			TraceAlignment alignment = TraceAlignmentFactory.createTraceAlignment();
			alignment.importFromCSVFile(csvReader, map);
			alignments.put(alignment.getLogMoves(), alignment);
		}
		csvReader.close();
	}
}
