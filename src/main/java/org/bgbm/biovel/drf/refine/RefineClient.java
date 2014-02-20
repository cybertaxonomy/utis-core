package org.bgbm.biovel.drf.refine;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.json.simple.JSONObject;

public class RefineClient {
	private static String boundary = "----dcBoundary";
	
	private String server;
	private String port;
	
	public RefineClient(String server, String port) {
		this.server = server;
		this.port=port;
	}
	
	public String initJob() throws IOException, DRFChecklistException, URISyntaxException {
		String jobID = null;
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","application/http"));
		headers.add(new BasicHeader("Accept","application/json"));
		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/create-importing-job");
		
		String initResponse = callCoreRefineService(uriBuilder.build(),headers,null);
		
		JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(initResponse);
		Long jobIDLong = (Long)jsonResponse.get("jobID");
		if(jobIDLong != null) {
			jobID = String.valueOf(jobIDLong);
		}
		return jobID;
	}
	
	public String uploadData(String csvData, String jobID) throws RefineException, IOException, URISyntaxException {
		String uploadResponse = "";

		if(csvData != null && !csvData.isEmpty()) {
			MultipartEntityBuilder meBuilder = getMEBuilder();
			meBuilder.setBoundary(boundary);
			
			String lineEnding = "\r\n";	
			String beginStr = "--" + boundary + lineEnding +
					"Content-Disposition: form-data; name=\"upload\"; filename=\"dcinput.txt\"" + lineEnding +
					"Content-Type: text/plain" + lineEnding + lineEnding;
			String endStr = lineEnding + "--" + boundary + "--";
			//String inputBody = beginStr + csvData.replaceAll(System.getProperty("line.separator"),"\r\n") + endStr;
			String inputBody = beginStr + csvData + endStr;
			
			meBuilder.addBinaryBody("input", inputBody.getBytes());
			
			List<Header> headers = new ArrayList<Header>();
			headers.add(new BasicHeader("Content-Type","multipart/form-data; boundary=" + boundary));
			headers.add(new BasicHeader("Accept","text/html"));
			
			URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/importing-controller");
			uriBuilder.setParameter("jobID", jobID);
			uriBuilder.setParameter("subCommand","load-raw-data");
			uriBuilder.setParameter("controller","core/default-importing-controller");
			
			uploadResponse = callCoreRefineService(uriBuilder.build(),headers,new ByteArrayEntity(inputBody.getBytes()));
			
		} else {
			throw new RefineException("Data to be uploaded to Refine server is null or empty");
		}
		return uploadResponse;
	}
	
	public boolean checkImportJobStatus(String jobID) throws IOException, URISyntaxException, DRFChecklistException {
		boolean importDone = false;
		String checkResponse;
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","multipart/form-data"));
		headers.add(new BasicHeader("Accept","application/json"));

		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/get-importing-job-status");
		uriBuilder.setParameter("jobID", jobID);
		while(!importDone) {
			checkResponse = callCoreRefineService(uriBuilder.build(),headers,null);
			System.out.println("importJobCheck : " + checkResponse);
			JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(checkResponse);
			JSONObject job = (JSONObject)jsonResponse.get("job");
			JSONObject config = (JSONObject)job.get("config");
			boolean hasData = (Boolean)config.get("hasData");
			String state = (String)config.get("state");
			if(hasData && state.equals("ready"))
				importDone = true;
		}
		return importDone;
	}
	
	public String initializeParser(String jobID) throws IOException, URISyntaxException {
		
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","application/xml"));
		headers.add(new BasicHeader("Accept","application/json"));

		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/importing-controller");		
		uriBuilder.setParameter("controller","core/default-importing-controller");
		uriBuilder.setParameter("jobID", jobID);
		uriBuilder.setParameter("subCommand","initialize-parser-ui");
		uriBuilder.setParameter("format","text/line-based/*sv");
		
		return  callCoreRefineService(uriBuilder.build(),headers,null);				
	}
	
	public String updateFormat(String jobID) throws IOException, URISyntaxException {
		
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","application/x-www-form-urlencoded"));
		headers.add(new BasicHeader("Accept","application/json"));
		
		String options = "options={\"encoding\":\"\",\"separator\":\",\",\"ignoreLines\":-1,\"headerLines\":1,\"skipDataLines\":0,\"limit\":-1,\"storeBlankRows\":false,\"guessCellValueTypes\":true,\"processQuotes\":true,\"storeBlankCellsAsNulls\":true,\"includeFileSources\":false,\"projectName\":\"" + jobID + "\"}";
		
		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/importing-controller");		
		uriBuilder.setParameter("controller","core/default-importing-controller");
		uriBuilder.setParameter("jobID", jobID);
		uriBuilder.setParameter("subCommand","update-format-and-options");
		uriBuilder.setParameter("format","text/line-based/*sv");				

		return  callCoreRefineService(uriBuilder.build(),headers,new ByteArrayEntity(options.getBytes()));				
	}
	
	public String createProject(String jobID) throws IOException, URISyntaxException {
		
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","application/x-www-form-urlencoded"));
		headers.add(new BasicHeader("Accept","application/json"));

		String options = "options={\"encoding\":\"\",\"separator\":\",\",\"ignoreLines\":-1,\"headerLines\":1,\"skipDataLines\":0,\"limit\":-1,\"storeBlankRows\":false,\"guessCellValueTypes\":true,\"processQuotes\":true,\"storeBlankCellsAsNulls\":true,\"includeFileSources\":false,\"projectName\":\"" + jobID + "\"}";
		
		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/importing-controller");		
		uriBuilder.setParameter("controller","core/default-importing-controller");
		uriBuilder.setParameter("jobID", jobID);
		uriBuilder.setParameter("subCommand","create-project");		
		uriBuilder.setParameter("format","text/line-based/*sv");
		
		
		return  callCoreRefineService(uriBuilder.build(),headers,new ByteArrayEntity(options.getBytes()));				
	}
	
	public String checkCreateProjectStatus(String jobID) throws IOException, URISyntaxException, DRFChecklistException, InterruptedException {
		boolean createDone = false;
		String checkResponse;
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","application/xml"));
		headers.add(new BasicHeader("Accept","application/json"));

		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/get-importing-job-status");
		uriBuilder.setParameter("jobID", jobID);
		
		Long percent;
		String projectID = null;
		while(!createDone) {
			checkResponse = callCoreRefineService(uriBuilder.build(),headers,null);
			System.out.println("createProjectCheck : " + checkResponse);
			JSONObject jsonResponse = (JSONObject) JSONUtils.parseJsonToObject(checkResponse);
		    JSONObject job = (JSONObject)jsonResponse.get("job");
		    JSONObject config = (JSONObject)job.get("config");
		    JSONObject progress = (JSONObject)config.get("progress");
		    if(progress != null) {
		    	percent = (Long)progress.get("percent");
		    	Long projectIDLong = (Long)config.get("projectID");
		    	projectID = String.valueOf(projectIDLong);
		    	createDone = (percent == 100);
		    }
		    Thread.sleep(1000);
		}
		
		return projectID;
	}
	
	public String deleteProject(String projectID) throws IOException, URISyntaxException {
		
		List<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader("Content-Type","application/x-www-form-urlencoded"));
		headers.add(new BasicHeader("Accept","application/json"));		
		
		URIBuilder uriBuilder = getURIBuilder(server,port,"/command/core/delete-project");		
		uriBuilder.setParameter("project", projectID);
				
		return  callCoreRefineService(uriBuilder.build(),headers,null);		
		
	}
	
	public String callCoreRefineService(URI uri,
			List<Header> headers,
			HttpEntity reqEntity) throws IOException {
		
		
		String responseString = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		try {
			HttpPost httpPost = new HttpPost(uri);

			Iterator<Header> headerItr = headers.iterator();
			while(headerItr.hasNext()) {
				httpPost.addHeader(headerItr.next());
			}
			if(reqEntity != null) {				
				httpPost.setEntity(reqEntity);						
			}

			System.out.println("executing request " + httpPost.getRequestLine());
			CloseableHttpResponse response = httpClient.execute(httpPost);
			
			try {
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				System.out.println(response.toString());
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					StringWriter writer = new StringWriter();
					responseString = contentToString(resEntity.getContent());
					System.out.println(responseString);
					System.out.println("Response content length: " + resEntity.getContentLength());
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
		return responseString;
	}
	
	public MultipartEntityBuilder getMEBuilder() {		
		MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
		
		return meBuilder;
	}
	
	private String contentToString(InputStream content) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(content, writer, "UTF-8");
		return writer.toString();
	}
	
	private URIBuilder getURIBuilder(String host, String port, String path) {
		URIBuilder uriBuilder = new URIBuilder();		
		uriBuilder.setScheme("http");
		uriBuilder.setHost(host);
		uriBuilder.setPort(Integer.valueOf(port));
		uriBuilder.setPath(path);
		
		return uriBuilder;
	}
	

}
