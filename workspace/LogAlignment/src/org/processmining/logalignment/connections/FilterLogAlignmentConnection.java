package org.processmining.logalignment.connections;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.parameters.FilterLogAlignmentParameters;

public class FilterLogAlignmentConnection extends AbstractConnection {

	public final static String ALIGNMENTS = "Alignments";
	public final static String NETS = "Nets";
	public final static String FITLEREDALIGNMENTS = " Filtered Alignments";

	private FilterLogAlignmentParameters parameters;

	public FilterLogAlignmentConnection(LogAlignmentArray alignments,
			AcceptingPetriNetArray nets, LogAlignmentArray fitleredAlignments, FilterLogAlignmentParameters parameters) {
		super("Convert Replay Result to Log Alignment Connection");
		put(ALIGNMENTS, alignments);
		put(NETS, nets);
		put(FITLEREDALIGNMENTS, fitleredAlignments);
		this.parameters = new FilterLogAlignmentParameters(parameters);
	}

	public FilterLogAlignmentParameters getParameters() {
		return parameters;
	}
}
