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


public class ConvertReplayResultToLogAlignmentParameters extends PluginParametersImpl implements ClassifierParameter, TransEvClassMappingParameter {

	private XEventClassifier classifier;
	private TransEvClassMapping map;
	
	public ConvertReplayResultToLogAlignmentParameters(EventLogArray logs, AcceptingPetriNetArray nets) {
		this(logs, nets, XUtils.getDefaultClassifier(logs.getLog(0)));
	}
	
	public ConvertReplayResultToLogAlignmentParameters(EventLogArray logs, AcceptingPetriNetArray nets, XEventClassifier classifier) {
		super();
		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
		
		setClassifier(classifier);
		
		XEventClass invisibleActivity = TransEvClassMappingUtils.getInstance().getInvisibleActivity();
		setMapping(new TransEvClassMapping(getClassifier(), invisibleActivity));
		Set<String> labels = new HashSet<String>();
		
		for (int index = 0; index < size; index++) {
			Set<XEventClass> activities = new HashSet<XEventClass>();
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), getClassifier());
			activities.addAll(info.getEventClasses().getClasses());
			TransEvClassMapping subMap = TransEvClassMappingUtils.getInstance().getMapping(nets.getNet(index).getNet(), activities, getClassifier());
			for (Transition transition : subMap.keySet()) {
				if (!labels.contains(transition.getLabel())) {
					getMapping().put(transition, subMap.get(transition));
					labels.add(transition.getLabel());
				}
			}
		}
		setTryConnections(true);
	}

	public ConvertReplayResultToLogAlignmentParameters(ConvertReplayResultToLogAlignmentParameters parameters) {
		super(parameters);
		setClassifier(parameters.getClassifier());
		setMapping(parameters.getMapping());
	}
	
	public boolean equals(Object object) {
		if (object instanceof ConvertReplayResultToLogAlignmentParameters) {
			ConvertReplayResultToLogAlignmentParameters parameters = (ConvertReplayResultToLogAlignmentParameters) object;
			return super.equals(parameters)
					&& getClassifier().equals(parameters.getClassifier()) 
					&& getMapping().equals(parameters.getMapping());
		}
		return false;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}
	
	public void setMapping(TransEvClassMapping mapping) {
		this.map = mapping;
	}
	
	public TransEvClassMapping getMapping() {
		return map;
	}
}
