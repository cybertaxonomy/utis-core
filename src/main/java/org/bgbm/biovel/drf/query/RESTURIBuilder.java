package org.bgbm.biovel.drf.query;

import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

public class RESTURIBuilder extends URIBuilder {

	private String querykey;

	public RESTURIBuilder(String hostName,
			int port,
			String endpointSuffix, 
			String key,
			Map<String, String> paramMap) {
		this.querykey = key;
		setScheme("http");
		setHost(hostName);
		setPort(port);
		setPath(endpointSuffix);		
		if(paramMap != null) {
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				setParameter(entry.getKey(), entry.getValue());		    
			}
		}
	}
	
	public RESTURIBuilder(String hostName,
			int port,
			String endpointUrl,
			Map<String, String> paramMap) {
		//this.querykey = key;
		setScheme("http");
		setHost(hostName);
		setPort(port);
		setPath(endpointUrl);		
		if(paramMap != null) {
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				setParameter(entry.getKey(), entry.getValue());		    
			}
		}
	}
	
	public void addQuery(String query) {
		addParameter(querykey, query.trim());		 
	}
	
}
