/**
 * Distribution.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.cybertaxonomy.utis.checklist.pesi;

public class Distribution  implements java.io.Serializable {
    private java.lang.String locality;

    private java.lang.String occurrenceStatus;

    private java.lang.String TDWG_level4;

    private int MRGID;

    public Distribution() {
    }

    public Distribution(
           java.lang.String locality,
           java.lang.String occurrenceStatus,
           java.lang.String TDWG_level4,
           int MRGID) {
           this.locality = locality;
           this.occurrenceStatus = occurrenceStatus;
           this.TDWG_level4 = TDWG_level4;
           this.MRGID = MRGID;
    }


    /**
     * Gets the locality value for this Distribution.
     * 
     * @return locality
     */
    public java.lang.String getLocality() {
        return locality;
    }


    /**
     * Sets the locality value for this Distribution.
     * 
     * @param locality
     */
    public void setLocality(java.lang.String locality) {
        this.locality = locality;
    }


    /**
     * Gets the occurrenceStatus value for this Distribution.
     * 
     * @return occurrenceStatus
     */
    public java.lang.String getOccurrenceStatus() {
        return occurrenceStatus;
    }


    /**
     * Sets the occurrenceStatus value for this Distribution.
     * 
     * @param occurrenceStatus
     */
    public void setOccurrenceStatus(java.lang.String occurrenceStatus) {
        this.occurrenceStatus = occurrenceStatus;
    }


    /**
     * Gets the TDWG_level4 value for this Distribution.
     * 
     * @return TDWG_level4
     */
    public java.lang.String getTDWG_level4() {
        return TDWG_level4;
    }


    /**
     * Sets the TDWG_level4 value for this Distribution.
     * 
     * @param TDWG_level4
     */
    public void setTDWG_level4(java.lang.String TDWG_level4) {
        this.TDWG_level4 = TDWG_level4;
    }


    /**
     * Gets the MRGID value for this Distribution.
     * 
     * @return MRGID
     */
    public int getMRGID() {
        return MRGID;
    }


    /**
     * Sets the MRGID value for this Distribution.
     * 
     * @param MRGID
     */
    public void setMRGID(int MRGID) {
        this.MRGID = MRGID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Distribution)) return false;
        Distribution other = (Distribution) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.locality==null && other.getLocality()==null) || 
             (this.locality!=null &&
              this.locality.equals(other.getLocality()))) &&
            ((this.occurrenceStatus==null && other.getOccurrenceStatus()==null) || 
             (this.occurrenceStatus!=null &&
              this.occurrenceStatus.equals(other.getOccurrenceStatus()))) &&
            ((this.TDWG_level4==null && other.getTDWG_level4()==null) || 
             (this.TDWG_level4!=null &&
              this.TDWG_level4.equals(other.getTDWG_level4()))) &&
            this.MRGID == other.getMRGID();
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
        if (getLocality() != null) {
            _hashCode += getLocality().hashCode();
        }
        if (getOccurrenceStatus() != null) {
            _hashCode += getOccurrenceStatus().hashCode();
        }
        if (getTDWG_level4() != null) {
            _hashCode += getTDWG_level4().hashCode();
        }
        _hashCode += getMRGID();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Distribution.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://PESI/v0.5", "Distribution"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("locality");
        elemField.setXmlName(new javax.xml.namespace.QName("", "locality"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("occurrenceStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "occurrenceStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("TDWG_level4");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TDWG_level4"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MRGID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MRGID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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
