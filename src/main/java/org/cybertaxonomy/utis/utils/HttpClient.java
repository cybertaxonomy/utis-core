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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
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

    private static final int readTimeout = 1000 * 10;

    private static final int connectTimeout = 1000 * 3;

    /**
     *
     */
    public HttpClient() {
        super();
    }

    /**
     * @param fileUri
     * @param prefix TODO
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    protected File toTempFile(String fileUri, String prefix) throws IOException, URISyntaxException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response;
        try {

            URI inURI = new URI(fileUri);
            String archiveFileName = FilenameUtils.getName(inURI.getRawPath());
            String existingPrefix = FilenameUtils.getPrefix(archiveFileName);
            if(prefix != null && (existingPrefix == null || !existingPrefix.equals(prefix))) {
                archiveFileName += '.' +  prefix;
            }
            File archiveFile = new File(tmpDir, archiveFileName);

            logger.debug("downloading rdf file from " + fileUri);

            long startTime = System.currentTimeMillis();

            URLConnection con = inURI.toURL().openConnection();
            con.setConnectTimeout(connectTimeout );
            con.setReadTimeout(readTimeout);
            InputStream in = con.getInputStream();


            FileOutputStream fout = new FileOutputStream(archiveFile);
            IOUtils.copy(in, new FileOutputStream(archiveFile));
            fout.close();
            double seconds = (double)(System.currentTimeMillis() - startTime) / 1000;

            logger.debug(archiveFile.length() + " bytes downloaded in " + seconds + " seconds to " + archiveFile.getCanonicalPath());
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