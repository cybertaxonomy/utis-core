package org.bgbm.biovel.drf.checklist;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpHost;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg.Query;
import org.bgbm.biovel.drf.tnr.msg.TnrResponse;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Species2000ColClient extends BaseChecklistClient {

	public static final String ID = "species2000col";
	public static final String LABEL = "Species2000 - Catalogue Of Life";
	public static final String URL = "http://www.catalogueoflife.org";
	public static final String DATA_AGR_URL = "http://www.catalogueoflife.org/col/info/copyright";
	
		
	public Species2000ColClient() {
		super();

	}

	@Override
	public HttpHost getHost() {
		// TODO Auto-generated method stub
		return new HttpHost("www.catalogueoflife.org",80);
	}


	@Override
	protected ChecklistInfo buildChecklistInfo() {
		ChecklistInfo checklistInfo = new ChecklistInfo(ID,LABEL,URL,DATA_AGR_URL);
		setChecklistInfo(checklistInfo);
		return checklistInfo;
	}


	@Override	
	public void resolveNames(TnrMsg tnrMsg) throws DRFChecklistException {
		List<TnrMsg.Query> queryList = tnrMsg.getQuery();
		if(queryList.size() ==  0) {
			throw new DRFChecklistException("GBIF query list is empty");
		}

		if(queryList.size() > 1) {
			throw new DRFChecklistException("GBIF query list has more than one query");
		}
		Query query = queryList.get(0);

		//http://www.catalogueoflife.org/col/webservice?response=full&name={sciName}


		Map<String, String> paramMap = new HashMap<String, String>();
		
		paramMap.put("response", "full");		

		URI taxonUri = buildUriFromQuery(query,
				"/col/webservice",									
				"name",
				paramMap);

		String response = processRESTService(taxonUri);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		DocumentBuilder parser;
		try {
			parser = factory.newDocumentBuilder();

		Document doc = parser.parse(new InputSource(new StringReader(response)));   
		XPathExpression xp = XPathFactory.newInstance().newXPath().compile("/results/result[1]/name_status");
		Node nameStatusNode = (Node) xp.evaluate(doc, XPathConstants.NODE);
		
		if(nameStatusNode != null) {
			String nameStatus = nameStatusNode.getTextContent();
			if(nameStatus.equals("synonym")) {
				xp = XPathFactory.newInstance().newXPath().compile("/results/result[1]/accepted_name/id");
				Node synTaxonIdNode = (Node) xp.evaluate(doc, XPathConstants.NODE);
				
				if(synTaxonIdNode != null) {
					String synTaxonId = synTaxonIdNode.getTextContent();
					//http://www.catalogueoflife.org/col/webservice?response=full&id={sciId}
					taxonUri = buildUriFromQueryString(synTaxonId,
							"/col/webservice",									
							"id",
							paramMap);

					response = processRESTService(taxonUri);
				} else {
					response = null;
				}
			} 
		}
		
		updateQueryWithResponse(query, response);
		
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  		

	}
	

	@Override
	public int getMaxPageSize() {		
		return 10;
	}
	


	private void updateQueryWithResponse(Query query, String colXMLResponse) {

		Source inSource = new StreamSource(new StringReader(colXMLResponse));
		Source xslSource = new StreamSource(new StringReader(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/colres_to_synres.xsl","UTF-8")));

		// the factory pattern supports different XSLT processors
		// e.g. set the "javax.xml.transform.TransformerFactory" system property
		TransformerFactory tnfFact = TransformerFactory.newInstance();
		Transformer tnf;
		
		try {
			tnf = tnfFact.newTransformer(xslSource);

			Writer outputWriter = new StringWriter();
			
			tnf.transform(inSource, new StreamResult(outputWriter));
			
			String tnrResponseXML = outputWriter.toString();
			System.out.println(tnrResponseXML);
			TnrResponse tnrResponse = TnrMsgUtils.convertXMLToTnrResponse(tnrResponseXML);			
			query.getTnrResponse().add(tnrResponse);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}


	
}


