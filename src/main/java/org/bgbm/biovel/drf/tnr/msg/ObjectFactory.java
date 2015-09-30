//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.09.30 at 01:51:04 PM CEST 
//


package org.bgbm.biovel.drf.tnr.msg;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.bgbm.biovel.drf.tnr.msg package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.bgbm.biovel.drf.tnr.msg
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link Response }
     * 
     */
    public Response createResponse() {
        return new Response();
    }

    /**
     * Create an instance of {@link TnrMsg }
     * 
     */
    public TnrMsg createTnrMsg() {
        return new TnrMsg();
    }

    /**
     * Create an instance of {@link Query.Request }
     * 
     */
    public Query.Request createQueryRequest() {
        return new Query.Request();
    }

    /**
     * Create an instance of {@link Taxon }
     * 
     */
    public Taxon createTaxon() {
        return new Taxon();
    }

    /**
     * Create an instance of {@link TaxonBase }
     * 
     */
    public TaxonBase createTaxonBase() {
        return new TaxonBase();
    }

    /**
     * Create an instance of {@link TaxonName }
     * 
     */
    public TaxonName createTaxonName() {
        return new TaxonName();
    }

    /**
     * Create an instance of {@link Source }
     * 
     */
    public Source createSource() {
        return new Source();
    }

    /**
     * Create an instance of {@link Classification }
     * 
     */
    public Classification createClassification() {
        return new Classification();
    }

    /**
     * Create an instance of {@link Response.OtherNames }
     * 
     */
    public Response.OtherNames createResponseOtherNames() {
        return new Response.OtherNames();
    }

    /**
     * Create an instance of {@link Synonym }
     * 
     */
    public Synonym createSynonym() {
        return new Synonym();
    }

    /**
     * Create an instance of {@link Query.ClientStatus }
     * 
     */
    public Query.ClientStatus createQueryClientStatus() {
        return new Query.ClientStatus();
    }

    /**
     * Create an instance of {@link AtomisedName }
     * 
     */
    public AtomisedName createAtomisedName() {
        return new AtomisedName();
    }

}
