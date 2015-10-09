/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package org.bgbm.biovel.drf.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.EEA_BDC_Client.RdfSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Sep 30, 2015
 *
 */
public class SparqlClient implements IQueryClient {

    protected Logger logger = LoggerFactory.getLogger(SparqlClient.class);

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    private Opmode opmode = null;

    private final String baseUri;

    private File rdfFile = null;

    private final Model model = null;

    private Dataset dataset = null;

    public enum Opmode{
        SPARCLE_ENDPOINT, RDF_ARCHIVE;
    }

    /**
     *
     */
    public SparqlClient(String baseUri, Opmode opmode) {
        this.baseUri = baseUri;
        this.opmode = opmode;
        if(opmode.equals(Opmode.RDF_ARCHIVE)) {
            if(baseUri != null) {
                this.rdfFile = downloadAndExtract();
            }
            try {
                createStore();
//                loadModel();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    private File downloadAndExtract() {
        File expandedFile = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        try {
            // 1. download and store in local filesystem in TMP
            logger.debug("downloading rdf file from " + baseUri);
            HttpGet httpGet = new HttpGet(baseUri);
            response = httpClient.execute(httpGet);
            String archiveFileName = FilenameUtils.getName(httpGet.getURI().getRawPath());
            File archiveFile = new File(tmpDir, archiveFileName);
            FileOutputStream fout = new FileOutputStream(archiveFile);
            IOUtils.copy(response.getEntity().getContent(), new FileOutputStream(archiveFile));
            fout.close();
            logger.debug(archiveFile.length() + " bytes downloaded to " + archiveFile.getCanonicalPath());

            // 2. extract the archive
            FileInputStream fin = new FileInputStream(archiveFile);
            InputStream ain = null;

            if(GzipUtils.isCompressedFilename(archiveFileName)) {
                logger.debug("Extracting GZIP file " + archiveFile.getCanonicalPath());
                ain = new GzipCompressorInputStream(fin);
            } else {
                // TO UNZIP
                //ArchiveInputStream ain = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, fin);
            }

            expandedFile = new File(tmpDir, GzipUtils.getUncompressedFilename(archiveFileName));
            fout = new FileOutputStream(expandedFile);
            IOUtils.copy(ain, fout);
            fout.close();
            fin.close();
            logger.debug("Extracted to " + expandedFile.getCanonicalPath());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return expandedFile;
    }

    /**
     * WARNING!!! This needs at least 1.5GB of heap space!!!
     * set -Xmx1500M
     *
     * NOTE: The bulkloader is a faster way to load data into an empty dataset than just using the Jena update operations.
     * the bulkloader also requires less memory
     * It is accessed through the command line utility tdbloader.
     * @throws IOException
     */
    private void createStore() throws IOException {

        File tdbStoreFolder = new File(tmpDir, "drf_tnb_store" + File.separator);
        if(tdbStoreFolder.exists()) {
            if(rdfFile != null) {
                FileUtils.cleanDirectory(tdbStoreFolder);
            }
        } else {
            tdbStoreFolder.mkdirs();
        }
        Location location = Location.create(tdbStoreFolder.toString());

        Dataset dataset = TDBFactory.createDataset(location);

        dataset.begin(ReadWrite.READ) ;
        // Get model inside the transaction
        Model model = dataset.getDefaultModel() ;
        dataset.end();

        if(rdfFile != null) {
            dataset.begin(ReadWrite.WRITE);
            model = dataset.getDefaultModel();
            // parse InputStream as RDF in Turtle format
            InputStream fin = new FileInputStream(rdfFile);
            logger.info("loading DRF/XML into TDB store");
            model.read(fin, null, "RDF/XML");
            logger.info("loading DRF/XML done");
            dataset.end();
            logger.info("rdf loaded into TDB store at " + tdbStoreFolder);
        }

        this.dataset = dataset;
    }

    public String select(String queryString) throws DRFChecklistException {

        QueryExecution qe = executionFor(queryString);

        try {
            ResultSet results = qe.execSelect();
            System.err.println(ResultSetFormatter.asText(results));
        } catch (HttpException e) {
            switch(e.getResponseCode()) {
                // interpretation based on
                // http://image.slidesharecdn.com/swtss1006sparql-100614020655-phpapp02/95/semantic-web-technologies-ss-2010-06-sparql-46-728.jpg?cb=1276481316
                case 400:
                    throw new DRFChecklistException("Malformed Query ?", e);
                case 500:
                    throw new DRFChecklistException("Query Request Refused ?", e);
                default:
                    throw e;
            }
        } finally {
            // Important - free up resources used running the query
            qe.close();
        }

        return null;
    }

    public Model describe(String queryString) throws DRFChecklistException {

        QueryExecution qe = executionFor(queryString);
        Model model = null;
        try {
            model = qe.execDescribe();
            if(dataset != null) {
                dataset.begin(ReadWrite.READ) ;
            }
            if(dataset== null && logger.isDebugEnabled()) {
                model.write(System.err);
            }
            if(dataset != null) {
                dataset.end();
            }

        } catch (HttpException e) {
            switch(e.getResponseCode()) {
                // interpretation based on
                // http://image.slidesharecdn.com/swtss1006sparql-100614020655-phpapp02/95/semantic-web-technologies-ss-2010-06-sparql-46-728.jpg?cb=1276481316
                case 400:
                    throw new DRFChecklistException("Malformed Query ?", e);
                case 500:
                    throw new DRFChecklistException("Query Request Refused ?", e);
                default:
                    throw e;
            }
        } finally {
            // Important - free up resources used running the query
            qe.close();
        }

        if(model != null && logger.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("subjects in response:\n");
            int i = 1;
            for(ResIterator it = model.listSubjects(); it.hasNext(); ++i) {
                Resource res = it.next();
                msg.append("    " + i + ": " + res.toString() + "\n");
            }
            logger.debug(msg.toString());
        }

        return model;
    }

    /**
     * @param queryString
     * @return
     */
    private QueryExecution executionFor(String queryString) {

        Query query = QueryFactory.create(queryString);

        QueryExecution qe;
        // Execute the query and obtain results
        if(opmode.equals(Opmode.SPARCLE_ENDPOINT)) {
            qe = QueryExecutionFactory.sparqlService(baseUri, query);
        } else {
            // RDF_ARCHIVE
            if(model != null) {
                // in-memory model
                qe = QueryExecutionFactory.create(queryString, model);
            } else if(dataset != null) {
                // TDB Store
                qe = QueryExecutionFactory.create(queryString, dataset);
            } else {
                throw new RuntimeException("Opmode is RDF_ARCHIVE but model was null");
            }
        }
        return qe;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public RDFNode asSingleObject(Resource subject, RdfSchema nameSpace, String localName) {
        RDFNode node = null;
        Resource _subject = subject;
        try {
            boolean hasNoPropertiesInGraph = !_subject.listProperties().hasNext();
            if(_subject.isURIResource() && hasNoPropertiesInGraph ) {
                logger.debug("loading RDF for UriResource " + _subject.getURI());
                _subject = getFromUri(_subject.getURI());
            }
            Model _model = _subject.getModel();
            node = _subject.listProperties(_model.getProperty(nameSpace.schemaUri(), localName)).next().getObject();
        } catch (NoSuchElementException e) {
            if(logger.isDebugEnabled()) {
                logger.debug(_subject.getURI() + " " +  nameSpace + ":" + localName + " not found in current graph");
                printProperties(_subject);
            }
        }
        return node;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public String objectAsString(Resource subject, RdfSchema nameSpace, String localName) {
        String txt = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if(node != null) {
            txt = node.toString();
        }
        return txt;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public Resource objectAsResource(Resource subject, RdfSchema nameSpace, String localName) {
        Resource resource = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if(node != null) {
            node.isResource();
            resource  = node.asResource();
        }
        return resource;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public URI objectAsURI(Resource subject, RdfSchema nameSpace, String localName) {
        URI uri = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if(node != null) {
            node.isURIResource();
            try {
                uri  = new URI(node.asResource().getURI());
            } catch (URISyntaxException e) {
                // this should actually never happen
                throw new RuntimeException(e);
            }
        }
        return uri;
    }

    /**
     * @param subject
     */
    private void printProperties(Resource subject) {
        for( StmtIterator it = subject.listProperties(); it.hasNext(); ) {
            System.err.println(it.next().toString());
        }
    }

    public Resource getFromUri(String uri) {

        final Model model = ModelFactory.createDefaultModel();
        model.read(uri);
        if(logger.isDebugEnabled()) {
            model.write(System.err);
        }
        return model.getResource(uri);

    }

    /**
     * @param matchedResourceURI
     * @return
     * @throws DRFChecklistException
     */
    public Resource getFromUri(URI matchedResourceURI) {
        return getFromUri(matchedResourceURI.toString());
    }

    /**
     * @return the rdfFile
     */
    public File getRdfFile() {
        return rdfFile;
    }


}
