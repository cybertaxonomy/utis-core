/**
 * PESINameServicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.cybertaxonomy.utis.checklist.pesi;

public interface PESINameServicePortType extends java.rmi.Remote {

    /**
     * <strong>Get the first exact matching GUID for a given name</strong>
     */
    public java.lang.String getGUID(java.lang.String scientificname) throws java.rmi.RemoteException;

    /**
     * <strong>Get one or more matching (max. 50) PESIRecords for
     * a given name.</strong>
     * 		<br/>Parameters:
     * 		<ul>
     * 			<li><u>like</u>: add a '%'-sign added after the ScientificName
     * (SQL LIKE function). Default=true</li>
     * 			<li><u>offset</u>: Starting recordnumber, when retrieving next
     * chunk of (50) records. Default=1</li>
     * 		</ul>
     * 		<br />Fields of output rows: <ul><li>	<u><b>GUID</b></u> </li><li>
     * 	<u><b>url</b></u> </li><li>	<u><b>scientificname</b></u> </li><li>
     * 	<u><b>authority</b></u> </li><li>	<u><b>rank</b></u> </li><li>	<u><b>status</b></u>
     * </li><li>	<u><b>valid_guid</b></u> </li><li>	<u><b>valid_name</b></u>
     * </li><li>	<u><b>valid_authority</b></u> </li><li>	<u><b>kingdom</b></u>
     * </li><li>	<u><b>phylum</b></u> </li><li>	<u><b>class</b></u> </li><li>
     * 	<u><b>order</b></u> </li><li>	<u><b>family</b></u> </li><li>	<u><b>genus</b></u>
     * </li><li>	<u><b>citation</b></u> </li><li>	<u><b>match_type</b></u>
     * </li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.PESIRecord[] getPESIRecords(java.lang.String scientificname, boolean like, int offset) throws java.rmi.RemoteException;

    /**
     * <strong>Get the correct name for a given GUID</strong>.
     */
    public java.lang.String getPESINameByGUID(java.lang.String GUID) throws java.rmi.RemoteException;

    /**
     * <strong>Get the complete PESI Record for a given GUID</strong>
     * 		<br />Output fields: <ul><li>	<u><b>GUID</b></u> </li><li>	<u><b>url</b></u>
     * </li><li>	<u><b>scientificname</b></u> </li><li>	<u><b>authority</b></u>
     * </li><li>	<u><b>rank</b></u> </li><li>	<u><b>status</b></u> </li><li>
     * 	<u><b>valid_guid</b></u> </li><li>	<u><b>valid_name</b></u> </li><li>
     * 	<u><b>valid_authority</b></u> </li><li>	<u><b>kingdom</b></u> </li><li>
     * 	<u><b>phylum</b></u> </li><li>	<u><b>class</b></u> </li><li>	<u><b>order</b></u>
     * </li><li>	<u><b>family</b></u> </li><li>	<u><b>genus</b></u> </li><li>
     * 	<u><b>citation</b></u> </li><li>	<u><b>match_type</b></u> </li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.PESIRecord getPESIRecordByGUID(java.lang.String GUID) throws java.rmi.RemoteException;

    /**
     * <strong>Get one or more PESI Records (max. 50) for a given
     * common name or vernacular.</strong>
     * 		<br/>Parameters:
     * 		<ul>
     * 			<li><u>offset</u>: Starting recordnumber, when retrieving next
     * chunk of (50) records. Default=1</li>
     * 		</ul>
     * 		<br />Fields of output rows: <ul><li>	<u><b>GUID</b></u> </li><li>
     * 	<u><b>url</b></u> </li><li>	<u><b>scientificname</b></u> </li><li>
     * 	<u><b>authority</b></u> </li><li>	<u><b>rank</b></u> </li><li>	<u><b>status</b></u>
     * </li><li>	<u><b>valid_guid</b></u> </li><li>	<u><b>valid_name</b></u>
     * </li><li>	<u><b>valid_authority</b></u> </li><li>	<u><b>kingdom</b></u>
     * </li><li>	<u><b>phylum</b></u> </li><li>	<u><b>class</b></u> </li><li>
     * 	<u><b>order</b></u> </li><li>	<u><b>family</b></u> </li><li>	<u><b>genus</b></u>
     * </li><li>	<u><b>citation</b></u> </li><li>	<u><b>match_type</b></u>
     * </li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.PESIRecord[] getPESIRecordsByVernacular(java.lang.String vernacular, int offset) throws java.rmi.RemoteException;

    /**
     * <strong>Get all vernaculars for a given GUID (max. 50).</strong>
     * 		<br/>Parameters:
     * 		<ul>
     * 			<li><u>offset</u>: Starting recordnumber, when retrieving next
     * chunk of (50) records. Default=1</li>
     * 		</ul>
     * 		<br />Fields of output rows: <ul><li>	<u><b>vernacular</b></u> </li><li>
     * 	<u><b>language_code</b></u> </li><li>	<u><b>language</b></u> </li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.Vernacular[] getPESIVernacularsByGUID(java.lang.String GUID, int offset) throws java.rmi.RemoteException;

    /**
     * <strong>Fuzzy matches one ScientificName to one or more (max.
     * 50) PESI Records. This function uses <a href='http://www.cmar.csiro.au/datacentre/taxamatch.htm'
     * target='_blank'>Tony Rees' TAXAMATCH algorithm.</a></strong>
     * 		<br />Fields of output rows: <ul><li>	<u><b>GUID</b></u> </li><li>
     * 	<u><b>url</b></u> </li><li>	<u><b>scientificname</b></u> </li><li>
     * 	<u><b>authority</b></u> </li><li>	<u><b>rank</b></u> </li><li>	<u><b>status</b></u>
     * </li><li>	<u><b>valid_guid</b></u> </li><li>	<u><b>valid_name</b></u>
     * </li><li>	<u><b>valid_authority</b></u> </li><li>	<u><b>kingdom</b></u>
     * </li><li>	<u><b>phylum</b></u> </li><li>	<u><b>class</b></u> </li><li>
     * 	<u><b>order</b></u> </li><li>	<u><b>family</b></u> </li><li>	<u><b>genus</b></u>
     * </li><li>	<u><b>citation</b></u> </li><li>	<u><b>match_type</b></u>
     * </li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.PESIRecord[] matchTaxon(java.lang.String scientificname) throws java.rmi.RemoteException;

    /**
     * <strong>Fuzzy matches multiple ScientificNames (max. 50) to
     * one or more PESI Records. This function uses <a href='http://www.cmar.csiro.au/datacentre/taxamatch.htm'
     * target='&#95;blank'>Tony Rees' TAXAMATCH algorithm.</a></strong>
     * 		<br />Fields of inner output rows: <ul><li>	<u><b>GUID</b></u> </li><li>
     * 	<u><b>url</b></u> </li><li>	<u><b>scientificname</b></u> </li><li>
     * 	<u><b>authority</b></u> </li><li>	<u><b>rank</b></u> </li><li>	<u><b>status</b></u>
     * </li><li>	<u><b>valid_guid</b></u> </li><li>	<u><b>valid_name</b></u>
     * </li><li>	<u><b>valid_authority</b></u> </li><li>	<u><b>kingdom</b></u>
     * </li><li>	<u><b>phylum</b></u> </li><li>	<u><b>class</b></u> </li><li>
     * 	<u><b>order</b></u> </li><li>	<u><b>family</b></u> </li><li>	<u><b>genus</b></u>
     * </li><li>	<u><b>citation</b></u> </li><li>	<u><b>match_type</b></u>
     * </li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.PESIRecord[][] matchTaxa(java.lang.String[] scientificnames) throws java.rmi.RemoteException;

    /**
     * <strong>Get all synonyms for a given GUID (max. 50).</strong>
     * 		<br/>Parameters:
     * 		<ul>
     * 			<li><u>offset</u>: Starting recordnumber, when retrieving next
     * chunk of (50) records. Default=1</li>
     * 		</ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.PESIRecord[] getPESISynonymsByGUID(java.lang.String GUID, int offset) throws java.rmi.RemoteException;

    /**
     * <strong>Get all distributions for a given GUID. (max. 50)</strong>
     * 		<br/>Parameters:
     * 		<ul>
     * 			<li><u>offset</u>: Starting recordnumber, when retrieving next
     * chunk of (50) records. Default=1</li>
     * 		</ul>
     * 		<br />Fields of output rows: <ul><li>	<u><b>locality</b></u> : The
     * specific description of the place</li><li>	<u><b>occurrenceStatus</b></u>
     * : A statement about the presence or absence of a Taxon at a Location</li><li>
     * 	<u><b>TDWG_level4</b></u> : TDWG World Geographical Scheme, level
     * 4, see <a href='http://www.kew.org/science-research-data/kew-in-depth/gis/resources-and-publications/data/tdwg/index.htm'
     * target='&#95;blank'>http://www.kew.org/science-research-data/kew-in-depth/gis/resources-and-publications/data/tdwg/index.htm</a></li><li>
     * 	<u><b>locationID</b></u> : An identifier for the locality. Using
     * the Marine Regions Geographic IDentifier (MRGID), see <a href='http://www.marineregions.org/mrgid.php'
     * target='_blank'>http://www.marineregions.org/mrgid.php</a></li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.Distribution[] getPESIDistributionsByGUID(java.lang.String GUID, int offset) throws java.rmi.RemoteException;

    /**
     * <strong>Get all sources/references linked to a given GUID.</strong>
     * 		<br />Fields of output rows: <ul><li>	<u><b>bibliographicCitation</b></u>
     * : A text string referring to an un-parsed bibliographic citation (see:
     * <a href='http://purl.org/dc/terms/identifier' target='&#95;blank'>http://purl.org/dc/terms/identifier</a>)</li><li>
     * 	<u><b>type</b></u> : Used to assign a bibliographic reference to
     * list of taxonomic or nomenclatural categories (see: <a href='http://purl.org/dc/terms/type'
     * target='&#95;blank'>http://purl.org/dc/terms/type</a>)</li><li>	<u><b>date</b></u>
     * : Date/year of publication (see: <a href='http://purl.org/dc/terms/date'
     * target='&#95;blank'>http://purl.org/dc/terms/date</a>)</li><li>	<u><b>creator</b></u>
     * : The author or authors of the referenced work(see: <a href='http://purl.org/dc/terms/creator'
     * target='&#95;blank'>http://purl.org/dc/terms/creator</a>)</li><li>
     * 	<u><b>title</b></u> : Title of book or article (see: <a href='http://purl.org/dc/terms/title'
     * target='&#95;blank'>http://purl.org/dc/terms/title</a>)</li><li>	<u><b>identifier</b></u>
     * : DOI, URI, etc refering to the reference (see: <a href='http://purl.org/dc/terms/identifier'
     * target='&#95;blank'>http://purl.org/dc/terms/identifier</a>)</li></ul>
     */
    public org.cybertaxonomy.utis.checklist.pesi.Source[] getPESISourcesByGUID(java.lang.String GUID) throws java.rmi.RemoteException;
}
