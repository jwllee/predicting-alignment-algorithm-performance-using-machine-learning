package org.processmining.sesedecomposition.parameters;

import java.util.HashMap;
import java.util.Map;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.models.graphbased.directed.DirectedGraphElement;

public class ConvertRPSTNodeToPetriNetParameters extends PluginParametersImpl {

	private Map<DirectedGraphElement, DirectedGraphElement> map;
	
	public ConvertRPSTNodeToPetriNetParameters() {
		super();
		setMap(new HashMap<DirectedGraphElement, DirectedGraphElement>());
	}
	
	public ConvertRPSTNodeToPetriNetParameters(ConvertRPSTNodeToPetriNetParameters parameters) {
		super(parameters);
		setMap(parameters.map);
	}

	public Map<DirectedGraphElement, DirectedGraphElement> getMap() {
		return map;
	}

	public void setMap(Map<DirectedGraphElement, DirectedGraphElement> map) {
		this.map = map;
	}
	
	public boolean equals(Object object) {
		if (object instanceof ConvertRPSTNodeToPetriNetParameters) {
			ConvertRPSTNodeToPetriNetParameters parameters = (ConvertRPSTNodeToPetriNetParameters) object;
			return getMap().equals(parameters.getMap());
		}
		return false;
	}
}
