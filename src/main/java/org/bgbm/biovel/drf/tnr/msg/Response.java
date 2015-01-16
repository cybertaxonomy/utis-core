//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.16 at 03:48:06 PM CET 
//


package org.bgbm.biovel.drf.tnr.msg;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *                   &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *                   &lt;element name="sources" type="{http://bgbm.org/biovel/drf/tnr/msg}source" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://bgbm.org/biovel/drf/tnr/msg}synonym" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="vernacularNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="checklist" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checklist_citation" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="matchingNameString" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="matchingNameType" type="{http://bgbm.org/biovel/drf/tnr/msg}nameType" /&gt;
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
    "synonym",
    "vernacularNames"
})
@XmlRootElement(name = "Response")
public class Response {

    protected Taxon taxon;
    protected List<Response.OtherNames> otherNames;
    protected List<Synonym> synonym;
    protected List<String> vernacularNames;
    @XmlAttribute(name = "checklist", required = true)
    protected String checklist;
    @XmlAttribute(name = "checklist_id", required = true)
    protected String checklistId;
    @XmlAttribute(name = "checklist_url", required = true)
    protected String checklistUrl;
    @XmlAttribute(name = "checklist_version", required = true)
    protected String checklistVersion;
    @XmlAttribute(name = "checklist_citation", required = true)
    protected String checklistCitation;
    @XmlAttribute(name = "matchingNameString")
    protected String matchingNameString;
    @XmlAttribute(name = "matchingNameType")
    protected NameType matchingNameType;

    /**
     * Gets the value of the taxon property.
     * 
     * @return
     *     possible object is
     *     {@link Taxon }
     *     
     */
    @ApiModelProperty("The accepted taxon")
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
     * {@link Response.OtherNames }
     * 
     * 
     */
    public List<Response.OtherNames> getOtherNames() {
        if (otherNames == null) {
            otherNames = new ArrayList<Response.OtherNames>();
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
     * Gets the value of the vernacularNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vernacularNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVernacularNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    @ApiModelProperty("A common or vernacular name.")
    public List<String> getVernacularNames() {
        if (vernacularNames == null) {
            vernacularNames = new ArrayList<String>();
        }
        return this.vernacularNames;
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
     * Gets the value of the checklistId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecklistId() {
        return checklistId;
    }

    /**
     * Sets the value of the checklistId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecklistId(String value) {
        this.checklistId = value;
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
     * Gets the value of the matchingNameString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("Refers to the name string of the accepted taxon, synonym or otherName which was matching the query string")
    public String getMatchingNameString() {
        return matchingNameString;
    }

    /**
     * Sets the value of the matchingNameString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMatchingNameString(String value) {
        this.matchingNameString = value;
    }

    /**
     * Gets the value of the matchingNameType property.
     * 
     * @return
     *     possible object is
     *     {@link NameType }
     *     
     */
    @ApiModelProperty("Reports which of the names was matching the query string:  'taxon', 'synonym', 'vernacularName', or 'otherName'")
    public NameType getMatchingNameType() {
        return matchingNameType;
    }

    /**
     * Sets the value of the matchingNameType property.
     * 
     * @param value
     *     allowed object is
     *     {@link NameType }
     *     
     */
    public void setMatchingNameType(NameType value) {
        this.matchingNameType = value;
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
     *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
     *         &lt;element name="sources" type="{http://bgbm.org/biovel/drf/tnr/msg}source" maxOccurs="unbounded" minOccurs="0"/&gt;
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
        "url",
        "sources"
    })
    public static class OtherNames {

        @XmlElement(required = true)
        protected TaxonName taxonName;
        @XmlElement(required = true)
        @XmlSchemaType(name = "anyURI")
        protected String url;
        protected List<Source> sources;

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
         * Gets the value of the url property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @ApiModelProperty("The URL pointing to the original name record of the checklist provider.")
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
         * Gets the value of the sources property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sources property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSources().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Source }
         * 
         * 
         */
        public List<Source> getSources() {
            if (sources == null) {
                sources = new ArrayList<Source>();
            }
            return this.sources;
        }

    }

}
