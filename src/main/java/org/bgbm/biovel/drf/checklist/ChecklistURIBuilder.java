package org.bgbm.biovel.drf.checklist;

import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

public class ChecklistURIBuilder extends URIBuilder {

	private String querykey;

	public ChecklistURIBuilder(String hostName,
			String endpointSuffix, 
			String key,
			Map<String, String> paramMap) {
		this.querykey = key;
		setScheme("http");
		setHost(hostName);
		setPath(endpointSuffix);		
		if(paramMap != null) {
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				setParameter(entry.getKey(), entry.getValue());		    
			}
		}
	}
	
	public ChecklistURIBuilder(String hostName,
			String endpointUrl,
			Map<String, String> paramMap) {
		//this.querykey = key;
		setScheme("http");
		setHost(hostName);
		setPath(endpointUrl);		
		if(paramMap != null) {
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				setParameter(entry.getKey(), entry.getValue());		    
			}
		}
	}
	
	public void addQuery(String query) {
		addParameter(querykey, query);		 
	}
	
}
