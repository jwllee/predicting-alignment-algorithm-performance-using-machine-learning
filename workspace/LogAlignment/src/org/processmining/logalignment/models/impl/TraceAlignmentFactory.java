package org.processmining.logalignment.models.impl;

import org.processmining.logalignment.models.TraceAlignment;

public class TraceAlignmentFactory {

	public static TraceAlignment createTraceAlignment() {
		return new TraceAlignmentImpl();
	}
	
}
