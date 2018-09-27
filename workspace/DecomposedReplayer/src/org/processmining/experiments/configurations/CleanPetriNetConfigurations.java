package org.processmining.experiments.configurations;

public class CleanPetriNetConfigurations {

	public String netDirtyPath = "";
    public String netCleanPath = "";
	
    @Override
    public String toString() {
    	return "Dirty net path: " + netDirtyPath + "\n" + 
    			"Clean net path: " + netCleanPath;
    }
}
