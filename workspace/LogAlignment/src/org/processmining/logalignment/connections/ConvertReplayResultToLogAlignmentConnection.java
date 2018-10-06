package org.processmining.logalignment.connections;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class ConvertReplayResultToLogAlignmentConnection extends AbstractConnection {

	public final static String LOGS = "Logs";
	public final static String NETS = "Nets";
	public final static String RESULTS = "ReplayResults";
	public final static String ALIGNMENTS = "LogAlignments";

	private ConvertReplayResultToLogAlignmentParameters parameters;

	public ConvertReplayResultToLogAlignmentConnection(EventLogArray logs, AcceptingPetriNetArray nets,
			PNRepResult replayResult, LogAlignment alignment, ConvertReplayResultToLogAlignmentParameters parameters) {
		super("Convert Replay Result to Log Alignment Connection");
		put(LOGS, logs);
		put(NETS, nets);
		put(RESULTS, replayResult);
		put(ALIGNMENTS, alignment);
		this.parameters = new ConvertReplayResultToLogAlignmentParameters(parameters);
	}

	public ConvertReplayResultToLogAlignmentConnection(EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults, LogAlignmentArray alignments,
			ConvertReplayResultToLogAlignmentParameters parameters) {
		super("Convert Replay Result to Log Alignment Connection");
		put(LOGS, logs);
		put(NETS, nets);
		put(RESULTS, replayResults);
		put(ALIGNMENTS, alignments);
		this.parameters = new ConvertReplayResultToLogAlignmentParameters(parameters);
	}

	public ConvertReplayResultToLogAlignmentParameters getParameters() {
		return parameters;
	}
}
