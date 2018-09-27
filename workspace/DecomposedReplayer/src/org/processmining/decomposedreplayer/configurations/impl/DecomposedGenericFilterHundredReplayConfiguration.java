package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericHundredStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedGenericFilterHundredReplayConfiguration extends DecomposedAbstractGenericFilterReplayConfiguration {

	public final static String NAME = "Decompose 100%";

	public String getName() {
		return NAME;
	}
	
	public String getStrategy() {
		return DecompositionGenericHundredStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.getSlider().setValue(100);
		slider.setEnabled(false);
	}
}
