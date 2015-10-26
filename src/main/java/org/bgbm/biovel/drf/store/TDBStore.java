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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location;

/**
 * @author a.kohlbecker
 * @date Oct 19, 2015
 *
 */
public class TDBStore extends Store {

    private Dataset dataset = null;

    /**
     * @throws Exception
     */
    public TDBStore() throws Exception {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String storeName() {
        return "tdb";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initStoreEngine() throws Exception {

        Location location = Location.create(storeLocation.toString());
        Dataset dataset = TDBFactory.createDataset(location);
        dataset.begin(ReadWrite.READ) ;
        // Get model inside the transaction
        Model model = dataset.getDefaultModel() ;
        logger.info("Dataset in TDB has " + dataset.asDatasetGraph().size() + " named graphs");
        logger.info("Model-size: " + model.size());
        dataset.end();

    }

    /**
     * WARNING!!! This needs at least 1.5GB of heap space!!!
     * set -Xmx1500M
     *
     * NOTE: The bulkloader is a faster way to load data into an empty dataset than just using the Jena update operations.
     * the bulkloader also requires less memory
     * It is accessed through the command line utility tdbloader.
     *
     * rm /tmp/drf_tnb_store/*; bin/tdbloader2 -l /tmp/drf_tnb_store /tmp/species.rdf
     * @throws IOException
     */
    @Override
    protected void load(File rdfFile) throws IOException {

        logger.info("Using TDB store at " + storeLocation);

        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        // parse InputStream as RDF in Turtle format
        InputStream fin = new FileInputStream(rdfFile);
        logger.info("loading RDF/XML into TDB store");
        model.read(fin, null, "RDF/XML");
        logger.info("loading RDF/XML done");
        logger.info("Dataset in TDB has " + dataset.asDatasetGraph().size() + " named graphs");
        logger.info("Model-size: " + model.size());
        dataset.commit();
        dataset.end();
        logger.info("rdf loaded into TDB store at " + storeLocation);


        this.setDataset(dataset);
    }


    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }


    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopStoreEngine() throws Exception {
        // TODO Auto-generated method stub

    }

}
