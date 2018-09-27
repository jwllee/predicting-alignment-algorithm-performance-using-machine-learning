package org.processmining.pnrepresultconversion.utils;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.plugins.balancedconformance.export.XAlignmentConverter;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignedLog;

/**
 * Builder for creating a XLog with alignment extension in an easy way. Basically a copy of the 
 * XLogBuilder {@link XLogBuilder}. By calling {@link XAlignedLog#newInstance()}, 
 * it can be used as follows:
 * 
 * <pre>
 * {@code
 * 	XLog alignedLog = XAlignedLog.newInstance()
 * 		.startAlignedLog("alignedLogName")
 * 		.addAlignment("alignmentName", 0.99, 2)
 * 		.addAttribute("alignmentAttribute", "nearly perfectly fitting")
 * 			.addSynchronousMove("Move1", "Activity A", "Activity A", 
 * 								"Activity A+COMPLETE", "Activity A")
 * 			.addAttribute("alignmentMoveAttribute", 1)
 * 			.addLogMove("Move2", "Activity B", "Activity B+COMPLETE", 10)
 * 			.build();
 * }
 * </pre>
 * Please note that a {@link XAlignedLogBuilder} instance is designed to be used 
 * to create one aligned log only.
 * 
 * I also looked at {@link XAlignmentConverter} for reference on how to do the conversion.
 * 
 * @author Wai Lam Jonathan Lee
 *
 */
public class XAlignedLogBuilder {

	public static XAlignedLogBuilder newInstance() {
		return new XAlignedLogBuilder();
	}
	
	private XFactory factory = XFactoryRegistry.instance().currentDefault();
	private final XConceptExtension conceptInstance = XConceptExtension.instance();
	private final XAlignmentExtension alignmentInstance = XAlignmentExtension.instance();
	
	private XLog log = null;
	
	private XTrace currentTrace = null;
	private int currentTraceMultiplicity = 1;
	
	private XEvent currentEvent = null;
	private int currentEventMultiplicity;
	
	public XAlignedLogBuilder startAlignedLog(String name) {
		log = factory.createLog();
		if (log != null) {
			conceptInstance.assignName(log, name);
		}
		return this;
	}
	
	public XAlignedLogBuilder addAlignment(String name, double fitness) {
		return addAlignment(name, fitness, 1);
	}
	
	public XAlignedLogBuilder addAlignment(String name, double fitness, int numberOfAlignments) {
		if (log == null) {
			throw new IllegalStateException("Please call 'startAlignedLog' first!");
		}
		if (currentEvent != null) {
			addCurrentAlignmentMoveToAlignment();
		}
		if (currentTrace != null) {
			addCurrentAlignmentToAlignedLog();
			currentEvent = null;
		}
		currentTrace = factory.createTrace();
		if (name != null) {
			conceptInstance.assignName(currentTrace, name);
		}
		if (fitness >= 0) {
			alignmentInstance.assignFitness(currentTrace, fitness);
		}
		currentTraceMultiplicity = numberOfAlignments;
		return this;
	}
	
	private void addCurrentAlignmentToAlignedLog() {
		log.add(currentTrace);
		if (currentTraceMultiplicity > 1) {
			for (int i = 0; i < currentTraceMultiplicity - 1; i++) {
				XTrace clone = (XTrace) currentTrace.clone();
				String name = conceptInstance.extractName(clone);
				if (name != null) {
					conceptInstance.assignName(clone, name.concat("-").concat(String.valueOf(i + 1)));
				}
				log.add(clone);
			}
		}
	}
	
	public XAlignedLogBuilder addSynchronousMove(String name, String logMove,
			String modelMove, String eventClassId, String activityId) {
		return addSynchronousMove(name, logMove, modelMove, eventClassId, activityId, 1);
	}
	
	public XAlignedLogBuilder addSynchronousMove(String name, String logMove,
			String modelMove, String eventClassId, String activityId, 
			int numberOfMoves) {
		if (currentTrace == null) {
			throw new IllegalStateException("Please call 'addAlignment' first!");
		}
		if (currentEvent != null) {
			addCurrentAlignmentMoveToAlignment();
		}
		currentEvent = factory.createEvent();
		conceptInstance.assignName(currentEvent, name);
		alignmentInstance.assignMoveType(currentEvent, MoveType.SYNCHRONOUS);
		alignmentInstance.assignLogMove(currentEvent, logMove);
		alignmentInstance.assignModelMove(currentEvent, modelMove);
		alignmentInstance.assignActivityId(currentEvent, activityId);
		alignmentInstance.assignEventClassId(currentEvent, eventClassId);
		alignmentInstance.assignIsObservable(currentEvent, true);
		currentEventMultiplicity = numberOfMoves;
		return this;
	}
	
	public XAlignedLogBuilder addLogMove(String name, String logMove,
			String eventClassId) {
		return addLogMove(name, logMove, eventClassId, 1);
	}
	
	public XAlignedLogBuilder addLogMove(String name, String logMove,
			String eventClassId, int numberOfMoves) {
		if (currentTrace == null) {
			throw new IllegalStateException("Please call 'addAlignment' first!");
		}
		if (currentEvent != null) {
			addCurrentAlignmentMoveToAlignment();
		}
		currentEvent = factory.createEvent();
		conceptInstance.assignName(currentEvent, name);
		alignmentInstance.assignMoveType(currentEvent, MoveType.LOG);
		alignmentInstance.assignLogMove(currentEvent, logMove);
		alignmentInstance.assignEventClassId(currentEvent, eventClassId);
		alignmentInstance.assignIsObservable(currentEvent, true);
		currentEventMultiplicity = numberOfMoves;
		return this;
	}
	
	public XAlignedLogBuilder addModelMove(String name, String modelMove,
			String activityId, Boolean observable) {
		return addModelMove(name, modelMove, activityId, observable, 1);
	}
	
	public XAlignedLogBuilder addModelMove(String name, String modelMove,
			String activityId, Boolean observable, int numberOfMoves) {
		if (currentTrace == null) {
			throw new IllegalStateException("Please call 'addAlignment' first!");
		}
		if (currentEvent != null) {
			addCurrentAlignmentMoveToAlignment();
		}
		currentEvent = factory.createEvent();
		conceptInstance.assignName(currentEvent, name);
		alignmentInstance.assignMoveType(currentEvent, MoveType.MODEL);
		alignmentInstance.assignModelMove(currentEvent, modelMove);
		alignmentInstance.assignActivityId(currentEvent, activityId);
		alignmentInstance.assignIsObservable(currentEvent, true);
		currentEventMultiplicity = numberOfMoves;
		return this;
	}
	
	private void addCurrentAlignmentMoveToAlignment() {
		currentTrace.add(currentEvent);
		if (currentEventMultiplicity > 1) {
			for (int i = 0; i < currentEventMultiplicity - 1; i++) {
				currentTrace.add((XEvent) currentEvent.clone());
			}
		}
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @return the {@link XAlignedLogBuilder} itself
	 */
	public XAlignedLogBuilder addAttribute(XAttribute attribute) {
		addAttributeInternal(attribute.getKey(), attribute);
		return this;
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @return the {@link XAlignedLogBuilder} itself
	 */
	public XAlignedLogBuilder addAttribute(String name, boolean value) {
		XAttribute attribute = factory.createAttributeBoolean(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @return the {@link XAlignedLogBuilder} itself
	 */
	public XAlignedLogBuilder addAttribute(String name, long value) {
		XAttribute attribute = factory.createAttributeDiscrete(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @return the {@link XAlignedLogBuilder} itself
	 */
	public XAlignedLogBuilder addAttribute(String name, String value) {
		XAttribute attribute = factory.createAttributeLiteral(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @return the {@link XAlignedLogBuilder} itself
	 */
	public XAlignedLogBuilder addAttribute(String name, Date value) {
		XAttribute attribute = factory.createAttributeTimestamp(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @return the {@link XAlignedLogBuilder} itself
	 */
	public XAlignedLogBuilder addAttribute(String name, double value) {
		XAttribute attribute = factory.createAttributeContinuous(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}
	
	public XAlignedLogBuilder setFactory(XFactory factory) {
		this.factory = factory;
		return this;
	}
	
	private void addAttributeInternal(String name, XAttribute attribute) {
		if (currentEvent == null && currentTrace == null) {
			throw new IllegalStateException("Please call 'addAlignmentMove' or 'addAlignment' first!");
		}
		
		if (currentEvent == null) {
			currentTrace.getAttributes().put(name, attribute);
		} else {
			currentEvent.getAttributes().put(name, attribute);
		}
	}
	
	/**
	 * Builds and returns the XLog with alignment extension. This is only used once!
	 * 
	 * @return the extended XLog
	 */
	public XLog build() {
		if (currentEvent != null) {
			addCurrentAlignmentMoveToAlignment();
		}
		if (currentTrace != null) {
			addCurrentAlignmentToAlignedLog();
		}
		log.getExtensions().add(alignmentInstance);
		return log;
	}
}
