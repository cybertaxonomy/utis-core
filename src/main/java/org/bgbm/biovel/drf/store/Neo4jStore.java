// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.bgbm.biovel.drf.store;

import java.io.File;

import org.openrdf.repository.base.RepositoryConnectionBase;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.blueprints.oupls.sail.SailLoader;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author a.kohlbecker
 * @date Oct 19, 2015
 *
 */
public class Neo4jStore extends Store{


    /**
     * @throws Exception
     */
    public Neo4jStore() throws Exception {
        super();
    }

    private RepositoryConnectionBase connection;
    private Neo4jGraph graph;
    private Sail sail = null;


    /**
     * {@inheritDoc}
     * @throws SailException
     */
    @Override
    protected void initStoreEngine() throws Exception  {

        graph = new Neo4jGraph(storeLocation.toString());
        sail = new GraphSail(graph);
        sail.initialize();
        logger.info("Using Neo4jGraph store at " + storeLocation.toString());
        logger.info("Neo4jGraph has " + sizeInfo());
    }


    /**
     * @throws Exception
     *
     */
    @Override
    protected void load(File rdfFile) throws Exception {

        SailLoader loader = new SailLoader(sail);
//            loader.setBufferSize(100000);
        logger.info("loading RDF/XML into Neo4jGraph store");
        loader.load(rdfFile);
        logger.info("loading RDF/XML done");
        logger.info("Neo4jGraph has " +  sizeInfo());

        logger.info("rdf loaded into Neo4jGraph store at " + storeLocation);
        connection = new SailRepository(sail).getConnection();
    }


    private long countEdges() {
        GremlinPipeline<Neo4jGraph, Object> pipe = new GremlinPipeline<Neo4jGraph, Object>();
        return pipe.start(graph).E().count();
    }

    private long countVertexes() {
        GremlinPipeline<Neo4jGraph, Object> pipe = new GremlinPipeline<Neo4jGraph, Object>();
        return pipe.start(graph).V().count();
    }

    public String sizeInfo() {
        return countEdges() + " edges, " + countVertexes() + " vertexes";
    }


    /**
     * @return the connection
     */
    public RepositoryConnectionBase connection() {
        return connection;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String storeName() {
        return "neo4j";
    }

    public Graph graph() {
        return graph;
    }



}
