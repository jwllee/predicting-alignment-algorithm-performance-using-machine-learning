package org.processmining.experiments.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XMxmlGZIPSerializer;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlGZIPSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

public class SetLogClassifier {

	public static enum ClassifierType {
		CONCEPT_NAME("concept-name"),
		STANDARD("standard");
		
		private final String label;
		
		private ClassifierType(String label) {
			this.label = label;
		}
		
		public String toString() {
			return label;
		}
	}
	
	public static void main(String[] args) {
		String logFpath = args[0];
		String clfType = args[1];
		String logOutpath = args[2];
		
		XLog log = null;
		XUniversalParser parser = new XUniversalParser();
		
		try {
			log = parser.parse(new File(logFpath)).iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(String.format("[%s] Cannot import log file: %s", SetLogClassifier.class.getSimpleName(), logFpath));
		}
		
		// remove all existing classifiers
		log.getClassifiers().clear();
		
		switch (clfType) {
			case "concept-name":
				log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
				break;
			case "standard":
				log.getClassifiers().add(new XEventAndClassifier(new XEventNameClassifier(), 
						new XEventLifeTransClassifier()));
				break;
			default:
				throw new IllegalArgumentException("Do not recognize classifier: " + args[1]);
		}
		
		XSerializer serializer = null;
		
		if (logOutpath.endsWith(".xes")) {
			serializer = new XesXmlSerializer();
		} else if (logOutpath.endsWith(".xes.gz")) {
			serializer = new XesXmlGZIPSerializer();
		} else if (logOutpath.endsWith(".mxml")) {
			serializer = new XMxmlSerializer();
		} else if (logOutpath.endsWith(".mxml.gz")) {
			serializer = new XMxmlGZIPSerializer();
		} else {
			throw new IllegalArgumentException(String.format("[%s] Cannot identify serializer type for: %s", SetLogClassifier.class.getSimpleName(), logOutpath));
		}
		
		try {
			serializer.serialize(log, new FileOutputStream(logOutpath));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(String.format("[%s] Cannot export log file: %s", SetLogClassifier.class.getSimpleName(), logOutpath));
		}
	}
}
