//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.29 at 10:17:55 AM CEST 
//


package org.bgbm.biovel.drf.tnr.msg;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.wordnik.swagger.annotations.ApiModelProperty;


/**
 * <p>Java class for taxonBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="taxonBase"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="taxonName" type="{http://bgbm.org/biovel/drf/tnr/msg}taxonName"/&gt;
 *         &lt;element name="accordingTo" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *         &lt;element name="taxonomicStatus" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
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
@XmlType(name = "taxonBase", propOrder = {
    "taxonName",
    "accordingTo",
    "url",
    "taxonomicStatus",
    "sources"
})
@XmlSeeAlso({
    Synonym.class,
    Taxon.class
})
public class TaxonBase {

    @XmlElement(required = true)
    protected TaxonName taxonName;
    protected String accordingTo;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String url;
    protected String taxonomicStatus;
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
     * Gets the value of the accordingTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("The reference to the source in which the specific taxon concept circumscription is defined or implied - traditionally signified by the Latin 'sensu' or 'sec.' (from secundum, meaning 'according to').")
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
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("The URL pointing to the original record of the checklist provider.")
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
     * Gets the value of the taxonomicStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("The taxonomic status string like 'invalid', 'misapplied', 'homotypic synonym', 'accepted', 'synonym'. Corresponds to http://rs.tdwg.org/dwc/terms/taxonomicStatus")
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
