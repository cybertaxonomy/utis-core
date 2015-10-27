// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.store;

import java.io.File;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.blueprints.oupls.sail.SailLoader;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author a.kohlbecker
 * @date Oct 19, 2015
 *
 */
public class Neo4jStore extends Store{

    private Neo4j2Graph graph;
    private Sail sail;
    private SailRepository sailRepo;
    private String storeName = "neo4j";


    /**
     * @throws Exception
     */
    public Neo4jStore() throws Exception {
        super();
    }

    public Neo4jStore(String storeName) throws Exception {
        super();
        this.storeName = storeName;
    }


    /**
     * {@inheritDoc}
     * @throws SailException
     */
    @Override
    protected void initStoreEngine() throws Exception  {

        graph = new Neo4j2Graph(storeLocation.toString());
        sail = new GraphSail<Neo4j2Graph>(graph);
        sail.initialize();
        sailRepo = new SailRepository(sail);

//        logger.info("Using Neo4jGraph store at " + storeLocation.toString());
//        logger.info("Neo4jGraph has " + sizeInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopStoreEngine() throws Exception {
        sailRepo.shutDown();
        sail.shutDown(); // should be none by the above command already
    }

    /**
     * @throws Exception
     *
     */
    @Override
    protected void load(File rdfFile) throws Exception {

        SailLoader loader = new SailLoader(sail);
//            loader.setBufferSize(100000); // TODO optimize?
        logger.info("loading RDF/XML into Neo4jGraph store");
        loader.load(rdfFile);
        logger.info("loading RDF/XML done");
        logger.info("Neo4jGraph has " +  sizeInfo());

        logger.info("rdf loaded into Neo4jGraph store at " + storeLocation);
    }


    private long countEdges() {
        GremlinPipeline<Neo4j2Graph, Object> pipe = new GremlinPipeline<Neo4j2Graph, Object>();
        return pipe.start(graph).E().count();
    }

    private long countVertexes() {
        GremlinPipeline<Neo4j2Graph, Object> pipe = new GremlinPipeline<Neo4j2Graph, Object>();
        return pipe.start(graph).V().count();
    }

    public String sizeInfo() {
        return countEdges() + " edges, " + countVertexes() + " vertexes";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String storeName() {
        return storeName;
    }

    public Graph graph() {
        return graph;
    }

    /**
     * @return the sailRepo
     */
    public SailRepository getSailRepo() {
        return sailRepo;
    }


}
