/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package org.bgbm.biovel.drf.query;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.EEA_BDC_Client.RdfSchema;
import org.bgbm.biovel.drf.store.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author a.kohlbecker
 * @date Sep 30, 2015
 *
 */
public class SparqlClient implements IQueryClient {

    protected Logger logger = LoggerFactory.getLogger(SparqlClient.class);

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static final File userHomeDir = new File(System.getProperty("user.home"));
    private static final File utisHome = new File(userHomeDir, ".utis");

    private String baseUri = null;

    private TripleStore tripleStore = null;

    /**
     * A model for caching
     */
    private final Model cache = null;


    /**
     *
     */
    public SparqlClient(String baseUri) {
        this.baseUri = baseUri;
    }

    /**
     * @param tripleStore
     */
    public SparqlClient(TripleStore tripleStore) {
        this.tripleStore = tripleStore;
    }


    public String select(String queryString) throws DRFChecklistException {

        QueryExecution qe = executionFor(queryString);

        try {
            ResultSet results = qe.execSelect();
            System.err.println(ResultSetFormatter.asText(results));
        } catch (HttpException e) {
            switch(e.getResponseCode()) {
                // interpretation based on
                // http://image.slidesharecdn.com/swtss1006sparql-100614020655-phpapp02/95/semantic-web-technologies-ss-2010-06-sparql-46-728.jpg?cb=1276481316
                case 400:
                    throw new DRFChecklistException("Malformed Query ?", e);
                case 500:
                    throw new DRFChecklistException("Query Request Refused ?", e);
                default:
                    throw e;
            }
        } finally {
            // Important - free up resources used running the query
            qe.close();
        }

        return null;
    }

    public Model describe(String queryString) throws DRFChecklistException {

        QueryExecution qe = executionFor(queryString);
        Model result = null;
        try {
            if(tripleStore != null) {
                tripleStore.getDataset().begin(ReadWrite.READ);
            }
            result = qe.execDescribe();
            if(logger.isDebugEnabled()) {
                result.write(System.err);
            }

        } catch (HttpException e) {
            switch(e.getResponseCode()) {
                // interpretation based on
                // http://image.slidesharecdn.com/swtss1006sparql-100614020655-phpapp02/95/semantic-web-technologies-ss-2010-06-sparql-46-728.jpg?cb=1276481316
                case 400:
                    throw new DRFChecklistException("Malformed Query ?", e);
                case 500:
                    throw new DRFChecklistException("Query Request Refused ?", e);
                default:
                    throw e;
            }
        } finally {
            // Important - free up resources used running the query
            qe.close();
        }

        if(result != null && logger.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("subjects in response:\n");
            int i = 1;
            for(ResIterator it = result.listSubjects(); it.hasNext(); ++i) {
                Resource res = it.next();
                msg.append("    " + i + ": " + res.toString() + "\n");
            }
            logger.debug(msg.toString());
        }
        if(tripleStore != null) {
            tripleStore.getDataset().end();
        }

        return result;
    }

    /**
     * @param queryString
     * @return
     */
    private QueryExecution executionFor(String queryString) {


        if(baseUri != null) {
            Query query = QueryFactory.create(queryString);
            return QueryExecutionFactory.sparqlService(baseUri, query);
        }
        if(tripleStore != null) {
            // local TDB Store
            return QueryExecutionFactory.create(queryString, tripleStore.getDataset());
        }

        return null;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public RDFNode asSingleObject(Resource subject, RdfSchema nameSpace, String localName) {
        RDFNode node = null;
        StmtIterator propertyIt = null;
        Resource _subject = subject;
        try {

            Model _model = _subject.getModel();
            Property property = _model.getProperty(nameSpace.schemaUri(), localName);
            propertyIt = _subject.listProperties(property);

            boolean propertyInGraph = propertyIt.hasNext();
            if(!propertyInGraph ) {
                _subject = getFromUri(subject.getURI());
                propertyIt = _subject.listProperties(property);
            }

            node = propertyIt.next().getObject();
        } catch (NoSuchElementException e) {
            if(logger.isTraceEnabled()) {
                logger.debug(_subject.getURI() + " " +  nameSpace + ":" + localName + " not found in current graph");
                printProperties(_subject);
            }
        }
        return node;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public StmtIterator listProperties(Resource subject, RdfSchema nameSpace, String localName) {
        RDFNode node = null;
        StmtIterator propertyIt = null;
        Resource _subject = subject;
        try {

            Model _model = _subject.getModel();
            Property property = _model.getProperty(nameSpace.schemaUri(), localName);
            propertyIt = _subject.listProperties(property);

            boolean propertyInGraph = propertyIt.hasNext();
            if(!propertyInGraph ) {
                _subject = getFromUri(subject.getURI());
                propertyIt = _subject.listProperties(property);
            }

        } catch (NoSuchElementException e) {
            if(logger.isTraceEnabled()) {
                logger.debug(_subject.getURI() + " " +  nameSpace + ":" + localName + " not found in current graph");
                printProperties(_subject);
            }
        }
        return propertyIt;
    }

    public  List<RDFNode> listObjects(Resource subject, RdfSchema nameSpace, String localName) {

        List<RDFNode> list = new ArrayList<RDFNode>();
        StmtIterator it = listProperties(subject, nameSpace, localName);
        while (it.hasNext()) {
            list.add(it.next().getObject());
        }
        return list;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public String objectAsString(Resource subject, RdfSchema nameSpace, String localName) {
        String txt = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if(node != null) {
            txt = node.toString();
        }
        return txt;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public Resource objectAsResource(Resource subject, RdfSchema nameSpace, String localName) {
        Resource resource = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if(node != null) {
            node.isResource();
            resource  = node.asResource();
        }
        return resource;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public URI objectAsURI(Resource subject, RdfSchema nameSpace, String localName) {
        URI uri = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if(node != null) {
            node.isURIResource();
            try {
                uri  = new URI(node.asResource().getURI());
            } catch (URISyntaxException e) {
                // this should actually never happen
                throw new RuntimeException(e);
            }
        }
        return uri;
    }

    public List<Resource> listResources(Model model, Property filterProperty, String filterValue, Resource exclude){

        List<Resource> resultList = new ArrayList<Resource>();
        ResIterator resItr = model.listSubjects();
        while(resItr.hasNext()) {
            Resource resource = resItr.next();

            if(exclude != null && resource.getURI().equals(exclude.getURI())) {
                continue;
            }

            boolean hasProperty = true;
            if(filterProperty != null) {
                if(filterValue != null) {
                    hasProperty = resource.hasProperty(filterProperty, filterValue);
                } else {
                    hasProperty = resource.hasProperty(filterProperty);
                }
            }

            if(filterProperty == null || hasProperty) {
                resultList.add(resource);
                logger.debug("adding " + resource.getURI());
            } else {
                logger.debug("skipping " + resource.getURI());
            }
        }
        return resultList;
    }

    /**
     * @param subject
     */
    private void printProperties(Resource subject) {
        for( StmtIterator it = subject.listProperties(); it.hasNext(); ) {
            System.err.println(it.next().toString());
        }
    }

    public Resource getFromUri(String uri) {

        Model model;
        if(tripleStore != null) {
            Dataset dataset = tripleStore.getDataset();
            dataset.begin(ReadWrite.READ) ;
            model = dataset.getDefaultModel();
            dataset.end();
        } else {
            // FIXME the same uri resource is loaded from remote multiple times
            //       create an in memory model as cache for the models loaded
            //       in the getFromUri
            //       so that all resources loaded are put into that model
            //       clean up the cache when it reaches a specific size
            logger.debug("loading remote UriResource " + uri);
            model = ModelFactory.createDefaultModel();
            model.read(uri);
        }
        if(logger.isDebugEnabled()) {
            model.write(System.err);
        }
        return model.getResource(uri);

    }

    /**
     * @param matchedResourceURI
     * @return
     * @throws DRFChecklistException
     */
    public Resource getFromUri(URI matchedResourceURI) {
        return getFromUri(matchedResourceURI.toString());
    }


}
