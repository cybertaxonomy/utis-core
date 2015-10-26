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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
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
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.FastNoSuchElementException;

/**
 *
 * Developer links:
 * <ul>
 * <li>https://github.com/tinkerpop/blueprints/wiki/Sail-Ouplementation</li>
 * <li>https://github.com/tinkerpop/gremlin/wiki</li>
 * <li>
 * http://rdf4j.org/sesame/2.7/docs/users.docbook?view#section-repository-api3</li>
 * <li>https://github.com/tinkerpop/gremlin/wiki/SPARQL-vs.-Gremlin</li>
 * <li>https://github.com/tinkerpop/gremlin/wiki/Using-Gremlin-through-Java</li>
 * <li>
 * http://markorodriguez.com/2011/06/15/graph-pattern-matching-with-gremlin-1-1/
 * </li>
 * <li>https://github.com/datablend/neo4j-sail-test</li>
 * </ul>
 *
 * @author a.kohlbecker
 * @date Sep 30, 2015
 *
 */
public class TinkerPopClient implements IQueryClient {

    public static final String KIND = GraphSail.KIND;

    public static final String VALUE = GraphSail.VALUE;

    public static final Object URI = GraphSail.URI;

    protected Logger logger = LoggerFactory.getLogger(TinkerPopClient.class);

    private final String baseUri = null;

    private Neo4jStore tripleStore = null;

    /**
     * @param tripleStore
     */
    public TinkerPopClient(Neo4jStore tripleStore) {
        this.tripleStore = tripleStore;
    }

    public GremlinPipeline<Graph, Graph> newPipe() {
        GremlinPipeline<Graph, Object> pipe = new GremlinPipeline<Graph, Object>();
        return pipe.start(graph());
    }

    public Graph graph() {
        return tripleStore.graph();
    }

    /**
     * @param sparql
     * @return
     * @throws MalformedQueryException
     * @throws RepositoryException
     * @throws QueryEvaluationException
     *
     * @deprecated directly use connection() and do not forget to close is after
     *             doing the query. See
     *             {@link org.openrdf.repository.sail.SailRepository#getConnection()}
     */
    @Deprecated
    public TupleQueryResult execute(String sparql) throws MalformedQueryException, RepositoryException,
            QueryEvaluationException {
        TupleQuery query = connection().prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        TupleQueryResult result = query.evaluate();
        return result;
    }

    /**
     * See {@link org.openrdf.repository.sail.SailRepository#getConnection()}
     *
     * @return
     * @throws RepositoryException
     */
    public SailRepositoryConnection connection() throws RepositoryException {
        SailRepositoryConnection connection = tripleStore.getSailRepo().getConnection();
        return connection;
    }

    public PipeFunction<Vertex, Boolean> createRegexMatchFilter(final String regex) {
        return new PipeFunction<Vertex, Boolean>() {

            @Override
            public Boolean compute(Vertex v) {
                return v.toString().matches(regex);
            }
        };
    }

    public PipeFunction<Vertex, Boolean> createEqualsFilter(final String string) {
        return new PipeFunction<Vertex, Boolean>() {

            @Override
            public Boolean compute(Vertex v) {
                return v.toString().equals(string);
            }
        };
    }

    public PipeFunction<Vertex, Boolean> createStarttWithFilter(final String string) {
        return new PipeFunction<Vertex, Boolean>() {

            @Override
            public Boolean compute(Vertex v) {
                return v.toString().startsWith(string);
            }
        };
    }

    public Model describe(String queryString) throws DRFChecklistException, QueryEvaluationException {

        // directly execute SPARQL queries in Gremlin over Sail-based graphs
        // using the method SailGraph.executeSparql().

        TupleQuery qe = executionFor(queryString);
        TupleQueryResult result = qe.evaluate();
        System.err.println(result.toString());

        if (result != null && logger.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("subjects in response:\n");
            int i = 1;
            try {
                for (; result.hasNext(); ++i) {
                    BindingSet res = result.next();
                    msg.append("    " + i + ": " + res.toString() + "\n");
                }
            } catch (QueryEvaluationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug(msg.toString());
        }

        return null; // FIXME result;
    }

    /**
     * @param queryString
     * @return
     */
    private TupleQuery executionFor(String queryString) {

        if (baseUri != null) {
            // see
            // https://github.com/tinkerpop/blueprints/wiki/Sail-Implementation
            // FIXME
            throw new RuntimeException("Mode unsupported");
            // Graph graph = new SparqlRepositorySailGraph(baseUri);
            // return QueryExecutionFactory.sparqlService(baseUri, query);
        }
        if (tripleStore != null) {
            // local TDB Store
            try {
                SailRepositoryConnection connection = connection();
                return connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
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
            if (!propertyInGraph) {
                _subject = getFromUri(subject.getURI());
                propertyIt = _subject.listProperties(property);
            }

            node = propertyIt.next().getObject();
        } catch (NoSuchElementException e) {
            if (logger.isTraceEnabled()) {
                logger.debug(_subject.getURI() + " " + nameSpace + ":" + localName + " not found in current graph");
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
            if (!propertyInGraph) {
                _subject = getFromUri(subject.getURI());
                propertyIt = _subject.listProperties(property);
            }

        } catch (NoSuchElementException e) {
            if (logger.isTraceEnabled()) {
                logger.debug(_subject.getURI() + " " + nameSpace + ":" + localName + " not found in current graph");
                printProperties(_subject);
            }
        }
        return propertyIt;
    }

    public List<RDFNode> listObjects(Resource subject, RdfSchema nameSpace, String localName) {

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
    public String relatedVertexValue(GremlinPipeline<Graph, Vertex> pipe, RdfSchema nameSpace, String localName) {
        String txt = null;
        String edgeLabel = nameSpace.propertyURI(localName);
        try {
            txt = pipe.outE(1, edgeLabel).inV().next().getProperty("value");
        } catch (FastNoSuchElementException e) {
            try {
                txt = pipe.inE(1, edgeLabel).inV().next().getProperty("value");
            } catch (FastNoSuchElementException e1) {
                logger.warn("edge with '" + edgeLabel + "' not found");
                if (logger.isDebugEnabled()) {
                    logger.debug("Vertices in pipe:");
                    StringBuffer out = new StringBuffer();
                    for (Vertex v : pipe.toList()) {
                        logger.debug("    " + v);
                    }
                }
            }
        }

        return txt;
    }

    public String relatedVertexValue(Vertex v, RdfSchema nameSpace, String localName) {
        String txt = null;
        String edgeLabel = nameSpace.propertyURI(localName);
        try {
            txt = v.getEdges(Direction.OUT, edgeLabel).iterator().next().getVertex(Direction.IN)
                    .getProperty(GraphSail.VALUE);
        } catch (NoSuchElementException e) {
            try {
                txt = v.getEdges(Direction.IN, edgeLabel).iterator().next().getVertex(Direction.OUT)
                        .getProperty(GraphSail.VALUE);
            } catch (NoSuchElementException e1) {
                logger.warn("edge with '" + edgeLabel + "' not found for " + v);

            }
        }

        return txt;
    }

    //

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public Resource objectAsResource(Resource subject, RdfSchema nameSpace, String localName) {
        Resource resource = null;
        RDFNode node = asSingleObject(subject, nameSpace, localName);
        if (node != null) {
            node.isResource();
            resource = node.asResource();
        }
        return resource;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     * @deprecated unused
     */
    @Deprecated
    public URI relatedVertexURI(GremlinPipeline<Graph, Vertex> pipe, RdfSchema nameSpace, String localName) {
        URI uri = null;
        String edgeLabel = nameSpace.propertyURI(localName);
        try {
            Vertex v = pipe.outE(1, edgeLabel).inV().next();
            if (v.getProperty(GraphSail.KIND).equals(GraphSail.URI)) {
                uri = (URI) v.getProperty(GraphSail.VALUE);
            } else {
                logger.warn("target vertex of '" + edgeLabel + "' is not an URI");
            }
        } catch (FastNoSuchElementException e) {
            logger.warn("edge with '" + edgeLabel + "' not found");
        }

        return uri;
    }

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     * @deprecated possibly broken
     */
    @Deprecated
    public URI relatedVertexURI(Vertex v, RdfSchema nameSpace, String localName) {
        URI uri = null;
        String edgeLabel = nameSpace.propertyURI(localName);
        try {
            if (v.getProperty(GraphSail.KIND).equals(GraphSail.URI)) {
                uri = new URI(v.getProperty(GraphSail.VALUE).toString());
            } else {
                logger.warn("target vertex of '" + edgeLabel + "' is not an URI");
            }
        } catch (NoSuchElementException e) {
            logger.warn("edge with '" + edgeLabel + "' not found");
        } catch (URISyntaxException e) {
            logger.error("Invalid URI id in " + v, e);
        }

        return uri;
    }

    /**
     * @param subject
     */
    private void printProperties(Resource subject) {
        for (StmtIterator it = subject.listProperties(); it.hasNext();) {
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

        // not needed
        return null;

    }

    public void showResults(TupleQueryResult result) throws QueryEvaluationException {
        int i = 0;
        while (result.hasNext()) {
            System.err.println(".");
            BindingSet bindingSet = result.next();
            System.err.println("+");
            for (String colName : result.getBindingNames()) {
                System.err.println(colName + ":" + bindingSet.getValue(colName));
            }
            System.err.println("|");
            i++;
        }
        System.err.println("We found " + i + " results");
        System.err.flush();
    }
}
