package org.processmining.decomposedreplayer.models.stats;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IterationStats {
	
	public enum Statistic {
		ITERATION("iteration"),
		N_TRACE_ALIGNED("n_trace_aligned"),
		N_ALIGNMENT_OPEN("n_alignment_open"),
		N_ALIGNMENT_VALID("n_alignment_valid"),
		N_ALIGNMENT_REJECTED("n_alignment_rejected"),
		N_RECOMPOSE_ACTIVITY("n_recompose_activity"),
		N_SUBNET("n_subnet"),
		N_BORDER_ACTIVITY("n_border_activity"),
		N_EXCLUDED_TRACE("n_excluded_trace"),
		LOWER_BOUND_COST("lower_bound_cost");
		
		private final String label;
		
		private Statistic(String label) {
			this.label = label;
		}
		
		public String toString() {
			return label;
		}
	}
	
	public Map<Statistic,Object> map;
	
	public IterationStats(int iteration) {
		this.map = new HashMap<>();
		map.put(Statistic.ITERATION, iteration);
	}
	
	public String getHeader(List<Statistic> keys) {
		String header = "";
		boolean first = true;
		
		for (Statistic key: Statistic.values()) {
			keys.add(key);
			if (!first) {
				header += ",";
			}
			
			header += key.toString();
			first = false;
		}
		
		return header;
	}
	
	public void writeToStream(PrintStream stream, boolean writeHeader, List<Statistic> keys) {
		synchronized (stream) {
			if (writeHeader) {
				keys.clear();
				stream.println(getHeader(keys));
			}
			
			if (keys.isEmpty()) {
				return;
			}
			
			Iterator<Statistic> iterator = keys.iterator();
			stream.print(map.get(iterator.next()));
			
			while (iterator.hasNext()) {
				stream.print("," + map.get(iterator.next()));
			}
		}
	}
	
}
