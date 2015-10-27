package org.cybertaxonomy.utis.utils;

import java.net.MalformedURLException;
import java.util.UUID;

import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.client.LSIDAuthority;
import com.sun.jndi.toolkit.url.Uri;

public class IdentifierUtils {

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

    @SuppressWarnings("unused")
    public static boolean checkLSID(String value){

        try {
           new LSIDAuthority(value);
        } catch (MalformedLSIDException e) {
            return false;
        }
        return true;
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
