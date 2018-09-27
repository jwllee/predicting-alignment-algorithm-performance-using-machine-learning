package org.processmining.decomposedreplayer.experiments.utils;

import java.io.File;
import java.util.Collection;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;

public class LogImporter {

	public static XLog importFile(String filename) throws Exception {
		XParser parser;
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		if (filename.toLowerCase().endsWith(".xes") || filename.toLowerCase().endsWith(".xez")
				|| filename.toLowerCase().endsWith(".xes.gz")) {
			parser = new XesXmlParser(factory);
		} else {
			parser = new XMxmlParser(factory);
		}
		Collection<XLog> logs = null;
		Exception firstException = null;
		String errorMessage = "";
		try {
			logs = parser.parse(new File(filename));
		} catch (Exception e) {
			logs = null;
			firstException = e;
			errorMessage = errorMessage + e;
		}
		if (logs == null || logs.isEmpty()) {
			// try any other parser
			for (XParser p : XParserRegistry.instance().getAvailable()) {
				if (p == parser) {
					continue;
				}
				try {
					logs = p.parse(new File(filename));
					if (logs.size() > 0) {
						break;
					}
				} catch (Exception e1) {
					// ignore and move on.
					logs = null;
					errorMessage = errorMessage + " [" + p.name() + ":" + e1 + "]";
				}
			}
		}

		// log sanity checks;
		// notify user if the log is awkward / does miss crucial information
		if (logs == null) {
//			context.getFutureResult(0).cancel(false);
			throw new Exception("Could not open log file, possible cause: "/* + errorMessage,*/ + firstException);
		}
		if (logs.size() == 0) {
//			context.getFutureResult(0).cancel(false);
			throw new Exception("No processes contained in log!");
		}

		XLog log = logs.iterator().next();
		if (XConceptExtension.instance().extractName(log) == null) {
			/*
			 * Log name not set. Create a default log name.
			 */
			XConceptExtension.instance().assignName(log, "Anonymous log imported from " + filename);
		}

//		if (log.isEmpty()) {
//			throw new Exception("No process instances contained in log!");
//		}

		/*
		 * Set the log name as the name of the provided object.
		 */
		
		return log;
	}
	
}
