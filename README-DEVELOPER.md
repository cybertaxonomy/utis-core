Compiling
============

### compiling the schema:

    mvn jaxb2:generate

the xml schema file contains ``<annox:annotate>`` elements
for the xjc Annotate Plugin which will compile them into java annotations.
See https://github.com/highsource/jaxb2-annotate-plugin  for more information.

### compiling wsdl2java

* https://axis.apache.org/axis/java/reference.html

re-enable the disabled dependencies in the pom.xml by un-commenting them. The according section is
between:

    <!-- ======================================================================= -->
    <!-- Dependencies needed for Apache Axix WSDL2Java (only during development) -->
    ...
    <!-- ======================================================================= -->
    
then compile the java classes

For PESI

    java -cp  $(mvn dependency:build-classpath | grep -v "\[")  org.apache.axis.wsdl.WSDL2Java "http://www.eu-nomen.eu/portal/soap.php\?wsdl\=1"  -p org.cybertaxonomy.utis.checklist.pesi -o src/main/java/

For WoRMS

    java -cp  $(mvn dependency:build-classpath | grep -v "\[")  org.apache.axis.wsdl.WSDL2Java "http://www.marinespecies.org/aphia.php?p=soap&wsdl=1"  -p org.cybertaxonomy.utis.checklist.worms -o src/main/java/

    
### debug the neo4j store

1. Download the neo4j community edition (neo4j-community-2.0.4) and extract it
1. In the extracted package navigate to the bin folder and start the neo4j shell:

    ./neo4j-shell -path ~/.utis/neo4j/

NOTE: you need to stop utis in advance of starting the neo4j shell!
    
    