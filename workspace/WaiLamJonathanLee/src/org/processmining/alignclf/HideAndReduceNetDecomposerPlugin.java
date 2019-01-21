package org.processmining.alignclf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetArrayPlugin;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinetdecomposer.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;
import org.processmining.acceptingpetrinetdecomposer.plugins.DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericStrategy;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionReplaceReduceStrategy;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarraycreator.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;
import org.processmining.activityclusterarraycreator.plugins.ConvertCausalActivityGraphToActivityClusterArrayPlugin;
import org.processmining.activityclusterarrayextractor.parameters.ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters;
import org.processmining.activityclusterarrayextractor.plugins.ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.parameters.ConvertCausalActivityMatrixToCausalActivityGraphParameters;
import org.processmining.causalactivitygraphcreator.plugins.ConvertCausalActivityMatrixToCausalActivityGraphPlugin;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixcreator.creators.impl.MatrixPNCreator;
import org.processmining.causalactivitymatrixcreator.parameters.CreateFromAcceptingPetriNetParameters;
import org.processmining.causalactivitymatrixcreator.plugins.CreateFromAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.log.utils.XUtils;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

@Plugin(name = "Hide and Reduce Net Decomposer",
		categories = { PluginCategory.ConformanceChecking },
		parameterLabels = { "Parameters" },
		returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class HideAndReduceNetDecomposerPlugin implements HTMLToString {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Jonathan Lee", email = "walee@uc.cl", pack = "JonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, HideAndReduceNetDecomposerParameters parameters) {
		
		String netFilePath = parameters.netFilePath;
		String outFilePath = parameters.outFilePath;
		String decompositionFilePath = parameters.decompositionFilePath;
		
		AcceptingPetriNet net = importNet(context, netFilePath);
		
		if (net == null) {
			System.out.println("Cannot import net: " + netFilePath);
			System.exit(0);
		}
		
		XEventClassifier classifier = createEventNameClassifier();
		TransEvClassMapping mapping = createMappingFromNet(net.getNet(), classifier);
		List<String> decomposition = readFileAsStringList(decompositionFilePath);
		HashSet<XEventClass> unsplittables = createUnsplittables(mapping, decomposition);
		AcceptingPetriNetArray netArray = getHideAndReduceDecomposition(context, net, classifier, mapping, unsplittables);
		exportNetArray(context, netArray, outFilePath);
		
		return new HideAndReduceNetDecomposerPlugin();
	}
	
	public static void exportNetArray(PluginContext context, AcceptingPetriNetArray netArray, String outFilePath) {
		ExportAcceptingPetriNetArrayPlugin netArrayExporter = new ExportAcceptingPetriNetArrayPlugin();
		File file = new File(outFilePath);
		
		try {
			
			netArrayExporter.export(context, netArray, file);
			
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			System.out.println("Cannot export net array to: " + outFilePath);
		}
		
	}
	
	public static HashSet<XEventClass> createUnsplittables(TransEvClassMapping mapping, List<String> decomposition) {
		HashSet<XEventClass> unsplittables = new HashSet<>();
		
		for (XEventClass activity: mapping.values()) {
			if (!decomposition.contains(activity.getId()))
				unsplittables.add(activity);
		}
		
		return unsplittables;
	}
	
	public static AcceptingPetriNet importNet(PluginContext context, String netFilePath) {
		AcceptingPetriNet net = null;
		ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();
		
		try {
		
			net = (AcceptingPetriNet) netImporter.importFile(context, netFilePath);
		
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("Cannot import net: " + netFilePath);
		
		}
		
		return net;
	}
	
	public static TransEvClassMapping createMappingFromNet(Petrinet net, XEventClassifier classifier) {
		XEventClass invisibleActivity = XUtils.INVISIBLEACTIVITY;
		TransEvClassMapping mapping = new TransEvClassMapping(classifier, invisibleActivity);
		
		// create activity set from net transitions
		HashMap<String,XEventClass> activityMap = new HashMap<>();
		HashSet<String> seenLabels = new HashSet<>();
		for (Transition transition: net.getTransitions()) {
			if (transition.isInvisible())
				continue;
			if (seenLabels.contains(transition.getLabel()))
				continue;
			
			XEventClass activity = new XEventClass(transition.getLabel(), activityMap.size());
			activityMap.put(transition.getLabel(), activity);
			seenLabels.add(transition.getLabel());
		}
		
		// do the mapping
		for (Transition transition: net.getTransitions()) {
			if (transition.isInvisible())
				mapping.put(transition, invisibleActivity);
			else
				mapping.put(transition, activityMap.get(transition.getLabel()));
		}
		
		return mapping;
	}
	
	public static XEventClassifier createEventNameClassifier() {
		XEventClassifier classifier = new XEventNameClassifier();
		return classifier;
	}
	
	public static AcceptingPetriNetArray getHideAndReduceDecomposition(PluginContext context, AcceptingPetriNet net, XEventClassifier classifier, TransEvClassMapping mapping, HashSet<XEventClass> unsplittableActivities) {
		AcceptingPetriNetArray netArray = null;
		
		// getMatrix -> CausalActivityMatrix
		// discover matrix from net is default parameter value for DecomposedReplayParameter
		CreateFromAcceptingPetriNetPlugin createMatrixPlugin = new CreateFromAcceptingPetriNetPlugin();
		CreateFromAcceptingPetriNetParameters createMatrixParams = new CreateFromAcceptingPetriNetParameters(net);
		createMatrixParams.setClassifier(classifier);
		createMatrixParams.setCreator(MatrixPNCreator.NAME);
		CausalActivityMatrix causalActivityMatrix = createMatrixPlugin.run(context, net, createMatrixParams);
		
		// getGraph -> CausalActivityGraph
		ConvertCausalActivityMatrixToCausalActivityGraphPlugin convertMatrixToGraphPlugin = new ConvertCausalActivityMatrixToCausalActivityGraphPlugin();
		ConvertCausalActivityMatrixToCausalActivityGraphParameters convertMatrixToGraphParams = new ConvertCausalActivityMatrixToCausalActivityGraphParameters();
		convertMatrixToGraphParams.setZeroValue(0.5);
		convertMatrixToGraphParams.setConcurrencyRatio(0.0);
		CausalActivityGraph causalActivityGraph = convertMatrixToGraphPlugin.run(context, causalActivityMatrix, convertMatrixToGraphParams);
		
		// getClusters -> ActivityClusterArray
		ConvertCausalActivityGraphToActivityClusterArrayPlugin convertGraphToClusterPlugin = new ConvertCausalActivityGraphToActivityClusterArrayPlugin();
		ConvertCausalActivityGraphToActivityClusterArrayParameters convertGraphToClusterParams = new ConvertCausalActivityGraphToActivityClusterArrayParameters(causalActivityGraph);
		convertGraphToClusterParams.setCheckBackwardArc(false);
		convertGraphToClusterParams.setIncludeAll(true);
		ActivityClusterArray cluster = convertGraphToClusterPlugin.run(context, causalActivityGraph, convertGraphToClusterParams);

//		No need to modify clusters 
//		// getModifiedClusters -> ActivityClusterArray
//		ModifyActivityClusterArrayPlugin modifyActivityClusterPlugin = new ModifyActivityClusterArrayPlugin();
//		ModifyActivityClusterArrayParameters modifyActivityClusterParams = new ModifyActivityClusterArrayParameters(cluster);
//		modifyActivityClusterParams.setNrOfClusters(cluster.getClusters().size());
		
		// getNets -> AcceptingPetriNetArray
		DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin decomposeNetPlugin = new DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin();
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters decomposeNetParams = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(net, cluster, classifier);
		decomposeNetParams.setMapping(mapping);
		decomposeNetParams.setIncludeClusterTransitions(true);
		decomposeNetParams.setStrategy(DecompositionGenericStrategy.NAME);
		decomposeNetParams.setPercentage(100);
		decomposeNetParams.setUnsplittableActivities(new HashSet<XEventClass>(unsplittableActivities));
		netArray = decomposeNetPlugin.run(context, net, cluster, decomposeNetParams);
		
		// getClusters -> ActivityClusterArray
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin extractClusterPlugin = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin();
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters extractClusterParams = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters(netArray, new HashSet<XEventClass>(causalActivityMatrix.getActivities()));
		extractClusterParams.setMapping(mapping);
		extractClusterParams.setIOAware(true);
		cluster = extractClusterPlugin.run(context, netArray, extractClusterParams);
		
		// getNets -> AcceptingPetriNetArray
		decomposeNetPlugin = new DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin();
		decomposeNetParams = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(net, cluster, classifier);
		decomposeNetParams.setMapping(mapping);
		decomposeNetParams.setIncludeClusterTransitions(true);
		decomposeNetParams.setStrategy(DecompositionReplaceReduceStrategy.NAME);
		decomposeNetParams.setUnsplittableActivities(new HashSet<XEventClass>(unsplittableActivities));
		
		return decomposeNetPlugin.run(context, net, cluster, decomposeNetParams);
	}

	public static String readFileAsString(String filePath) {
	       BufferedReader br = null;
	       FileReader fr = null;

	       StringBuilder stringBuilder = new StringBuilder();

	       try {

	           fr = new FileReader(filePath);
	           br = new BufferedReader(fr);

	           String currentLine;

	           while ((currentLine = br.readLine()) != null) {
	               if (stringBuilder.length() > 0)
	                   stringBuilder.append("\n");
	               stringBuilder.append(currentLine);
	           }

	       } catch (IOException ioe) {

	           ioe.printStackTrace();

	       } finally {

	    	   try {

	               if (br != null)
	                   br.close();
	               if (fr != null)
	                   fr.close();

	           } catch (IOException ioe) {

	               ioe.printStackTrace();

	           }

	       }

	       return stringBuilder.toString();
	   }
	
	public static List<String> readFileAsStringList(String filePath) {
	       BufferedReader br = null;
	       FileReader fr = null;

	       List<String> stringList = new ArrayList<>();

	       try {

	           fr = new FileReader(filePath);
	           br = new BufferedReader(fr);

	           String currentLine;

	           while ((currentLine = br.readLine()) != null) {
	               stringList.add(currentLine);
	           }

	       } catch (IOException ioe) {

	           ioe.printStackTrace();

	       } finally {

	    	   try {

	               if (br != null)
	                   br.close();
	               if (fr != null)
	                   fr.close();

	           } catch (IOException ioe) {

	               ioe.printStackTrace();

	           }

	       }

	       return stringList;
	  }

	public String toHTMLString(boolean includeHTMLTags) {
		// TODO Auto-generated method stub
		return "";
	}
	
}
