//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.06 at 04:54:40 PM CEST 
//


package org.bgbm.biovel.drf.tnr.msg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.wordnik.swagger.annotations.ApiModelProperty;


/**
 * <p>Java class for taxonName complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="taxonName"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fullName"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="canonicalName"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="atomisedName" type="{http://bgbm.org/biovel/drf/tnr/msg}atomisedName" minOccurs="0"/&gt;
 *         &lt;element name="authorship" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="rank" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="nomenclaturalReference" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taxonName", propOrder = {
    "fullName",
    "canonicalName",
    "atomisedName",
    "authorship",
    "rank",
    "nomenclaturalReference"
})
public class TaxonName {

    @XmlElement(required = true)
    protected String fullName;
    @XmlElement(required = true)
    protected String canonicalName;
    protected AtomisedName atomisedName;
    protected String authorship;
    protected String rank;
    @XmlElement(required = true)
    protected String nomenclaturalReference;

    /**
     * Gets the value of the fullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("The full scientific name, with authorship, publication date information and potentially further taxonomic information.")
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the value of the fullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullName(String value) {
        this.fullName = value;
    }

    /**
     * Gets the value of the canonicalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("Canonical name string consisting of only nomenclatural information, i.e. no authorship or taxonomic hierarchy information with the exception of the necessary placements within Genus or Species.")
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Sets the value of the canonicalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCanonicalName(String value) {
        this.canonicalName = value;
    }

    /**
     * Gets the value of the atomisedName property.
     * 
     * @return
     *     possible object is
     *     {@link AtomisedName }
     *     
     */
    public AtomisedName getAtomisedName() {
        return atomisedName;
    }

    /**
     * Sets the value of the atomisedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtomisedName }
     *     
     */
    public void setAtomisedName(AtomisedName value) {
        this.atomisedName = value;
    }

    /**
     * Gets the value of the authorship property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorship() {
        return authorship;
    }

    /**
     * Sets the value of the authorship property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorship(String value) {
        this.authorship = value;
    }

    /**
     * Gets the value of the rank property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRank() {
        return rank;
    }

    /**
     * Sets the value of the rank property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRank(String value) {
        this.rank = value;
    }

    /**
     * Gets the value of the nomenclaturalReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("A reference for the publication in which the scientificName was originally established under the rules of the associated nomenclaturalCode.")
    public String getNomenclaturalReference() {
        return nomenclaturalReference;
    }

    /**
     * Sets the value of the nomenclaturalReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomenclaturalReference(String value) {
        this.nomenclaturalReference = value;
    }

}
