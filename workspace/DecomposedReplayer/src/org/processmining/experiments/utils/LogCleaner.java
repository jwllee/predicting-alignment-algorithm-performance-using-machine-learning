package org.processmining.experiments.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XMxmlGZIPSerializer;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.experiments.configurations.CleanLogConfigurations;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

public class LogCleaner {

	protected CleanLogConfigurations configurations;
    protected XLog xLog;

    public LogCleaner(CleanLogConfigurations configurations) {
        this.configurations = configurations;
    }

    private XEventClassifier getClassifier(String classifierType) {
        XEventClassifier classifier = null;
        switch (classifierType) {
            case CleanLogConfigurations.CLASSIFIER_CONCEPTNAME:
                classifier = XLogInfoImpl.NAME_CLASSIFIER;
                break;
            case CleanLogConfigurations.CLASSIFIER_STANDARD:
                classifier = new XEventAndClassifier(new XEventNameClassifier(),
                        new XEventLifeTransClassifier());
                break;
            default:
                throw new IllegalArgumentException("Do not recognize classifier: " + classifierType);
        }
        return classifier;
    }

    public void cleanLog() throws ValueException {
        try {
            this.importLog();
            // check if clean log already exists
            if (!hasCleanLog()) {
                this.xLog = this.addClassifier(this.xLog);
                this.exportCleanLog();
            }
        } catch (IOException ioe) {
            throw new ValueException("[LogCleaner] Error with configurations.");
        }
    }
    
    private boolean hasCleanLog() {
        File f = new File(this.configurations.logCleanPath);
        boolean hasClean = f.exists() && f.isFile();
        System.out.println("[LogCleaner] Checking if " + this.configurations.logCleanPath + " exists...");
        System.out.println("[LogCleaner] Has clean log: " + hasClean);
        return hasClean;
    }

    private void importLog() throws IOException {
        try {
            this.xLog = LogImporter.importFile(this.configurations.logDirtyPath);
        } catch (Exception e) {
            throw new IOException("[LogCleaner] Import exception with " + this.configurations.logDirtyPath);
        }
    }

    private XLog addClassifier(XLog xLog) {
        XEventClassifier classifier = getClassifier(this.configurations.classifierType);
        xLog.getClassifiers().clear();
        xLog.getClassifiers().add(classifier);
        return xLog;
    }

    private void exportCleanLog() throws IOException {
        String outFilePath = this.configurations.logCleanPath;
        XSerializer serializer = this.chooseSerializer();
        try {
            serializer.serialize(this.xLog, new FileOutputStream(outFilePath));
        } catch (IOException ioe) {
            throw new IOException("[LogCleaner] Cannot export xLog to " + outFilePath);
        }
    }

    private XSerializer chooseSerializer() {
        String outFilePath = this.configurations.logCleanPath;
        
        if (outFilePath.endsWith(".xes")) {
        	return new XesXmlSerializer();
        } else if (outFilePath.endsWith(".xes.gz")) {
        	return new XesXmlGZIPSerializer();
        } else if (outFilePath.endsWith(".mxml")) {
        	return new XMxmlSerializer();
        } else {
        	return new XMxmlGZIPSerializer();
        }
    }
	
}
