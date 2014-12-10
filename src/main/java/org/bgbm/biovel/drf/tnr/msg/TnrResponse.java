//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.10 at 01:53:31 PM CET 
//


package org.bgbm.biovel.drf.tnr.msg;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{http://bgbm.org/biovel/drf/tnr/msg}acceptedName" minOccurs="0"/>
 *         &lt;element name="otherName" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="taxonName" type="{http://bgbm.org/biovel/drf/tnr/msg}taxonName"/>
 *                   &lt;element name="info" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="acceptedNameUrl" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="source" type="{http://bgbm.org/biovel/drf/tnr/msg}source"/>
 *                   &lt;element name="scrutiny" type="{http://bgbm.org/biovel/drf/tnr/msg}scrutiny"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="synonym" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="taxonName" type="{http://bgbm.org/biovel/drf/tnr/msg}taxonName"/>
 *                   &lt;element name="info" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="acceptedNameUrl" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="taxonomicStatus" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="source" type="{http://bgbm.org/biovel/drf/tnr/msg}source"/>
 *                   &lt;element name="scrutiny" type="{http://bgbm.org/biovel/drf/tnr/msg}scrutiny"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="checklist" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="checklist_url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="checklist_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="checklist_citation" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "acceptedName",
    "otherName",
    "synonym"
})
@XmlRootElement(name = "tnrResponse")
public class TnrResponse {

    protected AcceptedName acceptedName;
    protected List<TnrResponse.OtherName> otherName;
    protected List<TnrResponse.Synonym> synonym;
    @XmlAttribute(required = true)
    protected String checklist;
    @XmlAttribute(name = "checklist_url", required = true)
    protected String checklistUrl;
    @XmlAttribute(name = "checklist_version", required = true)
    protected String checklistVersion;
    @XmlAttribute(name = "checklist_citation", required = true)
    protected String checklistCitation;

    /**
     * Gets the value of the acceptedName property.
     * 
     * @return
     *     possible object is
     *     {@link AcceptedName }
     *     
     */
    public AcceptedName getAcceptedName() {
        return acceptedName;
    }

    /**
     * Sets the value of the acceptedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link AcceptedName }
     *     
     */
    public void setAcceptedName(AcceptedName value) {
        this.acceptedName = value;
    }

    /**
     * Gets the value of the otherName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TnrResponse.OtherName }
     * 
     * 
     */
    public List<TnrResponse.OtherName> getOtherName() {
        if (otherName == null) {
            otherName = new ArrayList<TnrResponse.OtherName>();
        }
        return this.otherName;
    }

    /**
     * Gets the value of the synonym property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the synonym property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSynonym().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TnrResponse.Synonym }
     * 
     * 
     */
    public List<TnrResponse.Synonym> getSynonym() {
        if (synonym == null) {
            synonym = new ArrayList<TnrResponse.Synonym>();
        }
        return this.synonym;
    }

    /**
     * Gets the value of the checklist property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecklist() {
        return checklist;
    }

    /**
     * Sets the value of the checklist property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecklist(String value) {
        this.checklist = value;
    }

    /**
     * Gets the value of the checklistUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecklistUrl() {
        return checklistUrl;
    }

    /**
     * Sets the value of the checklistUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecklistUrl(String value) {
        this.checklistUrl = value;
    }

    /**
     * Gets the value of the checklistVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecklistVersion() {
        return checklistVersion;
    }

    /**
     * Sets the value of the checklistVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecklistVersion(String value) {
        this.checklistVersion = value;
    }

    /**
     * Gets the value of the checklistCitation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecklistCitation() {
        return checklistCitation;
    }

    /**
     * Sets the value of the checklistCitation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecklistCitation(String value) {
        this.checklistCitation = value;
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
        "source",
        "scrutiny"
    })
    public static class OtherName {

        @XmlElement(required = true)
        protected TaxonName taxonName;
        protected TnrResponse.OtherName.Info info;
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
         *     {@link TnrResponse.OtherName.Info }
         *     
         */
        public TnrResponse.OtherName.Info getInfo() {
            return info;
        }

        /**
         * Sets the value of the info property.
         * 
         * @param value
         *     allowed object is
         *     {@link TnrResponse.OtherName.Info }
         *     
         */
        public void setInfo(TnrResponse.OtherName.Info value) {
            this.info = value;
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
            protected TnrResponse.OtherName.Info.AcceptedNameUrl acceptedNameUrl;

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
             *     {@link TnrResponse.OtherName.Info.AcceptedNameUrl }
             *     
             */
            public TnrResponse.OtherName.Info.AcceptedNameUrl getAcceptedNameUrl() {
                return acceptedNameUrl;
            }

            /**
             * Sets the value of the acceptedNameUrl property.
             * 
             * @param value
             *     allowed object is
             *     {@link TnrResponse.OtherName.Info.AcceptedNameUrl }
             *     
             */
            public void setAcceptedNameUrl(TnrResponse.OtherName.Info.AcceptedNameUrl value) {
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
    public static class Synonym {

        @XmlElement(required = true)
        protected TaxonName taxonName;
        protected TnrResponse.Synonym.Info info;
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
         *     {@link TnrResponse.Synonym.Info }
         *     
         */
        public TnrResponse.Synonym.Info getInfo() {
            return info;
        }

        /**
         * Sets the value of the info property.
         * 
         * @param value
         *     allowed object is
         *     {@link TnrResponse.Synonym.Info }
         *     
         */
        public void setInfo(TnrResponse.Synonym.Info value) {
            this.info = value;
        }

        /**
         * 
         *                         The taxonomic status string like  "invalid", "misapplied", "homotypic synonym", "accepted", "synonym".
         *                         See also http://rs.tdwg.org/dwc/terms/taxonomicStatus
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
            protected TnrResponse.Synonym.Info.AcceptedNameUrl acceptedNameUrl;

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
             *     {@link TnrResponse.Synonym.Info.AcceptedNameUrl }
             *     
             */
            public TnrResponse.Synonym.Info.AcceptedNameUrl getAcceptedNameUrl() {
                return acceptedNameUrl;
            }

            /**
             * Sets the value of the acceptedNameUrl property.
             * 
             * @param value
             *     allowed object is
             *     {@link TnrResponse.Synonym.Info.AcceptedNameUrl }
             *     
             */
            public void setAcceptedNameUrl(TnrResponse.Synonym.Info.AcceptedNameUrl value) {
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

}
