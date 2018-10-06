package org.processmining.logalignment.models.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.Pair;
import org.processmining.logalignment.models.TraceAlignment;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class TraceAlignmentImpl implements TraceAlignment {

	/*
	 * We only support step types LMGOOD (sync move), L (log move), and MREAL
	 * (model move).
	 */
	private List<Pair<StepTypes, Pair<XEventClass, String>>> legalMoves;
	private double costs;
	private double maxCosts;
	private double decomposedSyncMoveNumerator;
	private double decomposedSyncMoveDenominator;
	private boolean reliable;
	private double millis;
	private double stateCount;
	private double queuedStates;
	private double traversedArcs;
	
	/*
	 * Event labels that resulted in the trace having no TBA.
	 */
	private Map<XEventClass, Double> conflicts;

	protected TraceAlignmentImpl() {
		conflicts = new HashMap<XEventClass, Double>();
	}

	public void init() {
		legalMoves = new ArrayList<Pair<StepTypes, Pair<XEventClass, String>>>();
		setCosts(0.0);
		setDecomposedSyncMoveNumerator(-1.0); // Any value < 0.0 means 'not set'.
		setDecomposedSyncMoveDenominator(-1.0); // Any value < 0.0 means 'not set'.
		setReliable(false);
		setStateCount(0.0);
		setQueuedStates(0.0);
		setTraversedArcs(0.0);
	}

	public List<Pair<StepTypes, Pair<XEventClass, String>>> getLegalMoves() {
		return legalMoves;
	}

	public Pair<StepTypes, Pair<XEventClass, String>> getLegalMove(int index) {
		return legalMoves.get(index);
	}

	public void setLegalMoves(List<Pair<StepTypes, Pair<XEventClass, String>>> legalMoves) {
		this.legalMoves = legalMoves;
	}

	public void addLegalMove(StepTypes stepType, XEventClass activity, String transitionId) {
		legalMoves.add(new Pair<StepTypes, Pair<XEventClass, String>>(stepType, new Pair<XEventClass, String>(activity,
				transitionId)));
	}

	public int getSize() {
		return legalMoves.size();
	}

	public List<XEventClass> getLogMoves() {
		List<XEventClass> trace = new ArrayList<XEventClass>();
		for (Pair<StepTypes, Pair<XEventClass, String>> legalMove : legalMoves) {
			if (legalMove.getFirst() == StepTypes.LMGOOD || legalMove.getFirst() == StepTypes.L) {
				trace.add(legalMove.getSecond().getFirst());
			}
		}
		return trace;
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
			buffer.append("<h1>Trace alignment</h1>");
		}
		buffer.append("<table><tr>");
		for (int index = 0; index < legalMoves.size(); index++) {
			if (legalMoves.get(index).getFirst() == StepTypes.LMGOOD || legalMoves.get(index).getFirst() == StepTypes.L) {
				buffer.append("<td>" + legalMoves.get(index).getSecond().getFirst() + "</td>");
			} else {
				buffer.append("<td>&gt;&gt;</td>");
			}
		}
		buffer.append("</tr><tr>");
		for (int index = 0; index < legalMoves.size(); index++) {
			if (legalMoves.get(index).getFirst() == StepTypes.LMGOOD
					|| legalMoves.get(index).getFirst() == StepTypes.MREAL
				|| legalMoves.get(index).getFirst() == StepTypes.MINVI) {
				buffer.append("<td>" + legalMoves.get(index).getSecond().getSecond() + "</td>");
			} else {
				buffer.append("<td>&gt;&gt;</td>");
			}
		}
		buffer.append("</tr></table>");
		buffer.append("<h2>Costs</h2>" + getCosts());
		buffer.append("<h2>Max costs</h2>" + getMaxCosts());
		buffer.append("<h2>Decomposed Sync-move Numerator</h2>" + getDecomposedSyncMoveNumerator());
		buffer.append("<h2>Decomposed Sync-move Denominator</h2>" + getDecomposedSyncMoveDenominator());
		buffer.append("<h2>Reliable?</h2>" + isReliable());
		buffer.append("<h2>State count</h2>" + getStateCount());
		buffer.append("<h2>Queued states</h2>" + getQueuedStates());
		buffer.append("<h2>Traversed arcs</h2>" + getTraversedArcs());
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();
	}

	public void exportToCSVFile(CsvWriter writer) throws IOException {
		writer.write("COSTS");
		writer.write(Double.toString(getCosts()));
		for (Pair<StepTypes, Pair<XEventClass, String>> legalMove : legalMoves) {
			switch (legalMove.getFirst()) {
				case LMGOOD :
					writer.write("LMGOOD");
					writer.write(legalMove.getSecond().getFirst().getId());
					writer.write(legalMove.getSecond().getSecond());
					break;
				case L :
					writer.write("L");
					writer.write(legalMove.getSecond().getFirst().getId());
					writer.write("N/A");
					break;
				case MREAL :
					writer.write("MREAL");
					writer.write("N/A");
					writer.write(legalMove.getSecond().getSecond());
					break;
				case MINVI :
					writer.write("MINVI");
					writer.write("N/A");
					writer.write(legalMove.getSecond().getSecond());
					break;
				default :
					writer.write("");
					break;
			}
		}
		writer.write("END");
		writer.endRecord();
	}

	public void importFromCSVFile(CsvReader reader, Map<String, XEventClass> activities) throws IOException {
		init();
		for (int i = 0; i < reader.getColumnCount();) {
			String tag = reader.get(i++);
			if (tag.equals("LMGOOD") || tag.equals("L") || tag.equals("MREAL") || tag.equals("MINVI")) {
				if (i + 1 < reader.getColumnCount()) {
					String activityId = reader.get(i++);
					String transitionId = reader.get(i++);
					if (!activities.keySet().contains(activityId)) {
						activities.put(activityId, new XEventClass(activityId, activities.keySet().size()));
					}
					XEventClass activity = activities.get(activityId);
					if (tag.equals("LMGOOD")) {
						addLegalMove(StepTypes.LMGOOD, activity, transitionId);
					} else if (tag.equals("L")) {
						addLegalMove(StepTypes.L, activity, null);
					} else if (tag.equals("MREAL")) {
						addLegalMove(StepTypes.MREAL, null, transitionId);
					} else if (tag.equals("MINVI")) {
						addLegalMove(StepTypes.MINVI, null, transitionId);
					} else {
						// Ignore
					}
				}
			} else if (tag.equals("COSTS")) {
				if (i < reader.getColumnCount()) {
					Double costs = Double.parseDouble(reader.get(i++));
					setCosts(costs);
				}
			}
		}
	}

	public void setCosts(double costs) {
		this.costs = costs;
	}

	public double getCosts() {
		return costs;
	}

	public boolean isReliable() {
		return reliable;
	}

	public void setReliable(boolean isReliable) {
		this.reliable = isReliable;
	}

	public void addConflictingActivity(XEventClass activity) {
		conflicts.put(activity, (conflicts.containsKey(activity) ? conflicts.get(activity) + 1.0 : 1.0));
	}
	
	public Map<XEventClass, Double> getConflictingActivities() {
		return conflicts;
	}

	public void setMillis(double millis) {
		this.millis = millis;
	}

	public double getMillis() {
		return millis;
	}

	public void setMaxCosts(double costs) {
		this.maxCosts = costs;
	}

	public double getMaxCosts() {
		return maxCosts;
	}

	public double getDecomposedSyncMoveNumerator() {
		return decomposedSyncMoveNumerator;
	}

	public void setDecomposedSyncMoveNumerator(double numerator) {
		decomposedSyncMoveNumerator = numerator;		
	}

	public double getDecomposedSyncMoveDenominator() {
		return decomposedSyncMoveDenominator;
	}

	public void setDecomposedSyncMoveDenominator(double denominator) {
		decomposedSyncMoveDenominator = denominator;
	}
	
	public double getStateCount() {
		return stateCount;
	}

	public void setStateCount(double stateCount) {
		this.stateCount = stateCount;
	}

	public double getQueuedStates() {
		return queuedStates;
	}

	public void setQueuedStates(double queuedStates) {
		this.queuedStates = queuedStates;
	}

	public double getTraversedArcs() {
		return traversedArcs;
	}

	public void setTraversedArcs(double traversedArcs) {
		this.traversedArcs = traversedArcs;
	}
}
