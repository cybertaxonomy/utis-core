/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package org.cybertaxonomy.utis.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.cybertaxonomy.utis.checklist.EEA_BDC_Client.RdfSchema;
import org.cybertaxonomy.utis.store.Neo4jStore;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.IndexHits;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Vertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.gremlin.java.GremlinPipeline;
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

    protected static final Logger logger = LoggerFactory.getLogger(TinkerPopClient.class);

    private Neo4jStore graphStore = null;

    /**
     * @param store
     */
    public TinkerPopClient(Neo4jStore store) {
        this.graphStore = store;
    }

    public Graph graph() {
        return graphStore.graph();
    }

    /**
     * See {@link org.openrdf.repository.sail.SailRepository#getConnection()}
     *
     * @return
     * @throws RepositoryException
     */
    public SailRepositoryConnection connection() throws RepositoryException {
        SailRepositoryConnection connection = graphStore.getSailRepo().getConnection();
        return connection;
    }

    /**
     * Returns the first Vertex of the edge with the property specified by
     * <code>nameSpace</code> and <code>localName</code>. Both, directions
     * of edges are taken into account whereas the OUT edge is tested first.
     *
     * @param v
     * @param nameSpace
     * @param localName
     * @return
     */
    public Vertex relatedVertex(Vertex v, RdfSchema nameSpace, String localName) {
        Vertex relatedV = null;
        try {
            relatedV = v.getEdges(Direction.OUT, nameSpace.property(localName)).iterator().next().getVertex(Direction.IN);
        } catch (NoSuchElementException e) {
            try {
                relatedV = v.getEdges(Direction.IN, nameSpace.property(localName)).iterator().next().getVertex(Direction.OUT);
            } catch (NoSuchElementException e2) {
                logger.error("No taxonomy information for " + v.toString());
            }
        }
        return relatedV;
    }

    /**
     * Returns the <code>value</code> property of the first Vertex of the edge with the property specified by
     * <code>nameSpace</code> and <code>localName</code>. Both, directions
     * of edges are taken into account whereas the OUT edge is tested first.
     *
     * @param pipe
     * @param nameSpace
     * @param localName
     * @return
     */
    public String relatedVertexValue(GremlinPipeline<Graph, Vertex> pipe, RdfSchema nameSpace, String localName) {
        String txt = null;
        String edgeLabel = nameSpace.property(localName);
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

    /**
     * Returns the <code>value</code> property of the first Vertex of the edge with the property specified by
     * <code>nameSpace</code> and <code>localName</code>. Both, directions
     * of edges are taken into account whereas the OUT edge is tested first.
     *
     * @param v
     * @param nameSpace
     * @param localName
     * @return
     */
    public String relatedVertexValue(Vertex v, RdfSchema nameSpace, String localName) {
        String txt = null;
        String edgeLabel = nameSpace.property(localName);
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

    /**
     * @param subject
     * @param nameSpace
     * @param localName
     * @return
     */
    public URI vertexURI(Vertex v, RdfSchema nameSpace, String localName) {
        URI uri = null;
        try {
            if (v.getProperty(GraphSail.KIND).equals(GraphSail.URI)) {
                uri = new URI(v.getProperty(GraphSail.VALUE).toString());
            } else {
                logger.warn("vertex of '" + v.toString() + "' is not an URI");
            }
        } catch (URISyntaxException e) {
            logger.error("Invalid URI id in " + v, e);
        }

        return uri;
    }

    /**
     * by using the Neo4j index directly it is possible to
     * take full advantage of the underlying Lucene search engine
     *
     * @param luceneQuery
     * @return
     */
    public ArrayList<Vertex> vertexIndexQuery(String luceneQuery) {
        Neo4j2Graph graph = (Neo4j2Graph)graph();
        AutoIndexer<Node> nodeAutoIndex = graph.getRawGraph().index().getNodeAutoIndexer();
        graph.autoStartTransaction(false);
        IndexHits<Node> nodes = nodeAutoIndex.getAutoIndex().query(luceneQuery);
        ArrayList<Vertex> hitVs = new ArrayList<Vertex>();
        while(nodes.hasNext()) {
            hitVs.add(new Neo4j2Vertex(nodes.next(), graph));
        }
        graph.commit();
        return hitVs;
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
