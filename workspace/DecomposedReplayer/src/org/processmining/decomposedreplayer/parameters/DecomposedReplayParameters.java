package org.processmining.decomposedreplayer.parameters;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfigurationManager;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters.Type;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.pnetreplayer.parameters.TransEvClassMappingParameter;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class DecomposedReplayParameters extends PluginParametersImpl implements ClassifierParameter, TransEvClassMappingParameter {

	private XEventClassifier classifier;
	private TransEvClassMapping mapping;
	private boolean combineClusters;
	private boolean discoverMatrixFromNet;
	private String configuration;
	private boolean tryConnections;
	private int moveOnLogCosts;
	private int moveOnModelCosts;
	private int percentage;
	private Set<XEventClass> unsplittableActivities;
	private long deadline;
	private boolean addConflictOnlyOnce;
	private boolean preferBorderTransitions;
	
	private Type algorithmType;
	private boolean moveSort;
	private boolean queueSort;
	private boolean preferExact;
	private int nThreads;
	private boolean useInt;
	private Debug debug;
	private String outputDir;
	private int timeoutPerTraceInSecs;
	private int maximumNumberOfStates;
	private int costUpperBound;
	private boolean partiallyOrderEvents;
	private boolean preProcessUsingPlaceBasedConstraints;
	private int initialSplits;
	private boolean printAlignments;

	public DecomposedReplayParameters(XLog log, AcceptingPetriNet net, XEventClassifier classifier,
			Type algorithmType, boolean moveSort, boolean queueSort, boolean preferExact, 
			int nThreads, boolean useInt, Debug debug, String outputDir, int timeoutPerTraceInSecs,
			int maximumNumberOfStates, int costUpperBound, boolean partiallyOrderEvents, 
			boolean preProcessUsingPlaceBasedConstraints, int initialSplits, boolean printAlignments) {
		this(log, net, classifier);
		
		setAlgorithmType(algorithmType);
		setMoveSort(moveSort);
		setQueueSort(queueSort);
		setPreferExact(preferExact);
		setnThreads(nThreads);
		setUseInt(useInt);
		setDebug(debug);
		setOutputDir(outputDir);
		setTimeoutPerTraceInSecs(timeoutPerTraceInSecs);
		setMaximumNumberOfStates(maximumNumberOfStates);
		setCostUpperBound(costUpperBound);
		setPartiallyOrderEvents(partiallyOrderEvents);
		setPreProcessUsingPlaceBasedConstraints(preProcessUsingPlaceBasedConstraints);
		setInitialSplits(initialSplits);
		setPrintAlignments(printAlignments);
	}

	public DecomposedReplayParameters(XLog log, AcceptingPetriNet net, XEventClassifier classifier) {
		setClassifier(classifier);
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		Set<XEventClass> activities = new HashSet<XEventClass>();
		activities.addAll(info.getEventClasses().getClasses());
		activities.add(XUtils.MOVEONMODELACTIVITY);
		setMapping(TransEvClassMappingUtils.getInstance().getMapping(net.getNet(), activities, classifier));
		setCombineClusters(false);
		setDiscoverMatrixFromNet(true);
		setConfiguration(DecomposedReplayConfigurationManager.getInstance().getConfiguration(null).getName());
		setMoveOnModelCosts(4);
		setMoveOnLogCosts(10);
		setTryConnections(true);
		setPercentage(100);
		setDeadline(10 * 60 * 1000);
		setUnsplittableActivities(new HashSet<XEventClass>());
		setAddConflictOnlyOnce(false);
		setPreferBorderTransitions(false);
	}

	public DecomposedReplayParameters(XLog log, AcceptingPetriNet net) {
		this(log, net, XUtils.getDefaultClassifier(log));
	}

	public DecomposedReplayParameters(DecomposedReplayParameters parameters) {
		setClassifier(parameters.getClassifier());
		setMapping(parameters.getMapping());
		setCombineClusters(parameters.isCombineClusters());
		setDiscoverMatrixFromNet(parameters.isDiscoverMatrixFromNet());
		setConfiguration(parameters.getConfiguration());
		setMoveOnModelCosts(10);
		setMoveOnLogCosts(10);
		setTryConnections(parameters.isTryConnections());
		setPercentage(parameters.getPercentage());
		setDeadline(parameters.getDeadline());
		setUnsplittableActivities(parameters.getUnsplittableActivities());
		setAddConflictOnlyOnce(parameters.isAddConflictOnlyOnce());
		setPreferBorderTransitions(parameters.isPreferBorderTransitions());
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setMapping(TransEvClassMapping mapping) {
		this.mapping = mapping;
	}

	public TransEvClassMapping getMapping() {
		return mapping;
	}

	public boolean equals(Object object) {
		if (object instanceof DecomposedReplayParameters) {
			DecomposedReplayParameters parameters = (DecomposedReplayParameters) object;
			return getClassifier().equals(parameters.getClassifier()) && getMapping().equals(parameters.getMapping())
					&& isCombineClusters() == parameters.isCombineClusters()
					&& isDiscoverMatrixFromNet() == parameters.isDiscoverMatrixFromNet()
					&& getConfiguration().equals(parameters.getConfiguration())
					&& isTryConnections() == parameters.isTryConnections()
					&& getPercentage() == parameters.getPercentage()
					&& getDeadline() == parameters.getDeadline();
		}
		return false;
	}

	//	public CostBasedCompleteParam createReplayParameters(Collection<XEventClass> activities, XEventClass invisibleActivity, AcceptingPetriNet net) {
	//		CostBasedCompleteParam parameters = new CostBasedCompleteParam(activities, new XEventClass(TransEvClassMappingPanel.INVISIBLE,
	//				activities.size()), net.getNet().getTransitions());
	//		parameters.setInitialMarking(net.getInitialMarking());
	//		Set<Marking> finalMarkings = net.getFinalMarkings();
	//		if (finalMarkings.isEmpty()) {
	//			finalMarkings = new HashSet<Marking>();
	//			finalMarkings.add(new Marking());
	//		}
	//		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
	//		return parameters;
	//	}
	//	
	public void setCombineClusters(boolean combineClusters) {
		this.combineClusters = combineClusters;
	}

	public boolean isCombineClusters() {
		return combineClusters;
	}

	public boolean isDiscoverMatrixFromNet() {
		return discoverMatrixFromNet;
	}

	public void setDiscoverMatrixFromNet(boolean discoverMatrixFromNet) {
		this.discoverMatrixFromNet = discoverMatrixFromNet;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public boolean isTryConnections() {
		return tryConnections;
	}

	public void setTryConnections(boolean tryConnections) {
		this.tryConnections = tryConnections;
	}

	public int getMoveOnLogCosts() {
		return moveOnLogCosts;
	}

	public void setMoveOnLogCosts(int moveOnLogCosts) {
		this.moveOnLogCosts = moveOnLogCosts;
	}

	public int getMoveOnModelCosts() {
		return moveOnModelCosts;
	}

	public void setMoveOnModelCosts(int moveOnModelCosts) {
		this.moveOnModelCosts = moveOnModelCosts;
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public Set<XEventClass> getUnsplittableActivities() {
		return unsplittableActivities;
	}

	public void setUnsplittableActivities(Set<XEventClass> unsplittableActivities) {
		this.unsplittableActivities = unsplittableActivities;
	}

	public long getDeadline() {
		return deadline;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	public boolean isAddConflictOnlyOnce() {
		return addConflictOnlyOnce;
	}

	public void setAddConflictOnlyOnce(boolean addConflictOnlyOnce) {
		this.addConflictOnlyOnce = addConflictOnlyOnce;
	}

	public boolean isPreferBorderTransitions() {
		return preferBorderTransitions;
	}

	public void setPreferBorderTransitions(boolean preferBorderTransitions) {
		this.preferBorderTransitions = preferBorderTransitions;
	}

	public Type getAlgorithmType() {
		return algorithmType;
	}

	public void setAlgorithmType(Type algorithmType) {
		this.algorithmType = algorithmType;
	}

	public boolean isMoveSort() {
		return moveSort;
	}

	public void setMoveSort(boolean moveSort) {
		this.moveSort = moveSort;
	}

	public boolean isQueueSort() {
		return queueSort;
	}

	public void setQueueSort(boolean queueSort) {
		this.queueSort = queueSort;
	}

	public boolean isPreferExact() {
		return preferExact;
	}

	public void setPreferExact(boolean preferExact) {
		this.preferExact = preferExact;
	}

	public int getnThreads() {
		return nThreads;
	}

	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}

	public boolean isUseInt() {
		return useInt;
	}

	public void setUseInt(boolean useInt) {
		this.useInt = useInt;
	}

	public Debug getDebug() {
		return debug;
	}

	public void setDebug(Debug debug) {
		this.debug = debug;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public int getTimeoutPerTraceInSecs() {
		return timeoutPerTraceInSecs;
	}

	public void setTimeoutPerTraceInSecs(int timeoutPerTraceInSecs) {
		this.timeoutPerTraceInSecs = timeoutPerTraceInSecs;
	}

	public int getMaximumNumberOfStates() {
		return maximumNumberOfStates;
	}

	public void setMaximumNumberOfStates(int maximumNumberOfStates) {
		this.maximumNumberOfStates = maximumNumberOfStates;
	}

	public int getCostUpperBound() {
		return costUpperBound;
	}

	public void setCostUpperBound(int costUpperBound) {
		this.costUpperBound = costUpperBound;
	}

	public boolean isPartiallyOrderEvents() {
		return partiallyOrderEvents;
	}

	public void setPartiallyOrderEvents(boolean partiallyOrderEvents) {
		this.partiallyOrderEvents = partiallyOrderEvents;
	}

	public boolean isPreProcessUsingPlaceBasedConstraints() {
		return preProcessUsingPlaceBasedConstraints;
	}

	public void setPreProcessUsingPlaceBasedConstraints(boolean preProcessUsingPlaceBasedConstraints) {
		this.preProcessUsingPlaceBasedConstraints = preProcessUsingPlaceBasedConstraints;
	}

	public int getInitialSplits() {
		return initialSplits;
	}

	public void setInitialSplits(int initialSplits) {
		this.initialSplits = initialSplits;
	}

	public boolean isPrintAlignments() {
		return printAlignments;
	}

	public void setPrintAlignments(boolean printAlignments) {
		this.printAlignments = printAlignments;
	}

}
