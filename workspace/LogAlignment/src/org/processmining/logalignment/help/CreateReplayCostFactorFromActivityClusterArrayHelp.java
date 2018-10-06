package org.processmining.logalignment.help;

public class CreateReplayCostFactorFromActivityClusterArrayHelp {

	public final static String TEXT = ""
			+ "Creates a cost factor for the given activity cluster array. "
			+ "This cost factor will be such that, for any activity, the number of clusters that include this activity "
			+ "divides this factor. "
			+ "Note that this cost factor is needed because of the fact that a cost in the replayer should be a natural number, "
			+ "and that costs of an activity will be divided equally over all clusters that contain this activity. "
			+ "For this reason, all costs will first be multiplied by this factor, which allows them to be divided, "
			+ "and later on (when real numbers are used) they will be divided by this factor.";
}
