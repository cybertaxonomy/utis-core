package org.bgbm.biovel.drf.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bgbm.biovel.drf.checklist.SearchMode;
import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Query.ClientStatus;
import org.bgbm.biovel.drf.tnr.msg.Query.Request;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class TnrMsgUtils {

        public static Node convertTnrMsgToNode(TnrMsg tnrMsg) throws JAXBException, ParserConfigurationException {
            JAXBContext context = JAXBContext.newInstance(TnrMsg.class);

            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out
            //m.marshal(tnrMsg, System.out);

            // Write to Node
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();

            m.marshal(tnrMsg, doc);
            return doc;
        }

        public static String convertTnrMsgToJson(TnrMsg tnrMsg) throws JsonGenerationException, JsonMappingException, IOException {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            mapper.setAnnotationIntrospector(introspector);

            // Printing JSON
            return mapper.writeValueAsString(tnrMsg);
        }

        public static String convertTnrMsgToXML(TnrMsg tnrMsg) throws TnrMsgException  {
            Node node;
            try {
                node = convertTnrMsgToNode(tnrMsg);

            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter sw = new StringWriter();
            t.transform(new DOMSource(node), new StreamResult(sw));

            return sw.toString();
            } catch (JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new TnrMsgException(e);
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new TnrMsgException(e);
            } catch (TransformerConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new TnrMsgException(e);
            } catch (TransformerFactoryConfigurationError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new TnrMsgException(e);
            } catch (TransformerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new TnrMsgException(e);
            }

        }

        public static TnrMsg convertXMLToTnrMsg(String xmlMsg) throws JAXBException {
            JAXBContext context = JAXBContext.newInstance(TnrMsg.class);

            Unmarshaller um = context.createUnmarshaller();
            return (TnrMsg) um.unmarshal(new StringReader(xmlMsg));
        }

        public static Response convertXMLToTnrResponse(String xmlMsg) throws JAXBException, ParserConfigurationException, SAXException, IOException {
            JAXBContext context = JAXBContext.newInstance(Response.class);

            Unmarshaller um = context.createUnmarshaller();
            return (Response) um.unmarshal(new StringReader(xmlMsg));
        }

        public static TnrMsg convertXMLListToTnrMsg(List<String> xmlMsg) throws JAXBException {
            TnrMsg finalTnrMsg = new TnrMsg();
            Iterator<String> itrXMLMsg = xmlMsg.iterator();
            while(itrXMLMsg.hasNext()) {
                TnrMsg tnrMsg = convertXMLToTnrMsg(itrXMLMsg.next());
                finalTnrMsg.getQuery().add(tnrMsg.getQuery().get(0));
            }
            return finalTnrMsg;
        }

        public static List<TnrMsg> convertXMLListToTnrMsgList(List<String> xmlMsgs) throws JAXBException {
            List<TnrMsg> finalTnrMsgList = new ArrayList<TnrMsg>();
            Iterator<String> itrXMLMsg = xmlMsgs.iterator();
            while(itrXMLMsg.hasNext()) {
                TnrMsg tnrMsg = convertXMLToTnrMsg(itrXMLMsg.next());
                finalTnrMsgList.add(tnrMsg);
            }
            return finalTnrMsgList;
        }

        public static TnrMsg convertStringToTnrMsg(String name, SearchMode searchMode) {
            TnrMsg tnrMsg = new TnrMsg();
            Query query = new Query();
            Request request = new Request();

            request.setName(name);
            request.setSearchMode(searchMode.toString());
            query.setRequest(request);
            tnrMsg.getQuery().add(query);

            return tnrMsg;
        }

        public static List<TnrMsg> convertStringListToTnrMsgList(List<String> names, SearchMode searchMode) {
            List<TnrMsg> tnrMsgList = new ArrayList<TnrMsg>();
            Iterator<String> itrStringMsg = names.iterator();
            while(itrStringMsg.hasNext()) {
                TnrMsg tnrMsg = convertStringToTnrMsg(itrStringMsg.next(), searchMode);
                tnrMsgList.add(tnrMsg);
            }
            return tnrMsgList;
        }

        public static TnrMsg mergeTnrMsgs(List<TnrMsg> tnrMsgs) {
            Map<String,Query> nameQueryMap = new HashMap<String,Query>();
            Iterator<TnrMsg> itrTnrMsg = tnrMsgs.iterator();
            while(itrTnrMsg.hasNext()) {
                TnrMsg currentTnrMsg = itrTnrMsg.next();
                Iterator<Query> itrQuery = currentTnrMsg.getQuery().iterator();
                while(itrQuery.hasNext()) {
                    Query currentQuery = itrQuery.next();
                    String nameComplete = currentQuery.getRequest().getName();
                    Query query = nameQueryMap.get(nameComplete);
                    if(query == null) {
                        nameQueryMap.put(nameComplete, currentQuery);
                    } else {
                        query.getResponse().addAll(currentQuery.getResponse());
                    }
                }
            }
            TnrMsg finalTnrMsg = new TnrMsg();
            finalTnrMsg.getQuery().addAll(new ArrayList<Query>(nameQueryMap.values()));
            return  finalTnrMsg;
        }

        public static TnrMsg mergeTnrXMLList(List<String> xmlMsgs) throws JAXBException {
            return mergeTnrMsgs(convertXMLListToTnrMsgList(xmlMsgs));
        }

        /**
         * @param ci
         * @return
         */
        public static Response tnrResponseFor(ServiceProviderInfo ci) {
            Response tnrResponse = new Response();
            tnrResponse.setChecklistId(ci.getId());
            tnrResponse.setChecklist(ci.getLabel());
            tnrResponse.setChecklistUrl(ci.getDocumentationUrl());
            return tnrResponse;
        }

        public static ClientStatus tnrClientStatusFor(ServiceProviderInfo ci) {
            ClientStatus tnrClientStatus = new ClientStatus();
            tnrClientStatus.setChecklistId(ci.getId());
            return tnrClientStatus;
        }

        /**
         * Sets the given <code>searchMode</code> to the requests in all queries of the TnrMsg
         *
         * @deprecated this is a temporary solution to overcome the inconsistencies in the
         * current implementation of the SearchModes i the library
         */
        @Deprecated
        public static void updateWithSearchMode(TnrMsg tnrMsg, SearchMode searchMode){
            for(Query query :  tnrMsg.getQuery()){
                query.getRequest().setSearchMode(searchMode.toString());
            }
        }




}
