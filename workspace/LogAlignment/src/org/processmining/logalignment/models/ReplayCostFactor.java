package org.processmining.logalignment.models;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.framework.annotations.AuthoredType;
import org.processmining.framework.annotations.Icon;
import org.processmining.framework.util.HTMLToString;

@AuthoredType(typeName = "Replay cost factor", affiliation = AuthoredType.TUE, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
@Icon(icon = "resourcetype_rcf_30x35.png")
public interface ReplayCostFactor extends HTMLToString {

	public void init(String label, ActivityClusterArray clusters);

	public int getCostFactor(XEventClass activity);
	
	public int getCostBase();
}
