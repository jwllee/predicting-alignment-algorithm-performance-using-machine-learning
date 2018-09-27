package org.processmining.experiments.parameters;

public class TestSharedActivitiesParameters {
	
	public String netArrayFilePath = "";
	public String sharedActsFilePath = "";

	@Override 
	public String toString() {
		
		return "Net array file path: " + netArrayFilePath + "\n" + 
				"Shared activities file path: " + sharedActsFilePath;
		
	}
	
}
