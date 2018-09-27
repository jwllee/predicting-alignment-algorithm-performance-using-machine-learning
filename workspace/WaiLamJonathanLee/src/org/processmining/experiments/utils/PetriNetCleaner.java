package org.processmining.experiments.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.experiments.configurations.CleanPetriNetConfigurations;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class PetriNetCleaner {

	private CleanPetriNetConfigurations configurations;
    private ImportAcceptingPetriNetPlugin netImporter;
    private ExportAcceptingPetriNetPlugin netExporter;
    private AcceptingPetriNet apn;

    public PetriNetCleaner(CleanPetriNetConfigurations configurations, ImportAcceptingPetriNetPlugin netImporter,
                       ExportAcceptingPetriNetPlugin netExporter) {
        this.configurations = configurations;
        this.netImporter = netImporter;
        this.netExporter = netExporter;
    }

    public AcceptingPetriNet cleanPNet(CLIPluginContext context) throws ValueException {
        try {
            if (!hasCleanPNet()) {
                importPNet(context);
                apn = setFinalMarking(this.apn);
//                exportAPNet(context);
                return apn;
            }
        } catch (IOException ioe) {
            throw new ValueException("[PNetCleaner] Cannot clean apn");
        }
        return null;
    }
    
    private boolean hasCleanPNet() {
        File f = new File(this.configurations.netCleanPath);
        boolean hasClean = f.exists() && f.isFile();
        System.out.println("[PNetCleaner] Checking if " + this.configurations.netCleanPath + " exists...");
        System.out.println("[PNetCleaner] Has clean PNet: " + hasClean);
        return hasClean;
    }

    private void importPNet(CLIPluginContext context) throws IOException {
        String inputNetPath = this.configurations.netDirtyPath;
        try {
            apn = (AcceptingPetriNet) netImporter.importFile(context, inputNetPath);
        } catch (Exception e) {
            throw new IOException("[PNetCleaner] Cannot import apn from " + inputNetPath);
        }
    }

    private AcceptingPetriNet setFinalMarking(AcceptingPetriNet apn) {
        Petrinet net = apn.getNet();
        Set<Marking> finalMarkings = apn.getFinalMarkings();
        if (!finalMarkings.isEmpty()) {
            finalMarkings = new HashSet<>();
            for (Place place: net.getPlaces()) {
                if (net.getOutEdges(place).isEmpty()){
                    Marking finalMarking = new Marking();
                    finalMarking.add(place);
                    finalMarkings.add(finalMarking);
                }
            }
            if (finalMarkings.isEmpty())
                finalMarkings.add(new Marking());
            apn.setFinalMarkings(finalMarkings);
        }
        return apn;
    }

    private void exportAPNet(CLIPluginContext context) throws IOException {
        String outputNetPath = this.configurations.netCleanPath;
        File outputNetFile = new File(outputNetPath);
        netExporter.export(context, apn, outputNetFile);
    }
	
}
