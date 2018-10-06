package org.processmining.logalignment.parameters;

@Deprecated
public class ConnectionParameters {

	private boolean tryConnections;

	public ConnectionParameters() {
		setTryConnections(true);
	}

	public ConnectionParameters(ConnectionParameters parameters) {
		setTryConnections(parameters.isTryConnections());
	}

	public boolean isTryConnections() {
		return tryConnections;
	}

	public void setTryConnections(boolean tryConnections) {
		this.tryConnections = tryConnections;
	}
}
