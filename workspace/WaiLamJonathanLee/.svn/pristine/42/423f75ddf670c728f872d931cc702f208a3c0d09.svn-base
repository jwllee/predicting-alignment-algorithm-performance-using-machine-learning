package org.processmining.decomposedreplayer.experiments.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StringFileReader {

	private void saveCloseReaders(BufferedReader br, FileReader fr) {
        try {

           if (br != null)
               br.close();
           if (fr != null)
               fr.close();

       } catch (IOException ioe) {

           ioe.printStackTrace();

       }
   }

   public String readFileAsString(String filePath) {
       BufferedReader br = null;
       FileReader fr = null;

       StringBuilder stringBuilder = new StringBuilder();

       try {

           fr = new FileReader(filePath);
           br = new BufferedReader(fr);

           String currentLine;

           while ((currentLine = br.readLine()) != null) {
               if (stringBuilder.length() > 0)
                   stringBuilder.append("\n");
               stringBuilder.append(currentLine);
           }

       } catch (IOException ioe) {

           ioe.printStackTrace();

       } finally {

           saveCloseReaders(br, fr);

       }

       return stringBuilder.toString();
   }

   public List<String> readFileAsStringList(String filePath) {
       BufferedReader br = null;
       FileReader fr = null;

       List<String> stringList = new ArrayList<>();

       try {

           fr = new FileReader(filePath);
           br = new BufferedReader(fr);

           String currentLine;

           while ((currentLine = br.readLine()) != null) {
               stringList.add(currentLine);
           }

       } catch (IOException ioe) {

           ioe.printStackTrace();

       } finally {

           saveCloseReaders(br, fr);

       }

       return stringList;
   }
	
}
