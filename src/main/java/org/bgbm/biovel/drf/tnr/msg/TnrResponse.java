//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.13 at 04:24:16 PM CET 
//


package org.bgbm.biovel.drf.tnr.msg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;


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
 *         &lt;element ref="{http://bgbm.org/biovel/drf/tnr/msg}taxon" minOccurs="0"/&gt;
 *         &lt;element name="otherNames" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="taxonName" type="{http://bgbm.org/biovel/drf/tnr/msg}taxonName"/&gt;
 *                   &lt;element name="info" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                             &lt;element name="acceptedNameUrl" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;sequence&gt;
 *                                     &lt;/sequence&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="source" type="{http://bgbm.org/biovel/drf/tnr/msg}source"/&gt;
 *                   &lt;element name="scrutiny" type="{http://bgbm.org/biovel/drf/tnr/msg}scrutiny"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://bgbm.org/biovel/drf/tnr/msg}synonym" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="checklist" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_citation" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="status" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "taxon",
    "otherNames",
    "synonym"
})
@XmlRootElement(name = "tnrResponse")
public class TnrResponse {

    protected Taxon taxon;
    protected List<TnrResponse.OtherNames> otherNames;
    protected List<Synonym> synonym;
    @XmlAttribute(name = "checklist", required = true)
    protected String checklist;
    @XmlAttribute(name = "checklist_url", required = true)
    protected String checklistUrl;
    @XmlAttribute(name = "checklist_version", required = true)
    protected String checklistVersion;
    @XmlAttribute(name = "checklist_citation", required = true)
    protected String checklistCitation;
    @XmlAttribute(name = "status", required = true)
    protected String status;
    @XmlAttribute(name = "duration", required = true)
    protected BigDecimal duration;

    /**
     * Gets the value of the taxon property.
     * 
     * @return
     *     possible object is
     *     {@link Taxon }
     *     
     */
    public Taxon getTaxon() {
        return taxon;
    }

    /**
     * Sets the value of the taxon property.
     * 
     * @param value
     *     allowed object is
     *     {@link Taxon }
     *     
     */
    public void setTaxon(Taxon value) {
        this.taxon = value;
    }

    /**
     * Gets the value of the otherNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TnrResponse.OtherNames }
     * 
     * 
     */
    public List<TnrResponse.OtherNames> getOtherNames() {
        if (otherNames == null) {
            otherNames = new ArrayList<TnrResponse.OtherNames>();
        }
        return this.otherNames;
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
     * {@link Synonym }
     * 
     * 
     */
    @JsonProperty("synonyms")
    @ApiModelProperty("The list synonyms related to the accepted taxon")
    public List<Synonym> getSynonym() {
        if (synonym == null) {
            synonym = new ArrayList<Synonym>();
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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("Status of the request, possible values are 'ok', 'timeout', 'interrupted', 'unsupported search mode'.")
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    @ApiModelProperty("Duration of the request in milliseconds")
    public BigDecimal getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setDuration(BigDecimal value) {
        this.duration = value;
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
     *         &lt;element name="taxonName" type="{http://bgbm.org/biovel/drf/tnr/msg}taxonName"/&gt;
     *         &lt;element name="info" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *                   &lt;element name="acceptedNameUrl" minOccurs="0"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;sequence&gt;
     *                           &lt;/sequence&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="source" type="{http://bgbm.org/biovel/drf/tnr/msg}source"/&gt;
     *         &lt;element name="scrutiny" type="{http://bgbm.org/biovel/drf/tnr/msg}scrutiny"/&gt;
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
        "taxonName",
        "info",
        "source",
        "scrutiny"
    })
    public static class OtherNames {

        @XmlElement(required = true)
        protected TaxonName taxonName;
        protected TnrResponse.OtherNames.Info info;
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
         *     {@link TnrResponse.OtherNames.Info }
         *     
         */
        public TnrResponse.OtherNames.Info getInfo() {
            return info;
        }

        /**
         * Sets the value of the info property.
         * 
         * @param value
         *     allowed object is
         *     {@link TnrResponse.OtherNames.Info }
         *     
         */
        public void setInfo(TnrResponse.OtherNames.Info value) {
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
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
         *         &lt;element name="acceptedNameUrl" minOccurs="0"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;sequence&gt;
         *                 &lt;/sequence&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
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
            "url",
            "acceptedNameUrl"
        })
        public static class Info {

            protected String url;
            protected TnrResponse.OtherNames.Info.AcceptedNameUrl acceptedNameUrl;

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
             *     {@link TnrResponse.OtherNames.Info.AcceptedNameUrl }
             *     
             */
            public TnrResponse.OtherNames.Info.AcceptedNameUrl getAcceptedNameUrl() {
                return acceptedNameUrl;
            }

            /**
             * Sets the value of the acceptedNameUrl property.
             * 
             * @param value
             *     allowed object is
             *     {@link TnrResponse.OtherNames.Info.AcceptedNameUrl }
             *     
             */
            public void setAcceptedNameUrl(TnrResponse.OtherNames.Info.AcceptedNameUrl value) {
                this.acceptedNameUrl = value;
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
             *       &lt;/sequence&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
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
