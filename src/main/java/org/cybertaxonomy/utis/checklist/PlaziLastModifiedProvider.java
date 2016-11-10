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
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.util.DateUtil;
import org.cybertaxonomy.utis.store.LastModifiedProvider;
import org.cybertaxonomy.utis.utils.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Nov 9, 2016
 *
 */
public class PlaziLastModifiedProvider extends HttpClient implements LastModifiedProvider {

    protected Logger logger = LoggerFactory.getLogger(PlaziLastModifiedProvider.class);

    private UpdatableStoreInfo storeInfo;

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

    public PlaziLastModifiedProvider(UpdatableStoreInfo storeInfo) {

        this.storeInfo = storeInfo;
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

        File archiveFile;
        try {
            archiveFile = toTempFile(storeInfo.getTestUrl());
        } catch (IOException e) {
            throw new DRFChecklistException("Error fetching the lastModified Test file, this blocks updating the store", e);
        }

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
        return null;
    }

    protected Date parseRssLine(String line) throws ParseException{

        Matcher m = lastBuildDatePattern.matcher(line);
        if (m.find()) {
            return dateFormat.parse(m.group(1));
        }
        return null;
    }

}
