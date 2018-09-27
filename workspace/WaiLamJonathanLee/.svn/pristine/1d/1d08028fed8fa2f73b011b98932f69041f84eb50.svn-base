package org.processmining.sesedecomposition.parameters;

import java.util.HashMap;
import java.util.Map;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.models.graphbased.directed.DirectedGraphElement;

public class DecomposeBySESEsWithBridgingParameters extends PluginParametersImpl {
	
	private Map<DirectedGraphElement, DirectedGraphElement> map;
	private int maxSize;
	
	public DecomposeBySESEsWithBridgingParameters() {
		super();
		setMap(new HashMap<DirectedGraphElement, DirectedGraphElement>());
		setMaxSize(25);
	}
	
	public DecomposeBySESEsWithBridgingParameters(DecomposeBySESEsWithBridgingParameters parameters) {
		super(parameters);
		setMap(parameters.map);
		setMaxSize(parameters.maxSize);
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public Map<DirectedGraphElement, DirectedGraphElement> getMap() {
		return map;
	}

	public void setMap(Map<DirectedGraphElement, DirectedGraphElement> map) {
		this.map = map;
	}

	public int hashCode() {
		final int prime = 3;
		int result = 1;
		result = prime * result + maxSize;
		result = prime * result + (map.isEmpty() ? 0 : map.hashCode());
		return result;
	}
	
	public boolean equal(Object obj) {
		if (obj instanceof DecomposeBySESEsWithBridgingParameters) {
			DecomposeBySESEsWithBridgingParameters parameters = (DecomposeBySESEsWithBridgingParameters) obj;
			return map.equals(parameters.getMap()) &&
					maxSize == parameters.getMaxSize();
		}
		return false;
	}
	
}
