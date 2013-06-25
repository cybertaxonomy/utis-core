/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bgbm.biovel.drf.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author c.mathew
 */
public class BiovelUtils {
    public static final String[] delimiters = {";","|","~"};
    
    public static boolean containsWhitespace(List<String> elements){
        
        Iterator itr = elements.iterator();
        
        while(itr.hasNext()) {
            String element = (String)itr.next();
            if(element.trim().equals("")) {
                return true;
            }
        }
        return false;
    }
    
    public static String convertArrayToString(String[] fields, String delimiter) {
        StringBuilder csvStringBuilder = new StringBuilder();
        for(int i = 0; i < fields.length; i++) {
            if(i > 0) {
                csvStringBuilder.append(delimiter);
            }
            csvStringBuilder.append(fields[i]);
        }
        return csvStringBuilder.toString();
    }
    
    public static String convertArrayToString(String[] fields) {
        int delimIndex = 0;    
        StringBuilder stringBuilder = new StringBuilder();
        for(String dl : delimiters){
            for(String f : fields) {
                if(f.contains(dl)) {                    
                    stringBuilder = new StringBuilder();
                    break;
                } else {
                    stringBuilder.append(delimiters[delimIndex]).append(f);
                }
            }
        }    
        return stringBuilder.toString();
    }
    
	public static String getResourceAsString(String file, String encoding) {

		StringBuffer sb = new StringBuffer(4000);

		//InputStream data = ClassLoader.getSystemClassLoader().getResourceAsStream(file);
		InputStream data = BiovelUtils.class.getResourceAsStream(file);
		BufferedReader br;
		try {
			InputStreamReader is = new InputStreamReader(data,encoding);
			br = new BufferedReader(is);

			String str;
			String lineEnding = System.getProperty("line.separator");

			while ((str = br.readLine()) != null) {
				sb.append(str);
				sb.append(lineEnding);				
			}
			is.close();
			br.close();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return sb.toString();
	}
}
