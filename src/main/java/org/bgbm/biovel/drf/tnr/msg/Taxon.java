//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.11 at 11:24:29 AM CET 
//


package org.bgbm.biovel.drf.tnr.msg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="taxonName" type="{http://bgbm.org/biovel/drf/tnr/msg}taxonName"/>
 *         &lt;element name="accordingTo" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="info" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://bgbm.org/biovel/drf/tnr/msg}classification" minOccurs="0"/>
 *         &lt;element name="taxonomicStatus" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="source" type="{http://bgbm.org/biovel/drf/tnr/msg}source"/>
 *         &lt;element name="scrutiny" type="{http://bgbm.org/biovel/drf/tnr/msg}scrutiny"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "taxonName",
    "accordingTo",
    "info",
    "classification",
    "taxonomicStatus",
    "source",
    "scrutiny"
})
@XmlRootElement(name = "taxon")
public class Taxon {

    @XmlElement(required = true)
    protected TaxonName taxonName;
    protected String accordingTo;
    protected Taxon.Info info;
    protected Classification classification;
    protected String taxonomicStatus;
    @XmlElement(required = true)
    protected Source source;
    @XmlElement(required = true)
    protected Scrutiny scrutiny;

    /**
     * Gets the value of the taxonName property.
     * 
     * @return
     *     possible object is
     *     {@link TaxonName }
     *     
     */
    public TaxonName getTaxonName() {
        return taxonName;
    }

    /**
     * Sets the value of the taxonName property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxonName }
     *     
     */
    public void setTaxonName(TaxonName value) {
        this.taxonName = value;
    }

    /**
     * 
     *                   The reference to the source in which the specific
     *                   taxon concept circumscription is defined or implied -
     *                   traditionally signified by the Latin "sensu" or "sec."
     *                   (from secundum, meaning "according to"). For taxa that
     *                   result from identifications, a reference to the keys,
     *                   monographs, experts and other sources should be given.
     * 
     *                   Corresponds to
     *                   http://rs.tdwg.org/dwc/terms/nameAccordingTo
     *                 
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccordingTo() {
        return accordingTo;
    }

    /**
     * Sets the value of the accordingTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccordingTo(String value) {
        this.accordingTo = value;
    }

    /**
     * Gets the value of the info property.
     * 
     * @return
     *     possible object is
     *     {@link Taxon.Info }
     *     
     */
    public Taxon.Info getInfo() {
        return info;
    }

    /**
     * Sets the value of the info property.
     * 
     * @param value
     *     allowed object is
     *     {@link Taxon.Info }
     *     
     */
    public void setInfo(Taxon.Info value) {
        this.info = value;
    }

    /**
     * Gets the value of the classification property.
     * 
     * @return
     *     possible object is
     *     {@link Classification }
     *     
     */
    public Classification getClassification() {
        return classification;
    }

    /**
     * Sets the value of the classification property.
     * 
     * @param value
     *     allowed object is
     *     {@link Classification }
     *     
     */
    public void setClassification(Classification value) {
        this.classification = value;
    }

    /**
     * 
     *                   The taxonomic status string like "invalid",
     *                   "misapplied", "homotypic synonym", "accepted",
     *                   "synonym".
     * 
     *                   Corresponds to
     *                   http://rs.tdwg.org/dwc/terms/taxonomicStatus
     *                 
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxonomicStatus() {
        return taxonomicStatus;
    }

    /**
     * Sets the value of the taxonomicStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxonomicStatus(String value) {
        this.taxonomicStatus = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setSource(Source value) {
        this.source = value;
    }

    /**
     * Gets the value of the scrutiny property.
     * 
     * @return
     *     possible object is
     *     {@link Scrutiny }
     *     
     */
    public Scrutiny getScrutiny() {
        return scrutiny;
    }

    /**
     * Sets the value of the scrutiny property.
     * 
     * @param value
     *     allowed object is
     *     {@link Scrutiny }
     *     
     */
    public void setScrutiny(Scrutiny value) {
        this.scrutiny = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "url"
    })
    public static class Info {

        protected String url;

        /**
         * Gets the value of the url property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the value of the url property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUrl(String value) {
            this.url = value;
        }

    }

}
