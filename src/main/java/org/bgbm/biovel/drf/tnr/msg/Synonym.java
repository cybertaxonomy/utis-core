//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.10 at 03:19:51 PM CET 
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
 *         &lt;element name="info" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="acceptedNameUrl" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "info",
    "taxonomicStatus",
    "source",
    "scrutiny"
})
@XmlRootElement(name = "synonym")
public class Synonym {

    @XmlElement(required = true)
    protected TaxonName taxonName;
    protected Synonym.Info info;
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
     * Gets the value of the info property.
     * 
     * @return
     *     possible object is
     *     {@link Synonym.Info }
     *     
     */
    public Synonym.Info getInfo() {
        return info;
    }

    /**
     * Sets the value of the info property.
     * 
     * @param value
     *     allowed object is
     *     {@link Synonym.Info }
     *     
     */
    public void setInfo(Synonym.Info value) {
        this.info = value;
    }

    /**
     * Gets the value of the taxonomicStatus property.
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
     *         &lt;element name="acceptedNameUrl" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "url",
        "acceptedNameUrl"
    })
    public static class Info {

        protected String url;
        protected Synonym.Info.AcceptedNameUrl acceptedNameUrl;

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

        /**
         * Gets the value of the acceptedNameUrl property.
         * 
         * @return
         *     possible object is
         *     {@link Synonym.Info.AcceptedNameUrl }
         *     
         */
        public Synonym.Info.AcceptedNameUrl getAcceptedNameUrl() {
            return acceptedNameUrl;
        }

        /**
         * Sets the value of the acceptedNameUrl property.
         * 
         * @param value
         *     allowed object is
         *     {@link Synonym.Info.AcceptedNameUrl }
         *     
         */
        public void setAcceptedNameUrl(Synonym.Info.AcceptedNameUrl value) {
            this.acceptedNameUrl = value;
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
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class AcceptedNameUrl {


        }

    }

}
