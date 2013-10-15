package org.bgbm.biovel.drf.utils;

public class CSVUtils {
	
	public static String wrapWhenComma(String field) {
		if(field.contains(",")) {
			return "\"" + escapeQuotes(field) + "\""; 
		}
		return field;
	}
	
	public static String escapeQuotes(String field) {
		if(field.contains("\"")) {
			return field.replace("\"", "\"\"");
		}
		return field;
	}

}
