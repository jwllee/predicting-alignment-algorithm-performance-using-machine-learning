package org.processmining.logalignment.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.processmining.log.utils.XUtils;
import org.processmining.logalignment.parameters.ReplayEventLogArrayOnAcceptingPetriNetArrayParameters;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.pnetreplayer.dialogs.TransEvClassMappingPanel;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class ReplayEventLogArrayOnAcceptingPetriNetArrayDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5394268012165398553L;

	public JPanel getPanel(EventLogArray logs, AcceptingPetriNetArray nets,
			final ReplayEventLogArrayOnAcceptingPetriNetArrayParameters parameters, int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
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
			
			final NiceSlider moveOnLogSlider = SlickerFactory.instance().createNiceIntegerSlider("Move on Log Costs", 1, 100,
					parameters.getMoveOnLogCosts(), Orientation.HORIZONTAL);
			moveOnLogSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMoveOnLogCosts(moveOnLogSlider.getSlider().getValue());
				}
			});
			add(moveOnLogSlider, "0, 1");

			final NiceSlider moveOnModelSlider = SlickerFactory.instance().createNiceIntegerSlider("Move on Model Costs", 1, 100,
					parameters.getMoveOnModelCosts(), Orientation.HORIZONTAL);
			moveOnModelSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMoveOnModelCosts(moveOnModelSlider.getSlider().getValue());
				}
			});
			add(moveOnModelSlider, "0, 2");


		} else if (n == 1) {
			XEventClass invisibleActivity = TransEvClassMappingUtils.getInstance().getInvisibleActivity();
			parameters.setMapping(new TransEvClassMapping(parameters.getClassifier(), invisibleActivity));
			Set<String> labels = new HashSet<String>();
			Set<XEventClass> activities = new HashSet<XEventClass>();

			for (int index = 0; index < logs.getSize(); index++) {
				Set<XEventClass> subActivities = new HashSet<XEventClass>();
				XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), parameters.getClassifier());
				subActivities.addAll(info.getEventClasses().getClasses());
				activities.addAll(info.getEventClasses().getClasses());
				TransEvClassMapping subMap = TransEvClassMappingUtils.getInstance().getMapping(
						nets.getNet(index).getNet(), subActivities, parameters.getClassifier());
				for (Transition transition : subMap.keySet()) {
					if (!transition.isInvisible() || !labels.contains(transition.getLabel())) {
						parameters.getMapping().put(transition, subMap.get(transition));
						labels.add(transition.getLabel());
					}
				}
			}
			activities.add(XUtils.MOVEONMODELACTIVITY);
			TransEvClassMappingPanel mapPanel = new TransEvClassMappingPanel(activities, parameters);
			add(mapPanel, "0, 0");
		}
		return this;
	}
}
