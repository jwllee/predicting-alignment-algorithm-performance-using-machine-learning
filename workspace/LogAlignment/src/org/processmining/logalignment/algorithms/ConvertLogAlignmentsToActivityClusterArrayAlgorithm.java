package org.processmining.logalignment.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.logalignment.models.LogAlignmentArray;

public class ConvertLogAlignmentsToActivityClusterArrayAlgorithm {

	public ActivityClusterArray apply(LogAlignmentArray logAlignments) {
		Set<XEventClass> activities = new HashSet<XEventClass>(logAlignments.getSize());
		for (int index = 0; index < logAlignments.getSize(); index++) {
			activities.addAll(logAlignments.getAlignment(index).getCluster());
		}
		ActivityClusterArray clusters = ActivityClusterArrayFactory.createActivityClusterArray();
		clusters.init("", activities);
		for (int index = 0; index < logAlignments.getSize(); index++) {
			clusters.addCluster(logAlignments.getAlignment(index).getCluster());
		}
		return clusters;
	}
}
