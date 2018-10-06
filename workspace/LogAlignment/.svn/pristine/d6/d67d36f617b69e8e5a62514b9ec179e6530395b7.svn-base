package org.processmining.logalignment.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;

public class MergeLogAlignmentArrayIntoLogAlignmentConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String ALIGNMENTS = "Alignments";
	public final static String ALIGNMENT = "Alignment";

	private MergeLogAlignmentArrayIntoLogAlignmentParameters parameters;

	public MergeLogAlignmentArrayIntoLogAlignmentConnection(XLog log, LogAlignmentArray alignments,
			LogAlignment alignment, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		super("Merge Log Alignments Connection");
		put(LOG, log);
		put(ALIGNMENTS, alignments);
		put(ALIGNMENT, alignment);
		this.parameters = new MergeLogAlignmentArrayIntoLogAlignmentParameters(parameters);
	}

	public MergeLogAlignmentArrayIntoLogAlignmentParameters getParameters() {
		return parameters;
	}
}
