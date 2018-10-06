package org.processmining.logalignment.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class FilterLogAlignmentParameters extends PluginParametersImpl {
	
	public FilterLogAlignmentParameters() {
		super();
	}
	
	public FilterLogAlignmentParameters(FilterLogAlignmentParameters parameters) {
		super(parameters);
	}
	
	public boolean equals(Object object) {
		if (object instanceof FilterLogAlignmentParameters) {
			FilterLogAlignmentParameters parameters = (FilterLogAlignmentParameters) object;
			return super.equals(parameters);
		}
		return false;
	}

}
