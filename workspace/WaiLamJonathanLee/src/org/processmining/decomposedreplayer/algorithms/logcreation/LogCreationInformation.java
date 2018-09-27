package org.processmining.decomposedreplayer.algorithms.logcreation;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;

public interface LogCreationInformation {

	Set<XEventClass> getUnsplittables();
	
	Set<XEventClass> getRecomposeActivities();
	
	Set<XEventClass> getBorderActivities();
	
}
