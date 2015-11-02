//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.02 at 02:20:04 PM CET 
//


package org.cybertaxonomy.utis.tnr.msg;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="request"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="queryString" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="searchMode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="addSynonymy" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://cybertaxonomy.org/utis/tnr/msg}Response" maxOccurs="unbounded"/&gt;
 *         &lt;element name="clientStatus" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="checklistId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="duration" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *                   &lt;element name="statusMessage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
    "request",
    "response",
    "clientStatus"
})
@XmlRootElement(name = "query")
public class Query {

    @XmlElement(required = true)
    protected Query.Request request;
    @XmlElement(name = "Response", required = true)
    protected List<Response> response;
    protected List<Query.ClientStatus> clientStatus;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link Query.Request }
     *     
     */
    public Query.Request getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link Query.Request }
     *     
     */
    public void setRequest(Query.Request value) {
        this.request = value;
    }

    /**
     * Gets the value of the response property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the response property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResponse().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Response }
     * 
     * 
     */
    public List<Response> getResponse() {
        if (response == null) {
            response = new ArrayList<Response>();
        }
        return this.response;
    }

    /**
     * Gets the value of the clientStatus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clientStatus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClientStatus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Query.ClientStatus }
     * 
     * 
     */
    public List<Query.ClientStatus> getClientStatus() {
        if (clientStatus == null) {
            clientStatus = new ArrayList<Query.ClientStatus>();
        }
        return this.clientStatus;
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
     *         &lt;element name="checklistId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="duration" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
     *         &lt;element name="statusMessage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
        "checklistId",
        "duration",
        "statusMessage"
    })
    public static class ClientStatus {

        @XmlElement(required = true)
        protected String checklistId;
        @XmlElement(required = true)
        protected BigDecimal duration;
        @XmlElement(required = true)
        protected String statusMessage;

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
         * Gets the value of the duration property.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        @ApiModelProperty("Duration of the request processing in the specific checklist client in milliseconds.")
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
         * Gets the value of the statusMessage property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @ApiModelProperty("Status of the request, possible values are 'ok', 'timeout', 'interrupted', 'unsupported search mode'.")
        public String getStatusMessage() {
            return statusMessage;
        }

        /**
         * Sets the value of the statusMessage property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStatusMessage(String value) {
            this.statusMessage = value;
        }

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
     *         &lt;element name="queryString" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="searchMode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="addSynonymy" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
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
        "queryString",
        "searchMode",
        "addSynonymy"
    })
    public static class Request {

        @XmlElement(required = true)
        protected String queryString;
        @XmlElement(required = true)
        protected String searchMode;
        protected boolean addSynonymy;

        /**
         * Gets the value of the queryString property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @ApiModelProperty("The query string to match a scientific name, vernacular name or identifier depending on the searchMode")
        public String getQueryString() {
            return queryString;
        }

        /**
         * Sets the value of the queryString property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setQueryString(String value) {
            this.queryString = value;
        }

        /**
         * Gets the value of the searchMode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @ApiModelProperty("Specified the search mode to be usedPossible search modes are: scientificNameExact, scientificNameLike (begins with), vernacularNameExact, vernacularNameLike (contains).")
        public String getSearchMode() {
            return searchMode;
        }

        /**
         * Sets the value of the searchMode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSearchMode(String value) {
            this.searchMode = value;
        }

        /**
         * Gets the value of the addSynonymy property.
         * 
         */
        @ApiModelProperty("Indicates whether the synonymy of the accepted taxon should be included into the response. Turning this option on may lead to an increased response time.")
        public boolean isAddSynonymy() {
            return addSynonymy;
        }

        /**
         * Sets the value of the addSynonymy property.
         * 
         */
        public void setAddSynonymy(boolean value) {
            this.addSynonymy = value;
        }

    }

}
