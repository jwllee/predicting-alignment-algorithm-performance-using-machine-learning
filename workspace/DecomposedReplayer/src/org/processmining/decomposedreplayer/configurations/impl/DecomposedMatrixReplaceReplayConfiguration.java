package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionReplaceNoReduceStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedMatrixReplaceReplayConfiguration extends DecomposedAbstractMatrixReplaceReplayConfiguration {

	public final static String NAME = "Hide using matrix";
	
	public String getName() {
		return NAME;
	}

	public String getStrategy() {
		return DecompositionReplaceNoReduceStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.setEnabled(true);
	}

}
