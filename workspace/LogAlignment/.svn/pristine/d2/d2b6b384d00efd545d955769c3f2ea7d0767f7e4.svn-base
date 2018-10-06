package org.processmining.logalignment.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public interface ReplayResultArray {

	/**
	 * Initializes the array.
	 */
	void init();

	/**
	 * Adds the given replay to the array.
	 * 
	 * @param replay
	 *            The given replay.
	 * @return The index of the added replay.
	 */
	int addReplay(PNRepResult replay);

	/**
	 * Removes the first occurrence of the given replay from the array.
	 * 
	 * @param replay
	 *            The given replay.
	 * @return The index of the removed replay, if present. -1 if not present.
	 */
	int removeReplay(PNRepResult replay);

	/**
	 * Adds the given replay at the given index in the array.
	 * 
	 * @param index
	 *            The given index.
	 * @param replay
	 *            The given replay.
	 */
	void addReplay(int index, PNRepResult replay);

	/**
	 * Removes the replay from the given index from the array, if valid.
	 * 
	 * @param index
	 *            The given index.
	 */
	void removeReplay(int index);

	/**
	 * Returns the replay at the given index
	 * 
	 * @param index
	 *            The given index.
	 * @return The replay at the given index, if valid. null if not valid.
	 */
	PNRepResult getReplay(int index);
	
	int getSize();

	public void importFromStream(PluginContext context, InputStream input, String parent) throws Exception;
	public void exportToFile(PluginContext context, File file) throws IOException;
}
