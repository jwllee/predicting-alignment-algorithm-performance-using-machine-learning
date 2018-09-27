package org.processmining.decomposedreplayer.algorithms.logcreation.impl;

import org.processmining.decomposedreplayer.algorithms.logcreation.LogCreationStrategy;

public class LogCreationStrategyFactory {

	public static LogCreationStrategy createLogCreationStrategy(String strategy) {
		switch (strategy) {
			case IncludeAllLogCreationStrategy.NAME:
				return new IncludeAllLogCreationStrategy();
			
			case GroupByConflictLogCreationStrategy.NAME:
				return new GroupByConflictLogCreationStrategy();
				
			case StrictExcludeByConflictLogCreationStrategy.NAME:
				return new StrictExcludeByConflictLogCreationStrategy();
				
			default:
				throw new IllegalArgumentException("[" + LogCreationStrategyFactory.class.getSimpleName() + "] Do not recognize strategy: " + strategy);
		}
	}
	
}
