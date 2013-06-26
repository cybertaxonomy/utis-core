package org.bgbm.biovel.drf.checklist;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseChecklistClient {

	protected Logger logger = LoggerFactory.getLogger(BaseChecklistClient.class);
	public final static String QUERY_PLACEHOLDER = "{q}";
	
	protected final static String CHECKLIST_KEY = "checklist";
	protected final static String CHECKLIST_URL_KEY = "checklist_url";
	protected final static String COPYRIGHT_URL_KEY = "copyright_url";
	protected final static String CHECKLIST_LIST = "checklist_list";
	
	private ChecklistInfo checklistInfo;
	

	public BaseChecklistClient() {
		checklistInfo = buildChecklistInfo();
	}
	
	public ChecklistInfo getChecklistInfo() {
		return checklistInfo;
	}

	public String getChecklistInfoAsJson() throws DRFChecklistException {
		
		if(getChecklistInfo() != null) {
			return JSONUtils.convertObjectToJson(checklistInfo);
		}
		return null;
	}
	
	public void setChecklistInfo(ChecklistInfo checklistInfo) {
		this.checklistInfo = checklistInfo;
	}
	

	public String processRESTService(URI uri) throws DRFChecklistException {	
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);

		try {			
			System.out.println(">> Request URI: " + request.getRequestLine().getUri());
			HttpResponse response = client.execute(request);

			String strResponse = EntityUtils.toString(response.getEntity());
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


	
	public void queryChecklist(TnrMsg tnrMsg) throws DRFChecklistException {								
		resolveNames(tnrMsg);					
	}
	
	public TnrMsg queryChecklist(List<TnrMsg> tnrMsgs) throws DRFChecklistException {			
		
		TnrMsg finalTnrMsg = new TnrMsg();
		Iterator<TnrMsg> itrTnrMsg = tnrMsgs.iterator();
		while(itrTnrMsg.hasNext()) {
			Iterator<TnrMsg.Query> itrQuery = itrTnrMsg.next().getQuery().iterator();
			while(itrQuery.hasNext()) {				
				finalTnrMsg.getQuery().add(itrQuery.next());							
			}
		}
		
		resolveNames(finalTnrMsg);
		
		return finalTnrMsg;
	}
	
	
	public URI buildUriFromQueryStringList(List<String> queryList, 
			String endpointSuffix, 
			String queryKey,
			Map<String, String> paramMap) {
		
		ChecklistURIBuilder builder = new ChecklistURIBuilder(getHost().getHostName(),
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
		
		ChecklistURIBuilder builder = new ChecklistURIBuilder(getHost().getHostName(),
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
		
		ChecklistURIBuilder builder = new ChecklistURIBuilder(getHost().getHostName(), endpointUrl, paramMap);
		
		URI uri = null;		
						
		try {
			uri = builder.build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return uri;
	}	
	
	public URI buildUriFromQueryList(List<TnrMsg.Query> queryList,			
			String endpointSuffix, 			
			String queryKey,
			Map<String, String> paramMap) {
		List<String> queries = new ArrayList<String>();
		Iterator<TnrMsg.Query> itrQuery = queryList.iterator();
		while(itrQuery.hasNext()) {
			queries.add(itrQuery.next().getTnrRequest().getTaxonName().getName().getNameComplete());
		}
		System.out.println("Query size : " + queries.size());
		return buildUriFromQueryStringList(queries,
				endpointSuffix,
				queryKey,
				paramMap);
	}
	
	public URI buildUriFromQuery(TnrMsg.Query query,			
			String endpointSuffix, 			
			String queryKey,
			Map<String, String> paramMap) {
		return buildUriFromQueryString(query.getTnrRequest().getTaxonName().getName().getNameComplete(),
				endpointSuffix,
				queryKey,
				paramMap);
	}
	
	public URI buildUriFromQuery(TnrMsg.Query query,			
			String regexpUrl, 					
			Map<String, String> paramMap) {
		String url = regexpUrl.replace(QUERY_PLACEHOLDER, query.getTnrRequest().getTaxonName().getName().getNameComplete());
		return buildUriFromQueryString(url, paramMap);
	}
	
	protected abstract ChecklistInfo buildChecklistInfo();
	
	public abstract void resolveNames(TnrMsg tnrMsg) throws DRFChecklistException;
	
	public abstract HttpHost getHost();		
	
	public abstract int getMaxPageSize();
	
	public static class ChecklistInfo {
		
		private String id;
		private String label;
		private String url;
		private String copyrightUrl;
		private boolean use = false;
		private List<ChecklistInfo> subChecklists = null;
		
		public ChecklistInfo() {			
		}
		
		public ChecklistInfo(String id, String label, String url) {
			this(id,label,url,"");
		}
		
		public ChecklistInfo(String id, String label, String url, String copyrightUrl) {
			this(id,label,url,copyrightUrl,false);			
		}
		
		public ChecklistInfo(String id, String label, String url, String copyrightUrl, boolean use) {
			this.id = id;
			this.label = label;
			this.url = url;
			this.copyrightUrl = copyrightUrl;
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
		
		public boolean getUse() {
			return use;
		}
		
		public void addSubChecklist(ChecklistInfo ci) {
			if(subChecklists == null) {
				subChecklists = new ArrayList<ChecklistInfo>();
			}
			subChecklists.add(ci);
		}
		
		public List<ChecklistInfo> getSubChecklists() {
			return subChecklists;
		}
		
		public static ChecklistInfo create(String[] ciArray) throws DRFChecklistException {
			if(ciArray.length != 4) {
				throw new DRFChecklistException("Not correct number of elements to create Checklist Info");
			}
			return new ChecklistInfo(ciArray[0],ciArray[1],ciArray[2],ciArray[3]);
		}
		
			
	}

}
