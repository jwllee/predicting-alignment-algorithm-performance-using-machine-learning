package org.processmining.logalignment.models.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.basicutils.utils.HTMLUtils;
import org.processmining.logalignment.models.ReplayCostFactor;

public class ReplayCostFactorImpl implements ReplayCostFactor {

	private String label;
	private Set<XEventClass> sortedActivities;
	private Map<XEventClass, Integer> activityCost;
	
	private int factor = 30030; // 210 = 2*3*5*7*11*13
	
	public ReplayCostFactorImpl() {
		label = "";
		sortedActivities = new TreeSet<XEventClass>();
		activityCost = new HashMap<XEventClass, Integer>();
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		buffer.append("<h1>Replay Cost for \"" + HTMLUtils.encode(label) + "\"</h1>");
		buffer.append("<table>");
		buffer.append("<tr>");
		buffer.append("<th>Activity</th><th>Cost</th>");
		buffer.append("</tr>");
		for (XEventClass activity : sortedActivities) {
			buffer.append("<tr>");
			buffer.append("<th>" + activity.getId() + "</th>");
			buffer.append("<td align=\"center\">" + getCostFactor(activity) + "</td>");
			buffer.append("</tr>");
		}
		buffer.append("<tr>");
		buffer.append("<th>Factor</th>");
		buffer.append("<th align=\"center\">" + getCostBase() + "</th>");
		buffer.append("</tr>");
		buffer.append("</table>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}

	public void init(String label, ActivityClusterArray clusters) {
		this.label = label;
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			sortedActivities.addAll(cluster);
		}
		Map<XEventClass, Integer> count = new HashMap<XEventClass, Integer>();
		for (XEventClass activity : sortedActivities) {
			count.put(activity, 0);
		}
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			for (XEventClass activity : cluster) {
				count.put(activity, count.get(activity) + 1);
			}
		}
		Set<Integer> counts = new HashSet<Integer>();
		for (XEventClass activity : sortedActivities) {
			counts.add(count.get(activity));
		}
		factor = lcm(counts);
		System.out.println("[ReplayCostFactorImpl] Factor = " + factor);
		for (XEventClass activity : sortedActivities) {
			activityCost.put(activity, factor / count.get(activity));
		}
	}

	public int getCostFactor(XEventClass activity) {
		return activityCost.containsKey(activity) ? activityCost.get(activity) : 0;
	}

	public int getCostBase() {
		return factor;
	}

	private int gcd(int a, int b) {
		while (b > 0) {
			int temp = b;
			b = a % b; // % is remainder
			a = temp;
		}
		return a;
	}

	private int lcm(int a, int b) {
		return a * (b / gcd(a, b));
	}

	private int lcm(Collection<Integer> inputs) {
		int result = 0;
		for (int input : inputs) {
			result = (result == 0 ? input : lcm(result, input));
		}
		return result;
	}
}
