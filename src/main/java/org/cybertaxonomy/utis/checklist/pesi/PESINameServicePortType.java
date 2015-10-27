/**
 * PESINameServicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.cybertaxonomy.utis.checklist.pesi;

public interface PESINameServicePortType extends java.rmi.Remote {

    /**
     * <strong>Get the first exact matching GUID for a given name.
     * </strong>
     */
    public java.lang.String getGUID(java.lang.String scientificname) throws java.rmi.RemoteException;

    /**
     * <strong>Get one or more matching (max. 50) PESIRecords for
     * a given name.<br/>Parameters:
     *    <ul>
     *     <li><u>like</u>: add a '%'-sign added after the ScientificName
     * (SQL LIKE function). Default=true.</li>
     *    </ul>
     *   </strong>
     */
    public PESIRecord[] getPESIRecords(java.lang.String scientificname, boolean like) throws java.rmi.RemoteException;

    /**
     * <strong>Get the correct name for a given GUID</strong>.
     */
    public java.lang.String getPESINameByGUID(java.lang.String GUID) throws java.rmi.RemoteException;

    /**
     * <strong>Get the complete PESI Record for a given GUID.</strong>
     */
    public PESIRecord getPESIRecordByGUID(java.lang.String GUID) throws java.rmi.RemoteException;

    /**
     * <strong>Get one or more PESI Records (max. 50) for a given
     * common name or vernacular.</strong>
     */
    public PESIRecord[] getPESIRecordsByVernacular(java.lang.String vernacular) throws java.rmi.RemoteException;

    /**
     * <strong>Get all vernaculars for a given GUID.</strong>
     */
    public Vernacular[] getPESIVernacularsByGUID(java.lang.String GUID) throws java.rmi.RemoteException;

    /**
     * <strong>Fuzzy matches one ScientificName to one or more (max.
     * 50) PESI Records.<br/>
     *   This function uses <a href="http://www.cmar.csiro.au/datacentre/taxamatch.htm"
     * target="_blank">Tony Rees' TAXAMATCH algorithm</a>
     *   </strong>
     */
    public PESIRecord[] matchTaxon(java.lang.String scientificname) throws java.rmi.RemoteException;

    /**
     * <strong>Fuzzy matches multiple ScientificNames (max. 50) to
     * one or more PESI Records.<br/>
     *    This function uses <a href="http://www.cmar.csiro.au/datacentre/taxamatch.htm"
     * target="_blank">Tony Rees' TAXAMATCH algorithm</a>
     *    </strong>
     */
    public PESIRecord[][] matchTaxa(java.lang.String[] scientificnames) throws java.rmi.RemoteException;

    /**
     * <strong>Get all synonyms for a given GUID.</strong>
     */
    public PESIRecord[] getPESISynonymsByGUID(java.lang.String GUID) throws java.rmi.RemoteException;
}
