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
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.pnetreplayer.dialogs.TransEvClassMappingPanel;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

public class ConvertReplayResultToLogAlignmentDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7981926888579119733L;

	public JPanel getPanel(EventLogArray logs, AcceptingPetriNetArray nets, ConvertReplayResultToLogAlignmentParameters parameters, int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (n == 0) {
			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
			for (int i = 0; i < logs.getSize(); i++) {
				if (i == 0) {
					classifiers.addAll(logs.getLog(i).getClassifiers());
				} else {
					classifiers.retainAll(logs.getLog(i).getClassifiers());
				}
			}
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, parameters);
			add(classifierPanel, "0, 0");
		} else if (n == 1) {
			XEventClass invisibleActivity = TransEvClassMappingUtils.getInstance().getInvisibleActivity();
			Map<String, XEventClass> activities = new HashMap<String, XEventClass>();
			for (int i = 0; i < logs.getSize(); i++) {
				XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(i), parameters.getClassifier());
				for (XEventClass activity : info.getEventClasses().getClasses()) {
					activities.put(activity.getId(), activity);
				}
			}
			TransEvClassMapping mapping = new TransEvClassMapping(null, invisibleActivity);
			for (int i = 0; i < nets.getSize(); i++) {
				for (Transition transition : nets.getNet(i).getNet().getTransitions()) {
					mapping.put(transition, invisibleActivity);
					if (!transition.isInvisible() && activities.containsKey(transition.getLabel())) {
						mapping.put(transition, activities.get(transition.getLabel()));
					}
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
