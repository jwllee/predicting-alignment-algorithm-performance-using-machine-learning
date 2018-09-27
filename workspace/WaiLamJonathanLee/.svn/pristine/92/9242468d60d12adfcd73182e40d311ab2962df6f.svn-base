package org.processmining.decomposedreplayer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.processmining.decomposedreplayer.models.utils.Weighted;

/**
 * Adapted from https://stackoverflow.com/questions/23971365/weighted-randomized-ordering
 * 
 * @author jonathan
 *
 */
public class WeightedShuffle {

	public static void weightedShuffle(List<Weighted> values, int seed) {
		weightedShuffle(values, seed, false);
	}
	
	public static void weightedShuffle(List<Weighted> values, int seed, boolean inverse) {
		// calculate the total weight
		int total = 0;
		for (Weighted v: values) {
			total += v.getWeight();
		}
		
		// start with all of them
		List<Weighted> remaining = new ArrayList<>(values);
		Random generator = new Random(seed);

		values.clear();
		
		do {
			// pick a random point
			int random = (int) (generator.nextDouble() * total);
			// pick one from the list
			Weighted picked = null;
			int pos = 0;
			for (Weighted v: remaining) {
				if (pos + v.getWeight() > random) {
					picked = v;
					break;
				}
				pos += v.getWeight();
			} 
			remaining.remove(picked);
			total -= picked.getWeight();
			values.add(picked);
		} while (!remaining.isEmpty());
		
		if (inverse)
			Collections.reverse(values);
		
	}
	
}
