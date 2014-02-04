package org.bgbm.biovel.drf.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bgbm.biovel.drf.checklist.BaseChecklistClient;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.RESTURIBuilder;
import org.bgbm.biovel.drf.utils.JSONUtils;

public abstract class TaxoRESTClient {
	
	private ServiceProviderInfo spInfo;
	
	public TaxoRESTClient() {
		spInfo = buildServiceProviderInfo();
	}
	
	public TaxoRESTClient(String checklistInfoJson) throws DRFChecklistException {
		setChecklistInfo(JSONUtils.convertJsonToObject(checklistInfoJson, BaseChecklistClient.ServiceProviderInfo.class));
	}
	
	public TaxoRESTClient(ServiceProviderInfo spInfo) throws DRFChecklistException {
		setChecklistInfo(spInfo);
	}
	
	public abstract ServiceProviderInfo buildServiceProviderInfo();
	
	public ServiceProviderInfo getServiceProviderInfo() {
		return spInfo;
	}
	
	public String getChecklistInfoAsJson() throws DRFChecklistException {
		
		if(getServiceProviderInfo() != null) {
			return JSONUtils.convertObjectToJson(spInfo);
		}
		return null;
	}
	
	public void setChecklistInfo(ServiceProviderInfo checklistInfo) {
		this.spInfo = checklistInfo;
	}
	
	public String processRESTService(URI uri) throws DRFChecklistException {	
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);

		try {			
			System.out.println(">> Request URI: " + request.getRequestLine().getUri());
			HttpResponse response = client.execute(request);

			String strResponse = EntityUtils.toString(response.getEntity(),Charset.forName("UTF-8"));
			System.out.println("<< Response: " + response.getStatusLine());
			System.out.println(strResponse);
			System.out.println("==============");

			return strResponse;
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DRFChecklistException(e);
		} 
		return null;
	}
	
	public abstract HttpHost getHost();
	
	public abstract int getMaxPageSize();
	
	
	public URI buildUriFromQueryStringList(List<String> queryList, 
			String endpointSuffix, 
			String queryKey,
			Map<String, String> paramMap) {
		
		RESTURIBuilder builder = new RESTURIBuilder(getHost().getHostName(),
					getHost().getPort(),
					endpointSuffix, 
					queryKey,
					paramMap);
		
		URI uri = null;
		Iterator<String> itrQuery = queryList.iterator();
		while(itrQuery.hasNext()) {
		    builder.addQuery(itrQuery.next());		    	
		}		
		
		try {
			uri = builder.build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return uri;
	}		
	
	public URI buildUriFromQueryString(String query, 
			String endpointSuffix, 
			String queryKey,
			Map<String, String> paramMap) {
		
		RESTURIBuilder builder = new RESTURIBuilder(getHost().getHostName(),
					getHost().getPort(),
					endpointSuffix, 
					queryKey,
					paramMap);
		
		URI uri = null;

		builder.addQuery(query);		    	
						
		try {
			uri = builder.build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return uri;
	}	
	
	public URI buildUriFromQueryString(String endpointUrl, 			
			Map<String, String> paramMap) {
		
		RESTURIBuilder builder = new RESTURIBuilder(getHost().getHostName(), getHost().getPort(),endpointUrl, paramMap);
		
		URI uri = null;		
						
		try {
			uri = builder.build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return uri;
	}	
	
public static class ServiceProviderInfo {
		
		private String id;
		private String label;
		private String url;
		private String copyrightUrl;
		private String version;
		private boolean use = false;
		private List<ServiceProviderInfo> subChecklists = null;
		
		public ServiceProviderInfo() {			
		}
		
		public ServiceProviderInfo(String id, String label, String url) {
			this(id,label,url,"");
		}
		
		public ServiceProviderInfo(String id, String label, String url, String copyrightUrl) {
			this(id,label,url,copyrightUrl,false);			
		}
		
		public ServiceProviderInfo(String id, String label, String url, String copyrightUrl, boolean use) {
			this(id,label,url,copyrightUrl,"",use);
		}
		
		public ServiceProviderInfo(String id, String label, String url, String copyrightUrl, String version, boolean use) {
			this.id = id;
			this.label = label;
			this.url = url;
			this.copyrightUrl = copyrightUrl;
			this.version = version;
			this.use = use;
			subChecklists = null;
		}
				
		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		public String getUrl() {
			return url;
		}
		
		public String getCopyrightUrl() {
			return copyrightUrl;
		}
		

		public String getVersion() {
			return version;
		}
		
		public boolean getUse() {
			return use;
		}
		
		public void addSubChecklist(ServiceProviderInfo ci) {
			if(subChecklists == null) {
				subChecklists = new ArrayList<ServiceProviderInfo>();
			}
			subChecklists.add(ci);
		}
		
		public List<ServiceProviderInfo> getSubChecklists() {
			if(subChecklists != null && subChecklists.size() > 0) { 
				Collections.sort(subChecklists,new ServiceProviderInfoComparator());
			}
			return subChecklists;
		}
		
		public static ServiceProviderInfo create(String[] ciArray) throws DRFChecklistException {
			if(ciArray.length != 4) {
				throw new DRFChecklistException("Not correct number of elements to create Checklist Info");
			}
			return new ServiceProviderInfo(ciArray[0],ciArray[1],ciArray[2],ciArray[3]);
		}

		public class ServiceProviderInfoComparator implements Comparator<ServiceProviderInfo> {
			  @Override
			  public int compare(ServiceProviderInfo spia, ServiceProviderInfo spib) {
				  return spia.getLabel().compareTo(spib.getLabel());
			  }

			}
			
	}

}
