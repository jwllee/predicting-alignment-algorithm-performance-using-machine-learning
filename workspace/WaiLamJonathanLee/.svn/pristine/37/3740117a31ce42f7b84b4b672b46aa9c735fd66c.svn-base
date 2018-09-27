package org.processmining.sesedecomposition.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public class GenerateRPSTFromPetriNetParameters extends PluginParametersImpl {
	
	private String name;
	
	public GenerateRPSTFromPetriNetParameters() {
		super();
		setName("");
	}
	
	public GenerateRPSTFromPetriNetParameters(GenerateRPSTFromPetriNetParameters parameters) {
		super(parameters);
		setName(parameters.name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object object) {
		if (object instanceof GenerateRPSTFromPetriNetParameters) {
			GenerateRPSTFromPetriNetParameters parameters = (GenerateRPSTFromPetriNetParameters) object;
			return getName().equals(parameters.getName());
		}
		return false;
	}
}
