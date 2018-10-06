package org.processmining.logalignment.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logalignment.parameters.ConvertLogAlignmentToReplayResultParameters;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.pnetreplayer.dialogs.TransEvClassMappingPanel;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

public class ConvertLogAlignmentToReplayResultDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1353464354397305031L;

	public JPanel getPanel(XLog log, AcceptingPetriNet net, ConvertLogAlignmentToReplayResultParameters parameters,
			int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (n == 0) {
			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
			classifiers.addAll(log.getClassifiers());
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, parameters);
			add(classifierPanel, "0, 0");
		} else if (n == 1) {
			Map<String, XEventClass> activities = new HashMap<String, XEventClass>();
			XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
			for (XEventClass activity : info.getEventClasses().getClasses()) {
				activities.put(activity.getId(), activity);
			}
			XEventClass invisibleActivity = TransEvClassMappingUtils.getInstance().getInvisibleActivity();
			TransEvClassMapping mapping = new TransEvClassMapping(parameters.getClassifier(), invisibleActivity);
			for (Transition transition : net.getNet().getTransitions()) {
				mapping.put(transition, invisibleActivity);
				if (!transition.isInvisible() && activities.containsKey(transition.getLabel())) {
					mapping.put(transition, activities.get(transition.getLabel()));
				}
			}
			parameters.setMapping(mapping);
			TransEvClassMappingPanel mapPanel = new TransEvClassMappingPanel(new HashSet<XEventClass>(
					activities.values()), parameters);
			add(mapPanel, "0, 0");
		}
		return this;
	}
}