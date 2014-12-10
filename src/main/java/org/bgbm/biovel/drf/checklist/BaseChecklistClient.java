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
import org.bgbm.biovel.drf.rest.TaxoRESTClient;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseChecklistClient extends TaxoRESTClient {

	protected Logger logger = LoggerFactory.getLogger(BaseChecklistClient.class);
	public final static String QUERY_PLACEHOLDER = "{q}";
	
	protected final static String CHECKLIST_KEY = "checklist";
	protected final static String CHECKLIST_URL_KEY = "checklist_url";
	protected final static String COPYRIGHT_URL_KEY = "copyright_url";
	protected final static String CHECKLIST_LIST = "checklist_list";
	
	
	

	public BaseChecklistClient() {
		super();
	}
	
	public BaseChecklistClient(String checklistInfoJson) throws DRFChecklistException {
		super(checklistInfoJson);
	}	
	
	public BaseChecklistClient(ServiceProviderInfo spInfo) throws DRFChecklistException {
		super(spInfo);
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
	
	public URI buildUriFromQueryList(List<TnrMsg.Query> queryList,			
			String endpointSuffix, 			
			String queryKey,
			Map<String, String> paramMap) {
		List<String> queries = new ArrayList<String>();
		Iterator<TnrMsg.Query> itrQuery = queryList.iterator();
		while(itrQuery.hasNext()) {
			queries.add(itrQuery.next().getTnrRequest().getTaxonName().getName().getFullName());
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
		return buildUriFromQueryString(query.getTnrRequest().getTaxonName().getName().getFullName(),
				endpointSuffix,
				queryKey,
				paramMap);
	}
	
	public URI buildUriFromQuery(TnrMsg.Query query,			
			String regexpUrl, 					
			Map<String, String> paramMap) {
		String url = regexpUrl.replace(QUERY_PLACEHOLDER, query.getTnrRequest().getTaxonName().getName().getFullName());
		return buildUriFromQueryString(url, paramMap);
	}
	
	
	
	public abstract void resolveNames(TnrMsg tnrMsg) throws DRFChecklistException;		
	
	

}
