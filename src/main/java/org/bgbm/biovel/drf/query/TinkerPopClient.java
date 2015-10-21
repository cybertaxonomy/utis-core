/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package org.bgbm.biovel.drf.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.checklist.EEA_BDC_Client.RdfSchema;
import org.bgbm.biovel.drf.store.Neo4jStore;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Developer links:
 * <ul>
 * <li>https://github.com/tinkerpop/blueprints/wiki/Sail-Ouplementation</li>
 * <li>https://github.com/tinkerpop/gremlin/wiki</li>
 * <li>http://rdf4j.org/sesame/2.7/docs/users.docbook?view#section-repository-api3</li>
 * <li>https://github.com/tinkerpop/gremlin/wiki/SPARQL-vs.-Gremlin</li>
 * <li>https://github.com/tinkerpop/gremlin/wiki/Using-Gremlin-through-Java</li>
 * </ul>
 * @author a.kohlbecker
 * @date Sep 30, 2015
 *
 */
public class TinkerPopClient implements IQueryClient {

    protected Logger logger = LoggerFactory.getLogger(TinkerPopClient.class);

    private final String baseUri = null;

    private Neo4jStore tripleStore = null;

    /**
     * A model for caching
     */
    private final Model cache = null;


    /**
     * @param tripleStore
     */
    public TinkerPopClient(Neo4jStore tripleStore) {
        this.tripleStore = tripleStore;
    }

    public Model describe(String queryString) throws DRFChecklistException, QueryEvaluationException {

        // directly execute SPARQL queries in Gremlin over Sail-based graphs
        // using the method SailGraph.executeSparql().

        TupleQuery qe = executionFor(queryString);
        TupleQueryResult result = qe.evaluate();
        System.err.println(result.toString());

        if(result != null && logger.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("subjects in response:\n");
            int i = 1;
            try {
                for(; result.hasNext(); ++i) {
                    BindingSet res = result.next();
                    msg.append("    " + i + ": " + res.toString() + "\n");
                }
            } catch (QueryEvaluationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug(msg.toString());
        }

        return null; //FIXME result;
    }

    /**
     * @param queryString
     * @return
     */
    private TupleQuery executionFor(String queryString) {


        if(baseUri != null) {
            // see https://github.com/tinkerpop/blueprints/wiki/Sail-Implementation
            // FIXME
            throw new RuntimeException("Mode unsupported");
            // Graph graph = new SparqlRepositorySailGraph(baseUri);
            //return QueryExecutionFactory.sparqlService(baseUri, query);
        }
        if(tripleStore != null) {
            // local TDB Store
            try {
                return  tripleStore.connection().prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            } catch (MalformedQueryException | RepositoryException e) {
                // TODO Auto-generated catch block
                logger.error("Error while perparing query", e);
            }

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

    /**
     * @param matchedResourceURI
     * @return
     * @throws DRFChecklistException
     */
    public Resource getFromUri(URI matchedResourceURI) {
        return getFromUri(matchedResourceURI.toString());
    }

    public Resource getFromUri(String uri) {

        Model model = null;
        if(tripleStore != null) {
            /* FIXME
            Dataset dataset = tripleStore.getDataset();
            dataset.begin(ReadWrite.READ) ;
            model = dataset.getDefaultModel();
            dataset.end();
            */
        } else {
            model = cache;
            // FIXME the same uri resource is loaded from remote multiple times
            //       create an in memory model as cache for the models loaded
            //       in the getFromUri
            //       so that all resources loaded are put into that model
            //       clean up the cache when it reaches a specific size
            logger.debug("loading remote UriResource " + uri);
            model.read(uri);
        }
        if(logger.isDebugEnabled()) {
            // see https://jena.apache.org/documentation/io/rdf-output.html#examples
            RDFDataMgr.write(System.err, model, RDFFormat.TURTLE_PRETTY);
        }
        return model.getResource(uri);

    }



}
