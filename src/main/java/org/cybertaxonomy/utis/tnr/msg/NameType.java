//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.07.09 at 05:13:44 PM CEST 
//


package org.cybertaxonomy.utis.tnr.msg;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nameType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="nameType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="taxon"/&gt;
 *     &lt;enumeration value="synonym"/&gt;
 *     &lt;enumeration value="vernacularName"/&gt;
 *     &lt;enumeration value="otherName"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "nameType")
@XmlEnum
public enum NameType {

    @XmlEnumValue("taxon")
    TAXON("taxon"),
    @XmlEnumValue("synonym")
    SYNONYM("synonym"),
    @XmlEnumValue("vernacularName")
    VERNACULAR_NAME("vernacularName"),
    @XmlEnumValue("otherName")
    OTHER_NAME("otherName");
    private final String value;

    NameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NameType fromValue(String v) {
        for (NameType c: NameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
