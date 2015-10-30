// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Oct 20, 2015
 *
 */
public abstract class Store {

    protected static final Logger logger = LoggerFactory.getLogger(Store.class);
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static final File userHomeDir = new File(System.getProperty("user.home"));
    protected static File utisHome = null;

    static {
        if(System.getProperty("utis.home") != null){
            utisHome = new File(System.getProperty("utis.home"));
            logger.info("utis.home defined by system property: " + utisHome.getAbsolutePath());
        } else {
            // this is the folder for production mode
            // /var/lib/utis must be created prior starting the application
            utisHome = new File("/var/lib/utis");
            logger.info("utis.home " + utisHome.getAbsolutePath());
            if(!utisHome.canWrite()) {
                logger.info("utis.home " + utisHome.getAbsolutePath() +  " is not writable, trying other location ...");
                // this is the folder for development mode
                // if the application is started inside a service as jetty
                // this folder will most probably not be writable.
                utisHome = new File(userHomeDir, ".utis");
            }
            logger.info("utis.home finally is " + utisHome.getAbsolutePath());
        }
    }
    protected File storeLocation = null;

    public Store() throws Exception {
        storeLocation = new File(utisHome, storeName() + File.separator);
        if( !storeLocation.exists()) {
            storeLocation.mkdirs();
            logger.debug("new store location created at " + storeLocation.getAbsolutePath());
        }
        initStoreEngine();
    }

    /**
     * @return
     */
    protected abstract String storeName();

    /**
     * @param rdfFileUri
    *
    */
    protected File downloadAndExtract(String rdfFileUri) {
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
     *
     * @param rdfFileUri
     *  the location of the file to load the rdf triples from
     * @throws Exception
     */
    public void loadIntoStore(String ... rdfFileUri) throws Exception {
        stopStoreEngine();
        clear();
        initStoreEngine();
        for (String uri : rdfFileUri) {
            File localF = downloadAndExtract(uri);
            load(localF);
        }
    }

    protected abstract void initStoreEngine() throws Exception;

    protected abstract void stopStoreEngine() throws Exception;

    protected abstract void load(File rdfFile) throws Exception;

    protected void clear() throws IOException {

        if(storeLocation.exists()) {
            FileUtils.cleanDirectory(storeLocation);
        } else {
            storeLocation.mkdirs();
        }
    }

}
