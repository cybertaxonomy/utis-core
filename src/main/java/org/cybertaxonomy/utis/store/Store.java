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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.utils.HttpClient;
import org.cybertaxonomy.utis.utils.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Oct 20, 2015
 *
 */
public abstract class Store extends HttpClient {

    protected static final Logger logger = LoggerFactory.getLogger(Store.class);

    private static final File userHomeDir = new File(System.getProperty("user.home"));

    protected static File utisHome = null;

    private String storeName;


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

    private StoreImportThread importThread;

    public Store(String storeName) throws Exception {
        this.storeName = storeName;
        storeLocation = new File(utisHome, VersionInfo.majorMinorVersion() + File.separator + storeType() + File.separator + storeName() + File.separator);
        if( !storeLocation.exists()) {
            storeLocation.mkdirs();
            logger.debug("new store location created at " + storeLocation.getAbsolutePath());
        }
        initStoreEngine();
    }

    /**
     *
     */
    protected String storeName() {
        return storeName;
    }

    protected abstract String storeType();


    /**
     * @param rdfFileUri
     * @throws URISyntaxException
     * @throws DRFChecklistException
    *
    */
    protected File downloadAndExtract(URI rdfFileUri) throws IOException, URISyntaxException {

           File dataFile;

           // 1. download and store in local filesystem in TMP
           dataFile = toTempFile(rdfFileUri.toString());

           // 2. extract the archive if needed
           if(dataFile != null){
               File expandedFile = null;
               FileInputStream fin = new FileInputStream(dataFile);
               InputStream ain = null;

               if(GzipUtils.isCompressedFilename(dataFile.getName())) {
                   logger.debug("Extracting GZIP file " + dataFile.getCanonicalPath());
                   ain = new GzipCompressorInputStream(fin);
               } else {
                   // TO UNZIP
                   //ArchiveInputStream ain = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, fin);
               }

               if(ain != null){
                   expandedFile = new File(tmpDir, GzipUtils.getUncompressedFilename(dataFile.getName()));
                   FileOutputStream fout = new FileOutputStream(expandedFile);
                   IOUtils.copy(ain, fout);
                   fout.close();
                   fin.close();
                   logger.debug("Extracted to " + expandedFile.getCanonicalPath());
                   dataFile = expandedFile;
               }
           }

           //check datafile extension
           if(!FilenameUtils.isExtension(dataFile.getName(), dataFileExtension())) {
               String newFileName = dataFile.getAbsolutePath() + "." + dataFileExtension();
               File newFile = new File(newFileName);
               if(dataFile.renameTo(newFile)) {
                   dataFile = newFile;
               } else {
                   logger.error("Cannot rename datafile to " + newFileName);
               }
           }

           return dataFile;
       }

    /**
     * @return
     */
    protected abstract String dataFileExtension();

    /**
     *
     * @param clearStore TODO
     * @param rdfFileUri
     *  the location of the file to load the rdf triples from
     * @throws Exception
     */
    public void loadIntoStore(List<URI> rdfFileUris, boolean clearStore) throws Exception {

        if(hasActiveImport()){
            logger.error("Cannot start second import while another is still running");
            return;
        }

        stopStoreEngine();
        if(clearStore){
            clear();
        }
        initStoreEngine();

        importThread = new StoreImportThread(this, rdfFileUris);
        importThread.setPriority(Thread.MIN_PRIORITY);
        importThread.setName(storeName + " import");
        importThread.start();
    }

    public void importFinished(){
        // release importThread
        importThread = null;
    }

    public boolean hasActiveImport(){
        return importThread != null;
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
