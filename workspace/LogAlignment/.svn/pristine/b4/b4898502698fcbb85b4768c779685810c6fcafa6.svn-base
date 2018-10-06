package org.processmining.logalignment.parameters;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.models.LogAlignmentArray;

public class MergeLogAlignmentArrayIntoLogAlignmentParameters extends PluginParametersImpl implements ClassifierParameter {

	public final static int LOWESTCOST = 0;
	public final static int HIGHESTCOST = 1;
	public final static int FIRST = 2;
	public final static int RANDOM = 3;
	
	private XEventClassifier classifier;
	private int strategy;
	private boolean pseudoUnreliable;
	private boolean addConflictOnlyOnce;
	/*
	 * HV 20180425: Fixed bug: Add costs for log moves on activities not covered by model.
	 */
	private int moveOnLogCosts;	

	public MergeLogAlignmentArrayIntoLogAlignmentParameters(XLog log, LogAlignmentArray alignments) {
		super();
		setClassifier(XUtils.getDefaultClassifier(log));
		setStrategy(HIGHESTCOST);
		setPseudoUnreliable(false);
		setAddConflictOnlyOnce(false);
		setMoveOnLogCosts(0);
	}

	public MergeLogAlignmentArrayIntoLogAlignmentParameters(MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		super(parameters);
		setClassifier(parameters.getClassifier());
		setStrategy(parameters.getStrategy());
		setPseudoUnreliable(parameters.isPseudoUnreliable());
		setAddConflictOnlyOnce(parameters.isAddConflictOnlyOnce());
		setMoveOnLogCosts(parameters.getMoveOnLogCosts());
	}

	public boolean equals(Object object) {
		if (object instanceof MergeLogAlignmentArrayIntoLogAlignmentParameters) {
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters = (MergeLogAlignmentArrayIntoLogAlignmentParameters) object;
			return super.equals(parameters)
					&& getClassifier().equals(parameters.getClassifier())
					&& getStrategy() == parameters.getStrategy()
					&& isPseudoUnreliable() == parameters.isPseudoUnreliable()
					&& isAddConflictOnlyOnce() == parameters.isAddConflictOnlyOnce()
					&& getMoveOnLogCosts() == parameters.getMoveOnLogCosts();
		}
		return false;
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public int getStrategy() {
		return strategy;
	}

	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	public boolean isPseudoUnreliable() {
		return pseudoUnreliable;
	}

	public void setPseudoUnreliable(boolean pseudoUnreliable) {
		this.pseudoUnreliable = pseudoUnreliable;
	}

	public boolean isAddConflictOnlyOnce() {
		return addConflictOnlyOnce;
	}

	public void setAddConflictOnlyOnce(boolean addConflictOnlyOnce) {
		this.addConflictOnlyOnce = addConflictOnlyOnce;
	}

	public int getMoveOnLogCosts() {
		return moveOnLogCosts;
	}

	public void setMoveOnLogCosts(int moveOnLogCosts) {
		this.moveOnLogCosts = moveOnLogCosts;
	}
}
