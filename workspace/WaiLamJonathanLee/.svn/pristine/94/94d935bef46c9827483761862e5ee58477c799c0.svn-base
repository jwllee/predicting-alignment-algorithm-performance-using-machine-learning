package org.processmining.pnrepresultconversion.plugins;

import java.util.Collection;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.pnrepresultconversion.algorithms.PNRepResultConversionAlgorithm;
import org.processmining.pnrepresultconversion.connections.PNRepResultConversionConnection;
import org.processmining.pnrepresultconversion.help.PNRepResultConversionHelp;
import org.processmining.pnrepresultconversion.parameters.PNRepResultConversionParameters;

@Plugin(name = "Convert PNRepResult to Log with Alignment Extension", parameterLabels = { "PNRepResult", "XLog", "PNRepResultConversionParameters" }, 
returnLabels = { "XLog" }, returnTypes = { XLog.class }, help = PNRepResultConversionHelp.TEXT)
public class PNRepResultConversionPlugin extends PNRepResultConversionAlgorithm {
	
	/**
	 * 
	 * 
	 * @param context The context to run in.
	 * @param PNRepResult
	 * @param XLog 
	 * @return XLog
	 */
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl")
	@PluginVariant(variantLabel = "Convert PNRepResult to Log with Alignment Extension", requiredParameterLabels = { 0, 1 })
	public XLog runDefault(PluginContext context, PNRepResult alignmentResult, XLog log) {
		// use concept:name as the default event classifier
		XEventClassifier eventClassifier = new XEventNameClassifier();
		if (!log.getClassifiers().isEmpty()) {
			// use the first one if any
			eventClassifier = log.getClassifiers().get(0);
		}
		PNRepResultConversionParameters parameters = new PNRepResultConversionParameters();
		parameters.setEventClassifier(eventClassifier);
		// Apply the algorithm depending on whether a connection already exists.
	    return runConnections(context, alignmentResult, log, parameters);
	}
	
	/**
	 * Apply the algorithm depending on whether a connection already exists.
	 * 
	 * @param context The context to run in.
	 * @param PNRepResult
	 * @param XLog
	 * @return XLog
	 */
	private XLog runConnections(PluginContext context, PNRepResult alignmentResult, 
			XLog log, PNRepResultConversionParameters parameters) {
		if (parameters.isTryConnections()) {
			// Try to found a connection that matches the inputs and the parameters.
			Collection<PNRepResultConversionConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						PNRepResultConversionConnection.class, context, alignmentResult, log);
				for (PNRepResultConversionConnection connection : connections) {
					if (connection.getObjectWithRole(PNRepResultConversionConnection.PNREPRESULT)
							.equals(alignmentResult) && connection.getObjectWithRole(PNRepResultAllRequiredParamConnection.LOG)
							.equals(log) && connection.getParameters().equals(parameters)) {
						// Found a match. Return the associated output as result of the algorithm.
						return connection
								.getObjectWithRole(PNRepResultConversionConnection.ALIGNEDLOG);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		// No connection found. Apply the algorithm to compute a fresh output result.
		XLog alignedLog = apply(context, alignmentResult, log, parameters);
		if (parameters.isTryConnections()) {
			// Store a connection containing the inputs, output, and parameters.
			context.getConnectionManager().addConnection(
					new PNRepResultConversionConnection(alignmentResult, log, alignedLog, parameters));
		}
		// Return the output.
		return alignedLog;
	}
}
