//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.12.16 at 01:51:32 PM CET
//


package org.bgbm.biovel.drf.tnr.msg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;

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
 *         &lt;element name="kingdom" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="phylum" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="class" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="order" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="family" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="genus" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;/restriction>
 *           &lt;/simpleType>
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
    "kingdom",
    "phylum",
    "clazz",
    "order",
    "family",
    "genus"
})
@XmlRootElement(name = "classification")
public class Classification {

    protected String kingdom;
    protected String phylum;
    @XmlElement(name = "class")
    protected String clazz;
    protected String order;
    protected String family;
    protected String genus;

    /**
     * Gets the value of the kingdom property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getKingdom() {
        return kingdom;
    }

    /**
     * Sets the value of the kingdom property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setKingdom(String value) {
        this.kingdom = value;
    }

    /**
     * Gets the value of the phylum property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhylum() {
        return phylum;
    }

    /**
     * Sets the value of the phylum property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhylum(String value) {
        this.phylum = value;
    }

    /**
     * Gets the value of the clazz property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @JsonProperty("class")
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the order property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOrder(String value) {
        this.order = value;
    }

    /**
     * Gets the value of the family property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFamily() {
        return family;
    }

    /**
     * Sets the value of the family property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFamily(String value) {
        this.family = value;
    }

    /**
     * Gets the value of the genus property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getGenus() {
        return genus;
    }

    /**
     * Sets the value of the genus property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setGenus(String value) {
        this.genus = value;
    }

}
