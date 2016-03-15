// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cybertaxonomy.utis.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Mar 15, 2016
 *
 */
public class VersionInfo {

    protected static final Logger logger = LoggerFactory.getLogger(VersionInfo.class);

    private static final String PROJECT_VERSION_KEY = "project.version";
    protected static String VERSION = "0.0-DEFAULT";
    private static final Pattern majorMinorPattern = Pattern.compile("^(\\d+\\.\\d+).*$");

    static {
            Properties versionProps = new Properties();
            InputStream propsResource = Store.class.getResourceAsStream("/version.properties");
            if(propsResource == null) {
                logger.error("No resource named 'version.properties' found, using default version '0.0-DEFAULT'");
            }
            try {
                versionProps.load(propsResource);
                VERSION = versionProps.get(PROJECT_VERSION_KEY).toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public static String majorMinorVersion() {
        Matcher matcher = majorMinorPattern.matcher(VERSION);
        if(matcher.matches() && matcher.groupCount() == 1) {
            return matcher.group(1);
        }
        return VERSION;
    }


}
