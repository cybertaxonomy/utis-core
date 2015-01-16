//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.16 at 10:36:48 AM CET 
//


package org.bgbm.biovel.drf.tnr.msg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for atomisedName complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="atomisedName"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="uninomial"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="subGenus"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="genusPart"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="infragenericEpithet" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="specificEpithet" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="infraspecificEpithet" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "atomisedName", propOrder = {
    "uninomial",
    "subGenus"
})
public class AtomisedName {

    protected String uninomial;
    protected AtomisedName.SubGenus subGenus;

    /**
     * Gets the value of the uninomial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUninomial() {
        return uninomial;
    }

    /**
     * Sets the value of the uninomial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUninomial(String value) {
        this.uninomial = value;
    }

    /**
     * Gets the value of the subGenus property.
     * 
     * @return
     *     possible object is
     *     {@link AtomisedName.SubGenus }
     *     
     */
    public AtomisedName.SubGenus getSubGenus() {
        return subGenus;
    }

    /**
     * Sets the value of the subGenus property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtomisedName.SubGenus }
     *     
     */
    public void setSubGenus(AtomisedName.SubGenus value) {
        this.subGenus = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="genusPart"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="infragenericEpithet" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="specificEpithet" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="infraspecificEpithet" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "genusPart",
        "infragenericEpithet",
        "specificEpithet",
        "infraspecificEpithet"
    })
    public static class SubGenus {

        @XmlElement(required = true)
        protected String genusPart;
        protected String infragenericEpithet;
        protected String specificEpithet;
        protected String infraspecificEpithet;

        /**
         * Gets the value of the genusPart property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getGenusPart() {
            return genusPart;
        }

        /**
         * Sets the value of the genusPart property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setGenusPart(String value) {
            this.genusPart = value;
        }

        /**
         * Gets the value of the infragenericEpithet property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInfragenericEpithet() {
            return infragenericEpithet;
        }

        /**
         * Sets the value of the infragenericEpithet property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInfragenericEpithet(String value) {
            this.infragenericEpithet = value;
        }

        /**
         * Gets the value of the specificEpithet property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSpecificEpithet() {
            return specificEpithet;
        }

        /**
         * Sets the value of the specificEpithet property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSpecificEpithet(String value) {
            this.specificEpithet = value;
        }

        /**
         * Gets the value of the infraspecificEpithet property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getInfraspecificEpithet() {
            return infraspecificEpithet;
        }

        /**
         * Sets the value of the infraspecificEpithet property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInfraspecificEpithet(String value) {
            this.infraspecificEpithet = value;
        }

    }

}
