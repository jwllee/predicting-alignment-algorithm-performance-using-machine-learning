package org.processmining.logalignment.parameters;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.pnetreplayer.parameters.TransEvClassMappingParameter;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

public class ReplayEventLogArrayOnAcceptingPetriNetArrayParameters extends PluginParametersImpl implements ClassifierParameter, TransEvClassMappingParameter {

	private XEventClassifier classifier;
	private TransEvClassMapping mapping;
	private int moveOnLogCosts;
	private int moveOnModelCosts;
	private long deadline;
	
//	private List<List<XEventClassifier>> classifiers;
//	private List<TransEvClassMapping> mappings;
//	protected List<PetrinetReplayerWithILP> replayers;
//	protected List<CostBasedCompleteParam> replayParameters;

	public ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(EventLogArray logs, AcceptingPetriNetArray nets) {
		this(logs, nets, XUtils.getDefaultClassifier(logs.getLog(0)));
	}
	
	public ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(EventLogArray logs, AcceptingPetriNetArray nets, XEventClassifier classifier) {
		super();
		setClassifier(classifier);

		XEventClass invisibleActivity = TransEvClassMappingUtils.getInstance().getInvisibleActivity();
		setMapping(new TransEvClassMapping(getClassifier(), invisibleActivity));
		Set<String> labels = new HashSet<String>();
		
		setMoveOnLogCosts(5);
		setMoveOnModelCosts(2);
		setDeadline(10 * 60 * 1000); // By default, the deadline is 10 minutes.
		
		for (int index = 0; index < logs.getSize(); index++) {
			Set<XEventClass> activities = new HashSet<XEventClass>();
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), getClassifier());
			activities.addAll(info.getEventClasses().getClasses());
			activities.add(XUtils.MOVEONMODELACTIVITY);
			TransEvClassMapping subMap = TransEvClassMappingUtils.getInstance().getMapping(nets.getNet(index).getNet(), activities, getClassifier());
			for (Transition transition : subMap.keySet()) {
				if (!labels.contains(transition.getLabel())) {
					getMapping().put(transition, subMap.get(transition));
					labels.add(transition.getLabel());
				}
			}
		}
		setTryConnections(true);

//		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
//		classifiers = new ArrayList<List<XEventClassifier>>();
//		mappings = new ArrayList<TransEvClassMapping>();
//		replayers = new ArrayList<PetrinetReplayerWithILP>();
//		replayParameters = new ArrayList<CostBasedCompleteParam>();
		
//		List<XEventClassifier> availableClassifiers = new ArrayList<XEventClassifier>();
//		for (int i = 0; i < logs.getSize(); i++) {
//			if (i == 0) {
//				availableClassifiers.addAll(logs.getLog(i).getClassifiers());
//			} else {
//				availableClassifiers.retainAll(logs.getLog(i).getClassifiers());
//			}
//		}
//		if (availableClassifiers.isEmpty()) {
//			availableClassifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
//		}
//		classifier = availableClassifiers.get(0);
//		for (int index = 0; index < size; index++) {
//			classifiers.add(index, new ArrayList<XEventClassifier>());
//			if (logs.getLog(index).getClassifiers().isEmpty()) {
//				setClassifier(index, new XEventAndClassifier(new XEventNameClassifier(),
//						new XEventLifeTransClassifier()));
//			} else {
//				setClassifier(index, logs.getLog(0).getClassifiers().iterator().next());
//			}
//			Set<XEventClass> activities = new HashSet<XEventClass>();
//			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), getClassifier(index));
//			activities.addAll(info.getEventClasses().getClasses());
//			mappings.add(index, TransEvClassMappingUtils.getInstance().getMapping(nets.getNet(index).getNet(), activities, getClassifier(index)));
//			replayers.add(index, new PetrinetReplayerWithILP());
//			replayParameters.add(index, createReplayParameters(activities, new XEventClass(TransEvClassMappingPanel.INVISIBLE,
//					info.getEventClasses().size()), nets.getNet(index)));
//		}
	}
	
	public ReplayEventLogArrayOnAcceptingPetriNetArrayParameters(ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters) {
		super(parameters);
		setClassifier(parameters.getClassifier());
		setMapping(parameters.getMapping());
		setDeadline(parameters.getDeadline());
	}
	
	public boolean equals(Object object) {
		if (object instanceof ReplayEventLogArrayOnAcceptingPetriNetArrayParameters) {
			ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters = (ReplayEventLogArrayOnAcceptingPetriNetArrayParameters) object;
			return super.equals(parameters)
					&& getMoveOnModelCosts() == parameters.getMoveOnModelCosts()
					&& getMoveOnLogCosts() == parameters.getMoveOnLogCosts()
					&& getClassifier().equals(parameters.getClassifier()) 
					&& getMapping().equals(parameters.getMapping())
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

	public long getDeadline() {
		return deadline;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

//	public void setClassifier(int index, XEventClassifier classifier) {
//		classifiers.get(index).clear();
//		classifiers.get(index).add(classifier);
//	}
//
//	public XEventClassifier getClassifier(int index) {
//		return classifiers.get(index).get(0);
//	}
//
//	public List<XEventClassifier> getClassifiers(int index) {
//		return classifiers.get(index);
//	}
//
//	public void setMapping(int index, TransEvClassMapping mapping) {
//		mappings.remove(index);
//		mappings.add(index, mapping);
//	}
//
//	public TransEvClassMapping getMapping(int index) {
//		return mappings.get(index);
//	}
//
//	public void setReplayer(int index, PetrinetReplayerWithILP replayer) {
//		replayers.remove(index);
//		replayers.add(index, replayer);
//	}
//
//	public PetrinetReplayerWithILP getReplayer(int index) {
//		return replayers.get(index);
//	}
//
//	public void setReplayParameters(int index, CostBasedCompleteParam parameters) {
//		replayParameters.remove(index);
//		replayParameters.add(index, parameters);
//	}
//
//	public CostBasedCompleteParam getReplayParameters(int index) {
//		return replayParameters.get(index);
//	}

}
