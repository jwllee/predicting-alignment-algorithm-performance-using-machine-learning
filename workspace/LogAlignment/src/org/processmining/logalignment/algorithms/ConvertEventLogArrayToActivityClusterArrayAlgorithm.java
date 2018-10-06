package org.processmining.logalignment.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;

public class ConvertEventLogArrayToActivityClusterArrayAlgorithm {

	public ActivityClusterArray apply(EventLogArray logs, ConvertReplayResultToLogAlignmentParameters parameters) {
		Set<XEventClass> activities = new HashSet<XEventClass>(logs.getSize());
		for (int index = 0; index < logs.getSize(); index++) {
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), parameters.getClassifier());
			activities.addAll(info.getEventClasses().getClasses());
		}
		ActivityClusterArray clusters = ActivityClusterArrayFactory.createActivityClusterArray();
		clusters.init("", activities);
		for (int index = 0; index < logs.getSize(); index++) {
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), parameters.getClassifier());
			clusters.addCluster(new HashSet<XEventClass>(info.getEventClasses().getClasses()));
		}
		return clusters;
	}
}
