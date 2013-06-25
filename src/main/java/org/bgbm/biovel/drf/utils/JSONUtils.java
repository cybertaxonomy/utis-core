package org.bgbm.biovel.drf.utils;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;


import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {

	public static JSONObject parseJsonToObject(String json) throws DRFChecklistException {
		JSONParser parser = new JSONParser();
		JSONObject obj;
		try {
			obj = (JSONObject)parser.parse(json);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} 
		return obj;
	}
	
	public static JSONArray parseJsonToArray(String json) throws DRFChecklistException {
		JSONParser parser = new JSONParser();
		JSONArray obj;
		try {
			obj = (JSONArray)parser.parse(json);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} 
		return obj;
	}

	public static <T> T convertJsonToObject(String json, Class<T> clazz) throws DRFChecklistException {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			return mapper.readValue(json, clazz);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		}		
	}
	
	
	public static String convertObjectToJson(Object obj) throws DRFChecklistException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		}
	}
}
