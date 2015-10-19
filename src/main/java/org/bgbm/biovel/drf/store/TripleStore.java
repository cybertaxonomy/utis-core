// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.bgbm.biovel.drf.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Oct 19, 2015
 *
 */
public class TripleStore {

    protected Logger logger = LoggerFactory.getLogger(TripleStore.class);

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static final File userHomeDir = new File(System.getProperty("user.home"));
    private static final File utisHome = new File(userHomeDir, ".utis");

    private File rdfFile = null;

    private Dataset dataset = null;


    public TripleStore() {

    }


    /**
     *
     * @param rdfFileUri
     *  the location of the file to load the rdf triples from
     * @throws IOException
     */
    public void loadIntoStore(String rdfFileUri) throws IOException {
        this.rdfFile = downloadAndExtract(rdfFileUri);
        createStore();
    }


    /**
     * WARNING!!! This needs at least 1.5GB of heap space!!!
     * set -Xmx1500M
     *
     * NOTE: The bulkloader is a faster way to load data into an empty dataset than just using the Jena update operations.
     * the bulkloader also requires less memory
     * It is accessed through the command line utility tdbloader.
     *
     * rm /tmp/drf_tnb_store/*; bin/tdbloader2 -l /tmp/drf_tnb_store /tmp/species.rdf
     * @throws IOException
     */
    private void createStore() throws IOException {

        boolean doClearStoreLocation = rdfFile != null;
        boolean doLoadRdfFile = rdfFile != null;

        File tdbStoreFolder = new File(utisHome, "tdb" + File.separator);
        if(tdbStoreFolder.exists()) {
            if( doClearStoreLocation ) {
                FileUtils.cleanDirectory(tdbStoreFolder);
            }
        } else {
            tdbStoreFolder.mkdirs();
        }
        Location location = Location.create(tdbStoreFolder.toString());

        Dataset dataset = TDBFactory.createDataset(location);

        logger.info("Using TDB store at " + location);

        dataset.begin(ReadWrite.READ) ;
        // Get model inside the transaction
        Model model = dataset.getDefaultModel() ;
        logger.info("Dataset in TDB has " + dataset.asDatasetGraph().size() + " named graphs");
        logger.info("Model-size: " + model.size());
        dataset.end();

        if(doLoadRdfFile) {
            dataset.begin(ReadWrite.WRITE);
            model = dataset.getDefaultModel();
            // parse InputStream as RDF in Turtle format
            InputStream fin = new FileInputStream(rdfFile);
            logger.info("loading RDF/XML into TDB store");
            model.read(fin, null, "RDF/XML");
            logger.info("loading RDF/XML done");
            logger.info("Dataset in TDB has " + dataset.asDatasetGraph().size() + " named graphs");
            logger.info("Model-size: " + model.size());
            dataset.commit();
            dataset.end();
            logger.info("rdf loaded into TDB store at " + tdbStoreFolder);
        }

        this.setDataset(dataset);
    }

    /**
     * @param rdfFileUri
    *
    */
   private File downloadAndExtract(String rdfFileUri) {
       File expandedFile = null;
       CloseableHttpClient httpClient = HttpClients.createDefault();
       CloseableHttpResponse response;
       try {
           // 1. download and store in local filesystem in TMP
           logger.debug("downloading rdf file from " + rdfFileUri);
           HttpGet httpGet = new HttpGet(rdfFileUri);
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
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }


    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

}
