package org.processmining.pnrepresultconversion.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.pnrepresultconversion.parameters.PNRepResultConversionParameters;

public class PNRepResultConversionConnection extends AbstractConnection {
	
	public final static String PNREPRESULT = "Alignment Results";
	
	public final static String LOG = "Log";
	
	public final static String ALIGNEDLOG = "Aligned Log";
	
	private PNRepResultConversionParameters parameters;
	
	public PNRepResultConversionConnection(PNRepResult alignmentResult,
			XLog log, XLog alignedLog, 
			PNRepResultConversionParameters parameters) {
		super("Convert PNRepResult to Log with Alignment Extension Connection");
		put(PNREPRESULT, alignmentResult);
		put(LOG, log);
		put(ALIGNEDLOG, alignedLog);
		this.parameters = parameters;
	}

	public PNRepResultConversionParameters getParameters() {
		return parameters;
	}
	
}
