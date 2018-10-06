package org.processmining.logalignment.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logalignment.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;

public class MergeLogAlignmentArrayIntoLogAlignmentDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8697712384734043122L;

	public JPanel getPanel(XLog log, MergeLogAlignmentArrayIntoLogAlignmentParameters parameters, int n) {
		removeAll();
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		if (n == 0) {
			List<XEventClassifier> classifiers = log.getClassifiers();
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, parameters);
			add(classifierPanel, "0, 0");
		}
		return this;
	}

	public void finish() {
	}
}
