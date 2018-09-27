package org.processmining.decomposedreplayer.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfigurationManager;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.log.parameters.UpdateParameter;
import org.processmining.log.utils.XUtils;
import org.processmining.pnetreplayer.dialogs.TransEvClassMappingPanel;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class DecomposedReplayDialog extends JPanel implements UpdateParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8820478411821736060L;

	private ProMList<String> list = null;
	private NiceSlider percentageSlider = null;

	public JPanel getPanel(XLog log, AcceptingPetriNet net, final DecomposedReplayParameters parameters, int n) {
		removeAll();

		if (n == 0) {
			double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
					{ TableLayoutConstants.FILL, 30, 30, 30 } };
			setLayout(new TableLayout(size));

			DefaultListModel<String> listModel = new DefaultListModel<String>();
			for (DecomposedReplayConfiguration configuration : DecomposedReplayConfigurationManager.getInstance()
					.getConfigurations()) {
				listModel.addElement(configuration.getName());
			}
			list = new ProMList<String>("Select configuration", listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			final String defaultAlgorithm = parameters.getConfiguration();
			list.setSelection(defaultAlgorithm);
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					List<String> selected = list.getSelectedValuesList();
					if (selected.size() == 1) {
						parameters.setConfiguration(selected.get(0));
					} else {
						/*
						 * Nothing selected. Revert to selection of default
						 * classifier.
						 */
						list.setSelection(defaultAlgorithm);
						parameters.setConfiguration(defaultAlgorithm);
					}
					update();
				}
			});
			list.setPreferredSize(new Dimension(100, 100));
			add(list, "0, 0");

			//			final JCheckBox checkBoxJoin = SlickerFactory.instance().createCheckBox("Join all clusters", false);
			//			checkBoxJoin.setSelected(parameters.isCombineClusters());
			//			checkBoxJoin.addActionListener(new ActionListener() {
			//
			//				public void actionPerformed(ActionEvent e) {
			//					parameters.setCombineClusters(checkBoxJoin.isSelected());
			//				}
			//
			//			});
			//			checkBoxJoin.setOpaque(false);
			//			checkBoxJoin.setPreferredSize(new Dimension(100, 30));
			//			add(checkBoxJoin, "0, 1");

			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
			classifiers.addAll(log.getClassifiers());
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, parameters);
			add(classifierPanel, "1, 0");

			//			final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Use net to discover Matrix", true);
			//			checkBox.setSelected(parameters.isDiscoverMatrixFromNet());
			//			checkBox.addActionListener(new ActionListener() {
			//
			//				public void actionPerformed(ActionEvent e) {
			//					parameters.setDiscoverMatrixFromNet(checkBox.isSelected());
			//				}
			//
			//			});
			//			checkBox.setOpaque(false);
			//			checkBox.setPreferredSize(new Dimension(100, 30));
			//			add(checkBox, "1, 1");
			//			
			final NiceSlider moveOnLogSlider = SlickerFactory.instance().createNiceIntegerSlider("Move on Log Costs", 1,
					100, parameters.getMoveOnLogCosts(), Orientation.HORIZONTAL);
			moveOnLogSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMoveOnLogCosts(moveOnLogSlider.getSlider().getValue());
				}
			});
			add(moveOnLogSlider, "0, 1, 1, 1");

			final NiceSlider moveOnModelSlider = SlickerFactory.instance().createNiceIntegerSlider(
					"Move on Model Costs", 1, 100, parameters.getMoveOnModelCosts(), Orientation.HORIZONTAL);
			moveOnModelSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMoveOnModelCosts(moveOnModelSlider.getSlider().getValue());
				}
			});
			add(moveOnModelSlider, "0, 2, 1, 2");

			percentageSlider = SlickerFactory.instance().createNiceIntegerSlider("Decomposition percentage", 0, 100,
					parameters.getPercentage(), Orientation.HORIZONTAL);
			percentageSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setPercentage(percentageSlider.getSlider().getValue());
				}
			});
			add(percentageSlider, "0, 3, 1, 3");

			update();

		} else if (n == 1) {
			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));
			Set<XEventClass> activities = new HashSet<XEventClass>();
			XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
			activities.addAll(info.getEventClasses().getClasses());
			activities.add(XUtils.MOVEONMODELACTIVITY);
			parameters.setMapping(TransEvClassMappingUtils.getInstance().getMapping(net.getNet(), activities, parameters.getClassifier()));
			TransEvClassMappingPanel mapPanel = new TransEvClassMappingPanel(new HashSet<XEventClass>(activities),
					parameters);
			add(mapPanel, "0, 0");
		}
		return this;
	}

	public void update() {
		if (percentageSlider != null && list != null && !list.getSelectedValuesList().isEmpty()) {
			DecomposedReplayConfigurationManager.getInstance().getConfiguration(list.getSelectedValuesList().get(0))
					.update(percentageSlider);
		}
	}

	//	public CostBasedCompleteParam createReplayParameters(Collection<XEventClass> activities,
	//			XEventClass invisibleActivity, AcceptingPetriNet net) {
	//		CostBasedCompleteParam parameters = new CostBasedCompleteParam(activities, invisibleActivity, net.getNet().getTransitions());
	//		parameters.setInitialMarking(net.getInitialMarking());
	//		Set<Marking> finalMarkings = net.getFinalMarkings();
	//		if (finalMarkings.isEmpty()) {
	//			finalMarkings = new HashSet<Marking>();
	//			finalMarkings.add(new Marking());
	//		}
	//		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[0]));
	//		return parameters;
	//	}

	//	public void finish() {
	//		for (Transition transition : net.getNet().getTransitions()) {
	//			parameters.getMapping().put(transition, mapping.get(transition));
	//		}
	//	}
}
