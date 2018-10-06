package org.processmining.logalignment.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class MergeReplayResultArrayIntoReplayResultParameters extends PluginParametersImpl {

	public MergeReplayResultArrayIntoReplayResultParameters() {
		super();
	}
	
	public MergeReplayResultArrayIntoReplayResultParameters(MergeReplayResultArrayIntoReplayResultParameters parameters) {
		super(parameters);
	}
	
	public boolean equals(Object object) {
		if (object instanceof MergeReplayResultArrayIntoReplayResultParameters) {
			MergeReplayResultArrayIntoReplayResultParameters parameters = (MergeReplayResultArrayIntoReplayResultParameters) object;
			return super.equals(parameters);
		}
		return false;
	}
}
