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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;
import com.tinkerpop.blueprints.oupls.sail.SailLoader;

/**
 * @author a.kohlbecker
 * @date Oct 19, 2015
 *
 */
public class Neo4jStore extends Store{

    private Neo4j2Graph graph;
    private Sail sail;
    private SailRepository sailRepo;
    private final static String STORE_TYPE = "neo4j";

    // January 1, 1970, 00:00:00 GMT as starting time
    private Date lastModified = new Date(0);


    public Neo4jStore(String storeName) throws Exception {
        super(storeName);
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

        logger.info("Using Neo4jGraph store at " + storeLocation.toString());
        logger.info("Neo4jGraph has " + sizeInfo());
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


    public long countEdges() {

        String indexName = graph.getRawGraph().index().getRelationshipAutoIndexer().getAutoIndex().getName();
        return countIndexDocuments("relationship" + File.separator +  indexName);
    }

    public long countVertexes() {

        String indexName = graph.getRawGraph().index().getNodeAutoIndexer().getAutoIndex().getName();
        return countIndexDocuments("node" + File.separator + indexName);
    }

    /**
     * @param nodeAutoIndexName
     * @return
     */
    private long countIndexDocuments(String nodeAutoIndexName) {
        File luceneFolder = new File(storeLocation, "index" + File.separator + "lucene");
        File indexFolder = new File(luceneFolder, nodeAutoIndexName);
        int cnt = 0;
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(indexFolder));
            cnt = reader.numDocs();
        } catch (CorruptIndexException e) {
            logger.warn("CorruptIndexException", e);
        } catch (IOException e) {
            logger.warn("Lucene index can not be read, this is ok as long there store location is empty. Original error: " + e.getMessage());
        }
        return cnt;
    }

    public String sizeInfo() {
        return countEdges() + " edges, " + countVertexes() + " vertexes";
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

    /**
     * @return the lastModified
     */
    public Date getLastModified() {

        try {
            String lastModifiedString = FileUtils.readFileToString(lastModifiedFile());
            lastModified = DateUtil.parseDate(lastModifiedString);
        } catch (IOException e) {
            logger.info("Could not read " + lastModifiedFile().getAbsolutePath());
        } catch (DateParseException e) {
            throw new RuntimeException("Error while parsing date in " + lastModifiedFile().getAbsolutePath(), e);
        }

        return lastModified;
    }

    /**
     * @param lastModified the lastModified to set
     * @throws IOException
     */
    public void setLastModified(Date lastModified) throws IOException {

        File lastModifiedFile = lastModifiedFile();
        File updateLogFile = updateLogFile();

        // write the timestamp as only content into the last modified file
        FileUtils.write(lastModifiedFile, DateUtil.formatDate(lastModified));

        // append a new line to the log file
        FileWriter logFileWriter = new FileWriter(updateLogFile);
        logFileWriter.append("\n").append(DateUtil.formatDate(lastModified));
        logFileWriter.close();

        // getLastModified() reads the date from the file
        this.lastModified = getLastModified();
        // test if file has been written correctly
        assert this.lastModified.equals(lastModified);
        logger.info(DateUtil.formatDate(lastModified) + " written to " + lastModifiedFile.getAbsolutePath());
    }

    /**
     * @return
     */
    private File lastModifiedFile() {
        return new File(storeLocation, "LAST_IMPORT_DATE");
    }

    private File updateLogFile() {
        return new File(storeLocation, "UPDATE.log");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String storeType() {
        return STORE_TYPE;
    }


}
