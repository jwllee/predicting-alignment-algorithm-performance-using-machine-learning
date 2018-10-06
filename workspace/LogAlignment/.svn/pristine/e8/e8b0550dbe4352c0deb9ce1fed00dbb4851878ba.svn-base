package org.processmining.logalignment.connections;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logalignment.parameters.MergeReplayResultArrayIntoReplayResultParameters;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class MergeReplayResultArrayIntoReplayResultConnection extends AbstractConnection {

	public final static String REPLAYS = "Replays";
	public final static String REPLAY = "Replay";

	private MergeReplayResultArrayIntoReplayResultParameters parameters;

	public MergeReplayResultArrayIntoReplayResultConnection(ReplayResultArray replays,
			PNRepResult replay, MergeReplayResultArrayIntoReplayResultParameters parameters) {
		super("Merge Replay Result Array Into Replay Result Connection");
		put(REPLAYS, replays);
		put(REPLAY, replay);
		this.parameters = new MergeReplayResultArrayIntoReplayResultParameters(parameters);
	}

	public MergeReplayResultArrayIntoReplayResultParameters getParameters() {
		return parameters;
	}
}

