package org.processmining.experiments.plugins;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetArrayPlugin;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinetdecomposer.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;
import org.processmining.acceptingpetrinetdecomposer.plugins.DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericStrategy;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionSESEStrategy;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.activityclusterarraycreator.plugins.ConvertCausalActivityGraphToActivityClusterArrayPlugin;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.causalactivitygraphcreator.plugins.ConvertCausalActivityMatrixToCausalActivityGraphPlugin;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixcreator.creators.impl.MatrixPNCreator;
import org.processmining.causalactivitymatrixcreator.parameters.CreateFromAcceptingPetriNetParameters;
import org.processmining.causalactivitymatrixcreator.plugins.CreateFromAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.experiments.parameters.TestAcceptingPetriNetDecompositionParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;

@Plugin(name = "Test decompose accepting petri net", parameterLabels = { "Parameters"  }, returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class TestAcceptingPetriNetDecompositionPlugin implements HTMLToString {

	private StringBuffer buf = new StringBuffer();
	private static DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin decomposer = new DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin();
	private static ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();
	private static ExportAcceptingPetriNetArrayPlugin netArrayExporter = new ExportAcceptingPetriNetArrayPlugin();
	
	@UITopiaVariant(affiliation = "PUC", author = "Wai Lam Jonathan Lee", email = "walee@uc.cl", pack = "WaiLamJonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestAcceptingPetriNetDecompositionParameters parameters) {
		AcceptingPetriNet apn;
		try {
			System.out.println("[TestDecomposedActivitiesPlugin] Importing " + parameters.netFilePath);
			apn = (AcceptingPetriNet) netImporter.importFile(context, parameters.netFilePath);
			
			return new TestAcceptingPetriNetDecompositionPlugin(context, apn, parameters);			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public TestAcceptingPetriNetDecompositionPlugin(final PluginContext context, 
			final AcceptingPetriNet apn, final TestAcceptingPetriNetDecompositionParameters parameters) {
	
		System.out.println("[TestDecomposedActivitiesPlugin] Getting clusters...");
		ActivityClusterArray clusters = getClusters(context, apn, parameters);
		
		
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters params = 
				new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(apn, clusters, 
						getClassifier(parameters.classifierType));
		
		// setup params
		params.setStrategy(parameters.decompositionStrategy);
		params.setIncludeClusterTransitions(parameters.includeClusterTransitions);
		
		Set<XEventClass> unsplittableActivities = new HashSet<>();
		for (String activityLabel: parameters.unsplittableActLabels) {
			XEventClass activity = new XEventClass(activityLabel, 0);
			unsplittableActivities.add(activity);
		}
		params.setUnsplittableActivities(unsplittableActivities);
		
		if (params.getStrategy().equals(DecompositionGenericStrategy.NAME)) {
			String percentageString = parameters.optionalConfigs.get("percentage");
			int percentage = Integer.parseInt(percentageString);
			params.setPercentage(percentage);
		}
		
		if (params.getStrategy().equals(DecompositionSESEStrategy.NAME)) {
			String maxSubnetArcsString = parameters.optionalConfigs.get("maxSubnetArcs");
			int maxSubnetArcs = Integer.parseInt(maxSubnetArcsString);
			params.setMaxSubnetArcs(maxSubnetArcs);
		}
		
		
		AcceptingPetriNetArray netArray = decomposer.run(context, apn, clusters, params);
		File file = new File(parameters.netArrayFilePath);
		
		try {
		
			netArrayExporter.export(context, netArray, file);
		
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		}
	}
	
	public AcceptingPetriNetArray getDecomposition(PluginContext context, AcceptingPetriNet apn,
			ActivityClusterArray clusters, TestAcceptingPetriNetDecompositionParameters parameters) {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			activities.addAll(cluster);
		}
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters params = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(
				apn, activities, null);
		params.setStrategy(parameters.decompositionStrategy);
		return decomposer.apply(context, apn, clusters, params);
	}
	
	public ActivityClusterArray getClusters(PluginContext context, AcceptingPetriNet apn,
			TestAcceptingPetriNetDecompositionParameters parameters) {
		CausalActivityMatrix matrix = getMatrix(context, apn, parameters);
		CausalActivityGraph graph = getGraph(context, matrix);
		ActivityClusterArray clusters = getClusters(context, graph);
		return clusters;
	}
	
	public ActivityClusterArray getClusters(PluginContext context, CausalActivityGraph graph) {
		ConvertCausalActivityGraphToActivityClusterArrayPlugin plugin = new ConvertCausalActivityGraphToActivityClusterArrayPlugin();
		ConvertCausalActivityGraphToActivityClusterArrayParameters params = new ConvertCausalActivityGraphToActivityClusterArrayParameters(
				graph);
		params.setCheckBackwardArc(false);
		params.setIncludeAll(true);
		return plugin.run(context, graph, params);
	}
	
	public CausalActivityGraph getGraph(PluginContext context, CausalActivityMatrix matrix) {
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin plugin = new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters params = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		params.setZeroValue(0.5);
		params.setConcurrencyRatio(0.0);
		//		params.setIncludeThreshold(0.005);
		return plugin.run(context, matrix, params);
	}
	
	public CausalActivityMatrix getMatrix(PluginContext context, AcceptingPetriNet apn, 
			TestAcceptingPetriNetDecompositionParameters parameters) {
		CreateFromAcceptingPetriNetPlugin plugin = new CreateFromAcceptingPetriNetPlugin();
		CreateFromAcceptingPetriNetParameters params = new CreateFromAcceptingPetriNetParameters(apn);
		params.setClassifier(getClassifier(parameters.classifierType));
		params.setCreator(MatrixPNCreator.NAME);
		return plugin.run(context, apn, params);
	}
	
	private XEventClassifier getClassifier(String classifierType) {
        XEventClassifier classifier = null;
        switch (classifierType) {
            case TestAcceptingPetriNetDecompositionParameters.CLASSIFIER_CONCEPTNAME:
                classifier = XLogInfoImpl.NAME_CLASSIFIER;
                break;
            case TestAcceptingPetriNetDecompositionParameters.CLASSIFIER_STANDARD:
                classifier = new XEventAndClassifier(new XEventNameClassifier(),
                        new XEventLifeTransClassifier());
                break;
            default:
                throw new IllegalArgumentException("Do not recognize classifier: " + classifierType);
        }
        return classifier;
    }
	
	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		System.out.println(buf);
		buffer.append(buf);
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		buffer.append("\n");
		return buffer.toString();
	}

}
