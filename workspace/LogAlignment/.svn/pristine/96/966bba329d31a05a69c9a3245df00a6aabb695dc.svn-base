package org.processmining.logalignment.connections;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;

public class ReplayEventLogArrayOnAcceptingPetriNetArrayConnection extends AbstractConnection {

	public final static String NETS = "Nets";
	public final static String LOGS = "Logs";
	public final static String REPLAYS = "Replays";

	private ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters;

	public ReplayEventLogArrayOnAcceptingPetriNetArrayConnection(AcceptingPetriNetArray nets, EventLogArray logs,
			ReplayResultArray replays, ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		super("Replay Event Log Array On Accepting Petri Net Array Connection");
		put(NETS, nets);
		put(LOGS, logs);
		put(REPLAYS, replays);
		this.parameters = parameters;
	}

	public ReplayEventLogArrayOnAcceptingPetriNetArrayParameters getParameters() {
		return parameters;
	}
}
