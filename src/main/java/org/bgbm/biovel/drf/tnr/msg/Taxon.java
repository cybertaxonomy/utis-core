//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.01.15 at 06:33:17 PM CET 
//


package org.bgbm.biovel.drf.tnr.msg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.wordnik.swagger.annotations.ApiModelProperty;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://bgbm.org/biovel/drf/tnr/msg}taxonBase"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="accordingTo" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://bgbm.org/biovel/drf/tnr/msg}classification" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accordingTo",
    "classification"
})
@XmlRootElement(name = "taxon")
public class Taxon
    extends TaxonBase
{

    protected String accordingTo;
    protected Classification classification;

    /**
     * Gets the value of the accordingTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @ApiModelProperty("The reference to the source in which the specific taxon concept circumscription is defined or implied - traditionally signified by the Latin 'sensu' or 'sec.' (from secundum, meaning 'according to'). For taxa that result from identifications, a reference to the keys, monographs, experts and other sources should be given. Corresponds to http://rs.tdwg.org/dwc/terms/nameAccordingTo")
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

}
