package org.processmining.logalignment.parameters;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.log.utils.XUtils;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.pnetreplayer.parameters.TransEvClassMappingParameter;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

public class ConvertLogAlignmentToReplayResultParameters extends PluginParametersImpl implements ClassifierParameter, TransEvClassMappingParameter {

	private XEventClassifier classifier;
	private TransEvClassMapping map;

	public ConvertLogAlignmentToReplayResultParameters(XLog log, AcceptingPetriNet net, XEventClassifier classifier) {
		super();
		setClassifier(classifier);
		XLogInfo info = XLogInfoFactory.createLogInfo(log, getClassifier());
		Set<XEventClass> activities = new HashSet<XEventClass>(info.getEventClasses().getClasses());
		setMapping(TransEvClassMappingUtils.getInstance().getMapping(net.getNet(), activities, getClassifier()));
		setTryConnections(true);
	}

	public ConvertLogAlignmentToReplayResultParameters(XLog log, AcceptingPetriNet net) {
		super();
		setClassifier(XUtils.getDefaultClassifier(log));
		XLogInfo info = XLogInfoFactory.createLogInfo(log, getClassifier());
		Set<XEventClass> activities = new HashSet<XEventClass>(info.getEventClasses().getClasses());
		setMapping(TransEvClassMappingUtils.getInstance().getMapping(net.getNet(), activities, getClassifier()));
		setTryConnections(true);
	}

	public ConvertLogAlignmentToReplayResultParameters(ConvertLogAlignmentToReplayResultParameters parameters) {
		super(parameters);
		setClassifier(parameters.getClassifier());
		setMapping(parameters.getMapping());
	}

	public boolean equals(Object object) {
		if (object instanceof ConvertLogAlignmentToReplayResultParameters) {
			ConvertLogAlignmentToReplayResultParameters parameters = (ConvertLogAlignmentToReplayResultParameters) object;
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

	public void setMapping(TransEvClassMapping map) {
		this.map = map;
	}

	public TransEvClassMapping getMapping() {
		return map;
	}
}
