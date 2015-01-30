Compiling
============

### compiling the schema:

  mvn jaxb2:generate

the xml schema file contains ```<annox:annotate>``` elements
for the xjc Annotate Plugin which will compile them into java annotations.
See https://github.com/highsource/jaxb2-annotate-plugin  for more information.

### compiling wsdl2java

re-enable the disabled dependencies in the pom.xml by un-commenting them. The according section is
between:

```
   <!-- ======================================================================= -->
   <!-- Dependencies needed for Apache Axix WSDL2Java (only during development) -->
   ...
   <!-- ======================================================================= -->
```
then compile the java classes

```
java -cp  $(mvn3 dependency:build-classpath | grep -v "\[")  org.apache.axis.wsdl.WSDL2Java <url-to-wsdl-file>
```