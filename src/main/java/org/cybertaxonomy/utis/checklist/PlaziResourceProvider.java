/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.httpclient.util.DateUtil;
import org.cybertaxonomy.utis.store.LastModifiedProvider;
import org.cybertaxonomy.utis.store.ResourceProvider;
import org.cybertaxonomy.utis.utils.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Nov 9, 2016
 *
 */
public class PlaziResourceProvider extends HttpClient implements ResourceProvider, LastModifiedProvider {

    /**
     *
     */
    private static final String XML_ELEMENT_ITEM = "item";
    private static final String XML_ELEMENT_LINK = "link";
    private static final String XML_ELEMENT_PUBDATE = "pubDate";

    /**
     * time in milliseconds after which the local copy of the rss-feed expires
     *
     * Currently at 30 minutes
     */
    private static final long FILE_EXPIRE_MS = 1000 * 60 * 30;

    protected Logger logger = LoggerFactory.getLogger(PlaziResourceProvider.class);

    private PlaziClient plaziClient;

    private File rssFeedFile = null;

    private final Pattern lastBuildDatePattern = Pattern.compile("<lastBuildDate>([^<]*)</lastBuildDate>");

    /**
     * The expected datetime format is a variant of the Atom format
     * An example: 2016-11-09T02:59:44-02:00
     *
     * This can be parsed by omitting the timezone information in the pattern
     * yyyy-MM-dd'T'HH:mm:ssZ
     *
     * If more accuracy is needed the following link provides helpful information:
     * http://stackoverflow.com/questions/2375222/java-simpledateformat-for-time-zone-with-a-colon-separator
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public PlaziResourceProvider(PlaziClient storeInfo) {

        this.plaziClient = storeInfo;

        // set timezone as GMT to conform to the timezone used for the store timestamps
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.cybertaxonomy.utis.store.LastModifiedProvider#getLastModified()
     */
    @Override
    public Date getLastModified() throws DRFChecklistException {

        File archiveFile = fetchRSSFile();

        if(archiveFile != null){
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(archiveFile));
                String line;
                Date lastModified = null;
                while ((line = br.readLine()) != null) {
                    Matcher m = lastBuildDatePattern.matcher(line);
                    if (m.find()) {
                        lastModified = dateFormat.parse(m.group(1));
                    }
                    if(lastModified != null){
                        logger.debug("lastModified:" + DateUtil.formatDate(lastModified));
                        return lastModified;
                    }
                }
            } catch (IOException | ParseException e) {
                throw new DRFChecklistException(
                        "Error reading the fetched lastModified Test file, this blocks updating the store", e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        /* IGNORE */}
                }
            }
        }
        return null;
    }

    /**
     * @return
     * @throws DRFChecklistException
     */
    protected File fetchRSSFile() {

        if(rssFeedFile != null && rssFeedFile.canRead()){
            // re-use the local copy if not outdated
            if(rssFeedFile.lastModified() + FILE_EXPIRE_MS > System.currentTimeMillis()){
                return rssFeedFile;
            }
        }

        try {
            rssFeedFile = toTempFile(plaziClient.getTestUrl());
        } catch (IOException | URISyntaxException e) {
            logger.error("Error fetching rss File", e);
        }

        return rssFeedFile;
    }

    /**
     * expires the cached rss file
     */
    public void expireRssFile(){
        rssFeedFile = null;
    }

    protected Date parseLastBuildDate(String line) throws ParseException{

        Matcher m = lastBuildDatePattern.matcher(line);
        if (m.find()) {
            return dateFormat.parse(m.group(1));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<URI> getResources(Date lastUpdated) throws DRFChecklistException {


        List<URI> resources = new ArrayList<>();

        File rssFile = fetchRSSFile();

        if(rssFile == null){
            // an error occurred in fetchRSSFile
            return resources;
        }
        FileInputStream in;
        boolean inItem = false;
        boolean inPubDate = false;
        boolean inLink = false;
        Date pubDate = null;
        String location = null;
        try {
            in = new FileInputStream(rssFile);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader parser = factory.createXMLStreamReader(in);
            while (true) {
                int event = parser.next();
                if (event == XMLStreamConstants.END_DOCUMENT) {
                    parser.close();
                    break;
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if(parser.getLocalName().equals(XML_ELEMENT_ITEM)){
                        inItem = true;
                    }
                    inPubDate = parser.getLocalName().equals(XML_ELEMENT_PUBDATE);
                    inLink = parser.getLocalName().equals(XML_ELEMENT_LINK);

                }
                if (event == XMLStreamConstants.CHARACTERS) {
                    if(inItem){
                        String elementText = parser.getText();
                        if(inPubDate){
                            try {
                                pubDate = dateFormat.parse(elementText);
                            } catch (ParseException e) {
                                logger.error("Plazi - Invalid date format in xml.rss.xml: " + elementText);
                            }
                        }
                        if(inLink){
                            location = elementText;
                        }
                    }
                }
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if(parser.getLocalName().equals(XML_ELEMENT_ITEM)){
                        if(pubDate != null && location != null){

                            String taxonConceptID = plaziClient.checkTreatmentIdentifier(location.replace("http://tb.plazi.org/GgServer/xml", "http://treatment.plazi.org/id"));
                            if(taxonConceptID == null){
                                logger.info("Adding resource " + location + " for import.");
                                try {
                                    location = location.replace("/GgServer/xml/", "/GgServer/cdmRdf/");
                                    resources.add(URI.create(location));
                                } catch (IllegalArgumentException e) {
                                    logger.error("Plazi - Location string is not a proper URI: " + location);
                                }
                            } else {
                               logger.info("Resource " + location + " already exists.");
                            }
                        }
                        inItem = false;
                    }
                    inPubDate = false;
                    inLink = false;
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return resources;
    }

}
