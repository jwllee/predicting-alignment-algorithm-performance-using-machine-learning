package org.processmining.pnrepresultconversion.parameters;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class PNRepResultConversionParameters extends PluginParametersImpl {

	private XEventClassifier eventClassifier;

	public XEventClassifier getEventClassifier() {
		return eventClassifier;
	}

	public void setEventClassifier(XEventClassifier eventClassifier) {
		this.eventClassifier = eventClassifier;
	}
	
}
