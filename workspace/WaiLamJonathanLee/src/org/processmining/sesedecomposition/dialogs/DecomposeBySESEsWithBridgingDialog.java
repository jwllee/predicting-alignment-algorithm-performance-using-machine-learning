package org.processmining.sesedecomposition.dialogs;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.sesedecomposition.parameters.DecomposeBySESEsWithBridgingParameters;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class DecomposeBySESEsWithBridgingDialog extends JPanel {
	
	public JPanel getPanel(AcceptingPetriNet net, 
			final DecomposeBySESEsWithBridgingParameters parameters, int n) {
		removeAll();
		
		if (n == 0) {
			double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
					{ TableLayoutConstants.FILL, 30, 30, 30 }
			};
			
			setLayout(new TableLayout(size));
			
			int netSize = net.getNet().getEdges().size();
			final NiceSlider maxSubnetArcSizeSlider = SlickerFactory.instance().createNiceIntegerSlider(
					"Maximum number of arcs per subnet", 1, netSize, parameters.getMaxSize(), Orientation.HORIZONTAL);
			maxSubnetArcSizeSlider.addChangeListener(new ChangeListener() {
				
				public void stateChanged(ChangeEvent e) {
					parameters.setMaxSize(maxSubnetArcSizeSlider.getSlider().getValue());
					
				}
			});
			add(maxSubnetArcSizeSlider, "0, 0");
		}
		
		return this;
	}
}
