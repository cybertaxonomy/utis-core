package org.cybertaxonomy.utis.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UtilFactory {
	
	public static BufferedReader getReader (String fileUrl, String encoding) throws IOException {
		InputStreamReader reader;
		try {
			if (encoding == null) {
				reader = new FileReader(fileUrl);
			} else {
				reader = new InputStreamReader(new FileInputStream(fileUrl),encoding); 
			}
		}
		catch (FileNotFoundException e) {
			// try a real URL instead
			URL url = new URL(fileUrl);
			if (encoding == null) {
				reader = new InputStreamReader (url.openStream());
			} else {
				reader = new InputStreamReader (url.openStream(), encoding);
			}
		}
		return new BufferedReader(reader);
	}

}
