<?xml version="1.0" encoding="UTF-8" ?>

     <!--
          Document   : synreq_to_col.xsl
          Author     : c.mathew
          Description: converts the Catalogue of Life response to the internal
                       synonym response.
          
          -->

     <xsl:stylesheet version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
       <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" encoding="utf-8"/>
       


       <xsl:template match="/">   
       <tnrResponse xmlns="http://cybertaxonomy.org/utis/tnr/msg" checklist="Catalogue Of Life" checklist_url="http://www.catalogueoflife.org/prototype/content/about" >
         <xsl:apply-templates select="results/result"/>  
       </tnrResponse>   
     </xsl:template>

     <xsl:template match="result">

       <xsl:choose>
         <xsl:when test="name_status='accepted name'">           
           <acceptedName xmlns="http://cybertaxonomy.org/utis/tnr/msg" >
             <taxonName>
               <authorship><xsl:apply-templates select="author"/></authorship>
               <name>
                 <nameCanonical><xsl:apply-templates select="name"/></nameCanonical>
                 <nameComplete>
                   <xsl:apply-templates select="name"/>
                   <xsl:text> </xsl:text>   
                   <xsl:apply-templates select="author"/>
                 </nameComplete>
                 <atomisedName>
                   <subGenus>
                     <genusPart><xsl:apply-templates select="genus"/></genusPart>
                     <specificEpithet><xsl:apply-templates select="species"/></specificEpithet>
                     <infraspecificEpithet><xsl:apply-templates select="infraspecies"/></infraspecificEpithet>
                   </subGenus>
                 </atomisedName>
                 <nameStatus><xsl:apply-templates select="name_status"/></nameStatus>
               </name>
               <rank><xsl:apply-templates select="rank"/></rank>
             </taxonName>
             <source>
               <name><xsl:apply-templates select="source_database"/></name>
               <url><xsl:apply-templates select="source_database_url"/></url>
             </source>
             <info>
               <url><xsl:apply-templates select="url"/></url>
             </info>
             <classification><xsl:apply-templates select="classification/taxon"/></classification>
           </acceptedName>
         </xsl:when>
         <xsl:otherwise>
           <otherName xmlns="http://cybertaxonomy.org/utis/tnr/msg">
             <taxonName>
               <authorship><xsl:apply-templates select="author"/></authorship>
               <name>
                 <nameCanonical><xsl:apply-templates select="name"/></nameCanonical>
                 <nameComplete>
                   <xsl:apply-templates select="name"/>
                   <xsl:text> </xsl:text>   
                   <xsl:apply-templates select="author"/>
                 </nameComplete>
                 <atomisedName>
                   <subGenus>
                     <genusPart><xsl:apply-templates select="genus"/></genusPart>
                     <specificEpithet><xsl:apply-templates select="species"/></specificEpithet>
                     <infraspecificEpithet><xsl:apply-templates select="infraspecies"/></infraspecificEpithet>
                   </subGenus>
                 </atomisedName>
                 <nameStatus><xsl:apply-templates select="name_status"/></nameStatus>
               </name>
               <rank><xsl:apply-templates select="rank"/></rank>
             </taxonName>
             <source>
               <name><xsl:apply-templates select="source_database"/></name>
               <url><xsl:apply-templates select="source_database_url"/></url>
             </source>
             <info>
               <url><xsl:apply-templates select="url"/></url>
             </info>
           </otherName>
         </xsl:otherwise>
       </xsl:choose>
       <xsl:apply-templates select="synonyms/synonym"/>      
      
     </xsl:template>
     
     <xsl:template match="taxon">            
           <xsl:if test="rank='Kingdom'">
             <kingdom>
               <xsl:apply-templates select="name"/>
             </kingdom>
           </xsl:if>  

           <xsl:if test="rank='Phylum'">
             <phylum>
               <xsl:apply-templates select="name"/>
             </phylum>
           </xsl:if>  

           <xsl:if test="rank='Class'">
             <class>
               <xsl:apply-templates select="name"/>
             </class>
           </xsl:if>  

           <xsl:if test="rank='Order'">
             <order>
               <xsl:apply-templates select="name"/>
             </order>
           </xsl:if>  
           
           <xsl:if test="rank='Family'">
             <family>
               <xsl:apply-templates select="name"/>
             </family>
           </xsl:if>  
           
           <xsl:if test="rank='Genus'">
             <genus>
               <xsl:apply-templates select="name"/>
             </genus>
           </xsl:if>  

     </xsl:template>
       
     <xsl:template match="synonym">
       <xsl:variable name="accNameID" select="/results/result/id"></xsl:variable>
       <xsl:variable name="synID" select="id"></xsl:variable>
       <synonym xmlns="http://cybertaxonomy.org/utis/tnr/msg">
         <taxonName>
           <authorship><xsl:apply-templates select="author"/></authorship>
           <name>
             <nameCanonical><xsl:apply-templates select="name"/></nameCanonical>
             <nameComplete>
               <xsl:apply-templates select="name"/>
               <xsl:text> </xsl:text>   
               <xsl:apply-templates select="author"/>
             </nameComplete>
             <atomisedName>
               <subGenus>
                 <genusPart><xsl:apply-templates select="genus"/></genusPart>
                 <specificEpithet><xsl:apply-templates select="species"/></specificEpithet>
                 <infraspecificEpithet><xsl:apply-templates select="infraspecies"/></infraspecificEpithet>
               </subGenus>
             </atomisedName>
             <nameStatus><xsl:apply-templates select="name_status"/></nameStatus>
           </name>
           <rank><xsl:apply-templates select="rank"/></rank>
         </taxonName>
         <source>
           <name><xsl:apply-templates select="source_database"/></name>
           <url><xsl:apply-templates select="source_database_url"/></url>
         </source>
         <info>
           <url><xsl:value-of select="concat('http://www.catalogueoflife.org/col/details/species/id/',$accNameID,'/synonym/',$synID)"/></url>
         </info>
       </synonym>
     
     </xsl:template>
    
     


   </xsl:stylesheet>
