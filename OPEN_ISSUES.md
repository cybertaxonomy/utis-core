Open Issues
========================

TODO
-------

### Documentation
add xs:documentation to the XML Schema file so that xjc can add them as java doc comments
see (https://jaxb.java.net/tutorial/section_5_4-Adding-Documentation.html#Adding%20Documentation and
http://stackoverflow.com/questions/1650249/how-to-make-generated-classes-contain-javadoc-from-xml-schema-documentation?rq=1)

Add a child to `<xsd:complexType>` or `<xsd: element>` or `<xsd:attribute>`:

    <xsd:annotation><xsd:appinfo><jxb:XXX><jxb:javadoc>
      This is my comment for a class/property
    </jxb:javadoc></jxb:XXX></xsd:appinfo></xsd:annotation>

Where XXX is either "class" or "property".

For a package you write a child to xsd:schema

    <xsd:annotation><xsd:appinfo><jxb:schemaBindings><jxb:package name="com.acme"><jxb:javadoc>
      This is my comment for a package
    </jxb:javadoc></jxb:package></jxb:schemaBindings></xsd:appinfo></xsd:annotation>

Writing HTML document requires bracketing with `<![CDATA[ --- ]]>`

### Annotations
Annotate plugins is capable of adding arbitrary annotations to the generated sources.

http://confluence.highsource.org/display/J2B/Annotate+Plugin




