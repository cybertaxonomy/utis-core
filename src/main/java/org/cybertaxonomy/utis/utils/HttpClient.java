/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.utils.IOUtils;
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
 * @date Nov 9, 2016
 *
 */
public class HttpClient {

    protected static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    protected static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    /**
     *
     */
    public HttpClient() {
        super();
    }

    /**
     * @param fileUri
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected File toTempFile(String fileUri) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        try {
            logger.debug("downloading rdf file from " + fileUri);
            HttpGet httpGet = new HttpGet(fileUri);
            response = httpClient.execute(httpGet);
            String archiveFileName = FilenameUtils.getName(httpGet.getURI().getRawPath());
            File archiveFile = new File(tmpDir, archiveFileName);
            FileOutputStream fout = new FileOutputStream(archiveFile);
            IOUtils.copy(response.getEntity().getContent(), new FileOutputStream(archiveFile));
            fout.close();
            logger.debug(archiveFile.length() + " bytes downloaded to " + archiveFile.getCanonicalPath());
            return archiveFile;
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                // IGNORE //
            }
        }

    }

}