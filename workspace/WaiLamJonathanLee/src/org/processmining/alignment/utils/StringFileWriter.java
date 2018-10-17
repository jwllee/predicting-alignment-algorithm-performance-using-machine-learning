package org.processmining.alignment.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class StringFileWriter {
	
	private void saveCloseWriters(BufferedWriter bw, FileWriter fw) {
		try {
			
			if (bw != null) 
				bw.close();
			
			if (fw != null)
				fw.close();
			
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		}
	}
	
	public boolean writeStringToFile(String string, String filePath) {
		
		List<String> list = new ArrayList<>();
		list.add(string);
		
		return writeStringListToFile(list, filePath);
		
	}
	
	public boolean writeStringListToFile(List<String> list, String filePath, boolean append) {

		boolean done = false;
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try {
			
			fw = new FileWriter(filePath, append);
			bw = new BufferedWriter(fw);
			
			done = writeStringListToFile(bw, list);
			
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		} finally {
			
			saveCloseWriters(bw, fw);
			
		}
		
		return done;
	}
	
	public boolean writeStringListToFile(List<String> list, String filePath) {
		
		boolean done = false;
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try {
			
			fw = new FileWriter(filePath);
			bw = new BufferedWriter(fw);
			
			done = writeStringListToFile(bw, list);
			
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		} finally {
			
			saveCloseWriters(bw, fw);
			
		}
		
		return done;
	}
	
	public boolean writeStringListToFile(Writer writer, List<String> list) throws IOException {
		
		if (list.size() > 0) {
			String firstLine = list.remove(0);
			writer.write(firstLine);
		}
		
		for (String line: list) {
			writer.write("\n");
			writer.write(line);
		}
		
		return true;
		
	}
	
}
