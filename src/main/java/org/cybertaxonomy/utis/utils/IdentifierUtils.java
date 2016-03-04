package org.cybertaxonomy.utis.utils;

import java.net.MalformedURLException;
import java.util.UUID;
import java.util.regex.Pattern;

import com.sun.jndi.toolkit.url.Uri;

public class IdentifierUtils {

    final static Pattern LSID_PATTERN = Pattern.compile("^urn:lsid(:[\\w\\.-]*){3,4}$");

    public static boolean checkInteger(String value){

        try {
            Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean checkUUID(String value){

        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the given <code>value</code> confirms to {@code urn:lsid:<Authority>:<Namespace>:<ObjectID>[:<Version>]}
     * @param value
     * @return
     */
    public static boolean checkLSID(String value){

        return LSID_PATTERN.matcher(value).matches();
    }

    /**
     * @param id
     * @param idBaseUri
     * @return
     */
    @SuppressWarnings("unused")
    public static boolean checkURI(String id) {
        try {
            new Uri(id);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

}
