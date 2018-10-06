package org.processmining.logalignment.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.parameters.ConvertLogAlignmentToReplayResultParameters;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class ConvertLogAlignmentToReplayResultConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String NET = "Net";
	public final static String RESULT = "Replay Result";
	public final static String ALIGNMENT = "Log Alignment";

	private ConvertLogAlignmentToReplayResultParameters parameters;

	public ConvertLogAlignmentToReplayResultConnection(LogAlignment alignment, AcceptingPetriNet net, XLog log, 
			PNRepResult replayResult, ConvertLogAlignmentToReplayResultParameters parameters) {
		super("Convert Log Alignment to Replay Result Connection");
		put(LOG, log);
		put(NET, net);
		put(RESULT, replayResult);
		put(ALIGNMENT, alignment);
		this.parameters = new ConvertLogAlignmentToReplayResultParameters(parameters);
	}

	public ConvertLogAlignmentToReplayResultParameters getParameters() {
		return parameters;
	}
}
