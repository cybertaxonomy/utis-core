/**
 * PESIRecord.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.cybertaxonomy.utis.checklist.pesi;

public class PESIRecord  implements java.io.Serializable {
    private java.lang.String GUID;

    private java.lang.String url;

    private java.lang.String scientificname;

    private java.lang.String authority;

    private java.lang.String rank;

    private java.lang.String status;

    private java.lang.String valid_guid;

    private java.lang.String valid_name;

    private java.lang.String valid_authority;

    private java.lang.String kingdom;

    private java.lang.String phylum;

    private java.lang.String _class;

    private java.lang.String order;

    private java.lang.String family;

    private java.lang.String genus;

    private java.lang.String citation;

    private java.lang.String match_type;

    public PESIRecord() {
    }

    public PESIRecord(
           java.lang.String GUID,
           java.lang.String url,
           java.lang.String scientificname,
           java.lang.String authority,
           java.lang.String rank,
           java.lang.String status,
           java.lang.String valid_guid,
           java.lang.String valid_name,
           java.lang.String valid_authority,
           java.lang.String kingdom,
           java.lang.String phylum,
           java.lang.String _class,
           java.lang.String order,
           java.lang.String family,
           java.lang.String genus,
           java.lang.String citation,
           java.lang.String match_type) {
           this.GUID = GUID;
           this.url = url;
           this.scientificname = scientificname;
           this.authority = authority;
           this.rank = rank;
           this.status = status;
           this.valid_guid = valid_guid;
           this.valid_name = valid_name;
           this.valid_authority = valid_authority;
           this.kingdom = kingdom;
           this.phylum = phylum;
           this._class = _class;
           this.order = order;
           this.family = family;
           this.genus = genus;
           this.citation = citation;
           this.match_type = match_type;
    }


    /**
     * Gets the GUID value for this PESIRecord.
     * 
     * @return GUID
     */
    public java.lang.String getGUID() {
        return GUID;
    }


    /**
     * Sets the GUID value for this PESIRecord.
     * 
     * @param GUID
     */
    public void setGUID(java.lang.String GUID) {
        this.GUID = GUID;
    }


    /**
     * Gets the url value for this PESIRecord.
     * 
     * @return url
     */
    public java.lang.String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this PESIRecord.
     * 
     * @param url
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }


    /**
     * Gets the scientificname value for this PESIRecord.
     * 
     * @return scientificname
     */
    public java.lang.String getScientificname() {
        return scientificname;
    }


    /**
     * Sets the scientificname value for this PESIRecord.
     * 
     * @param scientificname
     */
    public void setScientificname(java.lang.String scientificname) {
        this.scientificname = scientificname;
    }


    /**
     * Gets the authority value for this PESIRecord.
     * 
     * @return authority
     */
    public java.lang.String getAuthority() {
        return authority;
    }


    /**
     * Sets the authority value for this PESIRecord.
     * 
     * @param authority
     */
    public void setAuthority(java.lang.String authority) {
        this.authority = authority;
    }


    /**
     * Gets the rank value for this PESIRecord.
     * 
     * @return rank
     */
    public java.lang.String getRank() {
        return rank;
    }


    /**
     * Sets the rank value for this PESIRecord.
     * 
     * @param rank
     */
    public void setRank(java.lang.String rank) {
        this.rank = rank;
    }


    /**
     * Gets the status value for this PESIRecord.
     * 
     * @return status
     */
    public java.lang.String getStatus() {
        return status;
    }


    /**
     * Sets the status value for this PESIRecord.
     * 
     * @param status
     */
    public void setStatus(java.lang.String status) {
        this.status = status;
    }


    /**
     * Gets the valid_guid value for this PESIRecord.
     * 
     * @return valid_guid
     */
    public java.lang.String getValid_guid() {
        return valid_guid;
    }


    /**
     * Sets the valid_guid value for this PESIRecord.
     * 
     * @param valid_guid
     */
    public void setValid_guid(java.lang.String valid_guid) {
        this.valid_guid = valid_guid;
    }


    /**
     * Gets the valid_name value for this PESIRecord.
     * 
     * @return valid_name
     */
    public java.lang.String getValid_name() {
        return valid_name;
    }


    /**
     * Sets the valid_name value for this PESIRecord.
     * 
     * @param valid_name
     */
    public void setValid_name(java.lang.String valid_name) {
        this.valid_name = valid_name;
    }


    /**
     * Gets the valid_authority value for this PESIRecord.
     * 
     * @return valid_authority
     */
    public java.lang.String getValid_authority() {
        return valid_authority;
    }


    /**
     * Sets the valid_authority value for this PESIRecord.
     * 
     * @param valid_authority
     */
    public void setValid_authority(java.lang.String valid_authority) {
        this.valid_authority = valid_authority;
    }


    /**
     * Gets the kingdom value for this PESIRecord.
     * 
     * @return kingdom
     */
    public java.lang.String getKingdom() {
        return kingdom;
    }


    /**
     * Sets the kingdom value for this PESIRecord.
     * 
     * @param kingdom
     */
    public void setKingdom(java.lang.String kingdom) {
        this.kingdom = kingdom;
    }


    /**
     * Gets the phylum value for this PESIRecord.
     * 
     * @return phylum
     */
    public java.lang.String getPhylum() {
        return phylum;
    }


    /**
     * Sets the phylum value for this PESIRecord.
     * 
     * @param phylum
     */
    public void setPhylum(java.lang.String phylum) {
        this.phylum = phylum;
    }


    /**
     * Gets the _class value for this PESIRecord.
     * 
     * @return _class
     */
    public java.lang.String get_class() {
        return _class;
    }


    /**
     * Sets the _class value for this PESIRecord.
     * 
     * @param _class
     */
    public void set_class(java.lang.String _class) {
        this._class = _class;
    }


    /**
     * Gets the order value for this PESIRecord.
     * 
     * @return order
     */
    public java.lang.String getOrder() {
        return order;
    }


    /**
     * Sets the order value for this PESIRecord.
     * 
     * @param order
     */
    public void setOrder(java.lang.String order) {
        this.order = order;
    }


    /**
     * Gets the family value for this PESIRecord.
     * 
     * @return family
     */
    public java.lang.String getFamily() {
        return family;
    }


    /**
     * Sets the family value for this PESIRecord.
     * 
     * @param family
     */
    public void setFamily(java.lang.String family) {
        this.family = family;
    }


    /**
     * Gets the genus value for this PESIRecord.
     * 
     * @return genus
     */
    public java.lang.String getGenus() {
        return genus;
    }


    /**
     * Sets the genus value for this PESIRecord.
     * 
     * @param genus
     */
    public void setGenus(java.lang.String genus) {
        this.genus = genus;
    }


    /**
     * Gets the citation value for this PESIRecord.
     * 
     * @return citation
     */
    public java.lang.String getCitation() {
        return citation;
    }


    /**
     * Sets the citation value for this PESIRecord.
     * 
     * @param citation
     */
    public void setCitation(java.lang.String citation) {
        this.citation = citation;
    }


    /**
     * Gets the match_type value for this PESIRecord.
     * 
     * @return match_type
     */
    public java.lang.String getMatch_type() {
        return match_type;
    }


    /**
     * Sets the match_type value for this PESIRecord.
     * 
     * @param match_type
     */
    public void setMatch_type(java.lang.String match_type) {
        this.match_type = match_type;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PESIRecord)) return false;
        PESIRecord other = (PESIRecord) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.GUID==null && other.getGUID()==null) || 
             (this.GUID!=null &&
              this.GUID.equals(other.getGUID()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            ((this.scientificname==null && other.getScientificname()==null) || 
             (this.scientificname!=null &&
              this.scientificname.equals(other.getScientificname()))) &&
            ((this.authority==null && other.getAuthority()==null) || 
             (this.authority!=null &&
              this.authority.equals(other.getAuthority()))) &&
            ((this.rank==null && other.getRank()==null) || 
             (this.rank!=null &&
              this.rank.equals(other.getRank()))) &&
            ((this.status==null && other.getStatus()==null) || 
             (this.status!=null &&
              this.status.equals(other.getStatus()))) &&
            ((this.valid_guid==null && other.getValid_guid()==null) || 
             (this.valid_guid!=null &&
              this.valid_guid.equals(other.getValid_guid()))) &&
            ((this.valid_name==null && other.getValid_name()==null) || 
             (this.valid_name!=null &&
              this.valid_name.equals(other.getValid_name()))) &&
            ((this.valid_authority==null && other.getValid_authority()==null) || 
             (this.valid_authority!=null &&
              this.valid_authority.equals(other.getValid_authority()))) &&
            ((this.kingdom==null && other.getKingdom()==null) || 
             (this.kingdom!=null &&
              this.kingdom.equals(other.getKingdom()))) &&
            ((this.phylum==null && other.getPhylum()==null) || 
             (this.phylum!=null &&
              this.phylum.equals(other.getPhylum()))) &&
            ((this._class==null && other.get_class()==null) || 
             (this._class!=null &&
              this._class.equals(other.get_class()))) &&
            ((this.order==null && other.getOrder()==null) || 
             (this.order!=null &&
              this.order.equals(other.getOrder()))) &&
            ((this.family==null && other.getFamily()==null) || 
             (this.family!=null &&
              this.family.equals(other.getFamily()))) &&
            ((this.genus==null && other.getGenus()==null) || 
             (this.genus!=null &&
              this.genus.equals(other.getGenus()))) &&
            ((this.citation==null && other.getCitation()==null) || 
             (this.citation!=null &&
              this.citation.equals(other.getCitation()))) &&
            ((this.match_type==null && other.getMatch_type()==null) || 
             (this.match_type!=null &&
              this.match_type.equals(other.getMatch_type())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getGUID() != null) {
            _hashCode += getGUID().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        if (getScientificname() != null) {
            _hashCode += getScientificname().hashCode();
        }
        if (getAuthority() != null) {
            _hashCode += getAuthority().hashCode();
        }
        if (getRank() != null) {
            _hashCode += getRank().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getValid_guid() != null) {
            _hashCode += getValid_guid().hashCode();
        }
        if (getValid_name() != null) {
            _hashCode += getValid_name().hashCode();
        }
        if (getValid_authority() != null) {
            _hashCode += getValid_authority().hashCode();
        }
        if (getKingdom() != null) {
            _hashCode += getKingdom().hashCode();
        }
        if (getPhylum() != null) {
            _hashCode += getPhylum().hashCode();
        }
        if (get_class() != null) {
            _hashCode += get_class().hashCode();
        }
        if (getOrder() != null) {
            _hashCode += getOrder().hashCode();
        }
        if (getFamily() != null) {
            _hashCode += getFamily().hashCode();
        }
        if (getGenus() != null) {
            _hashCode += getGenus().hashCode();
        }
        if (getCitation() != null) {
            _hashCode += getCitation().hashCode();
        }
        if (getMatch_type() != null) {
            _hashCode += getMatch_type().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PESIRecord.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://PESI/v0.5", "PESIRecord"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("GUID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "GUID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scientificname");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scientificname"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("authority");
        elemField.setXmlName(new javax.xml.namespace.QName("", "authority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rank");
        elemField.setXmlName(new javax.xml.namespace.QName("", "rank"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(new javax.xml.namespace.QName("", "status"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valid_guid");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valid_guid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valid_name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valid_name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valid_authority");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valid_authority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("kingdom");
        elemField.setXmlName(new javax.xml.namespace.QName("", "kingdom"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("phylum");
        elemField.setXmlName(new javax.xml.namespace.QName("", "phylum"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_class");
        elemField.setXmlName(new javax.xml.namespace.QName("", "class"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("order");
        elemField.setXmlName(new javax.xml.namespace.QName("", "order"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("family");
        elemField.setXmlName(new javax.xml.namespace.QName("", "family"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("genus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "genus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("citation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "citation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("match_type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "match_type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
