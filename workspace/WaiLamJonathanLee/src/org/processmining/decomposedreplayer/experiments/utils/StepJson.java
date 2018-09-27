package org.processmining.decomposedreplayer.experiments.utils;

public class StepJson {

	private String log;
	private String model;
	private String type;
	
	public StepJson(String log, String model, String type) {
		this.log = log;
		this.model = log;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return String.format("%s|%s|%s", log, model, type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
}
