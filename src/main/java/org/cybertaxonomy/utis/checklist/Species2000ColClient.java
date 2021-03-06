package org.cybertaxonomy.utis.checklist;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.RestClient;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.BiovelUtils;
import org.cybertaxonomy.utis.utils.IdentifierUtils;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Species2000ColClient extends BaseChecklistClient<RestClient> {

    private static final Logger logger = LoggerFactory.getLogger(Species2000ColClient.class);


    private static final HttpHost HTTP_HOST = new HttpHost("www.catalogueoflife.org",80);
    public static final String ID = "species2000col";
    public static final String LABEL = "Species2000 - Catalogue Of Life";
    public static final String URL = "http://www.catalogueoflife.org";
    public static final String DATA_AGR_URL = "http://www.catalogueoflife.org/col/info/copyright";

    public static final EnumSet<SearchMode> SEARCH_MODES = EnumSet.of(SearchMode.scientificNameExact);

    public static final EnumSet<ClassificationAction> CLASSIFICATION_ACTION = EnumSet.noneOf(ClassificationAction.class);


    public Species2000ColClient() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatelessClient() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initQueryClient() {
        queryClient = new RestClient(HTTP_HOST);

    }

    @Override
    public ServiceProviderInfo buildServiceProviderInfo() {
        ServiceProviderInfo checklistInfo = new ServiceProviderInfo(ID,LABEL,URL,DATA_AGR_URL, getSearchModes());
        return checklistInfo;
    }


    @Override
    public void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {

        Query query = singleQueryFrom(tnrMsg);

        //http://www.catalogueoflife.org/col/webservice?response=full&name={sciName}


        Map<String, String> paramMap = new HashMap<String, String>();

        paramMap.put("response", "full");

        URI taxonUri = queryClient.buildUriFromQuery(query,
                "/col/webservice",
                "name",
                paramMap);

        String responseBody = queryClient.get(taxonUri);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser;
        try {
            parser = factory.newDocumentBuilder();

        Document doc = parser.parse(new InputSource(new StringReader(responseBody)));
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
                    taxonUri = queryClient.buildUriFromQueryString(synTaxonId,
                            "/col/webservice",
                            "id",
                            paramMap);

                    responseBody = queryClient.get(taxonUri);
                } else {
                    responseBody = null;
                }
            }
        }

        updateQueryWithResponse(query, responseBody);

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

    private void updateQueryWithResponse(Query query, String colXMLResponse) {

        Source inSource = new StreamSource(new StringReader(colXMLResponse));
        Source xslSource = new StreamSource(new StringReader(BiovelUtils.getResourceAsString("/org/cybertaxonomy/utis/tnr/colres_to_synres.xsl","UTF-8")));

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
            Response tnrResponse = TnrMsgUtils.convertXMLToTnrResponse(tnrResponseXML);
            query.getResponse().add(tnrResponse);
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

    @Override
    public void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public EnumSet<SearchMode> getSearchModes() {
        return SEARCH_MODES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<ClassificationAction> getClassificationActions() {
        return CLASSIFICATION_ACTION;
    }

    @Override
    public void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSupportedIdentifier(String value) {
        return IdentifierUtils.checkLSID(value);
    }

    @Override
    public void findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("taxonomicChildren mode not supported by " + this.getClass().getSimpleName());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void higherClassification(TnrMsg tnrMsg) throws DRFChecklistException {
        throw new DRFChecklistException("higherClassification mode not supported by " + this.getClass().getSimpleName());

    }
}


