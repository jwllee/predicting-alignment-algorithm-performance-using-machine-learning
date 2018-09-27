package org.processmining.tests.experiments.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.processmining.experiments.utils.StringFileWriter;

public class TestStringFileWriter {

	@Test
	public void simpleTest() {
		assertEquals(2, 1 + 1);
	}

	@Test
	public void testWriteEmptyStringList() {
		
		StringWriter stringWriter = new StringWriter();
		StringFileWriter writer = new StringFileWriter();
		
		List<String> emptyList = new ArrayList<>();
		
		try {
			
			writer.writeStringListToFile(stringWriter, emptyList);
		
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		}
		
		assertEquals("",  stringWriter.toString());
	}
	
	@Test
	public void testWriteOneStringList() {
		
		StringWriter stringWriter = new StringWriter();
		StringFileWriter writer = new StringFileWriter();
		
		List<String> list = new ArrayList<>();
		list.add("Hello world");
		
		try {
			
			writer.writeStringListToFile(stringWriter, list);
		
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		}
		
		assertEquals("Hello world",  stringWriter.toString());
		
	}
	
	@Test
	public void testWriteTwoStringList() {
		
		StringWriter stringWriter = new StringWriter();
		StringFileWriter writer = new StringFileWriter();
		
		List<String> twoStringsList = new ArrayList<>();
		twoStringsList.add("Hello world");
		twoStringsList.add("Goodbye world");
		
		try {
			
			writer.writeStringListToFile(stringWriter, twoStringsList);
		
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
			
		}
		
		assertEquals("Hello world\nGoodbye world",  stringWriter.toString());
		
	}
	
}
