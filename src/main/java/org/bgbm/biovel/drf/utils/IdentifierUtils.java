package org.bgbm.biovel.drf.utils;

import java.util.UUID;

import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.client.LSIDAuthority;

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

}
