/**
 * AphiaNameService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.bgbm.biovel.drf.checklist.worms;

public interface AphiaNameService extends javax.xml.rpc.Service {

/**
 * The data is licensed under a Creative Commons 'BY' 3.0 License,
 * see http://creativecommons.org/licenses/by/3.0/deed.en. For more information,
 * please visit http://www.marinespecies.org/aphia.php?p=webservice.
 */
    public java.lang.String getAphiaNameServicePortAddress();

    public org.bgbm.biovel.drf.checklist.worms.AphiaNameServicePortType getAphiaNameServicePort() throws javax.xml.rpc.ServiceException;

    public org.bgbm.biovel.drf.checklist.worms.AphiaNameServicePortType getAphiaNameServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
