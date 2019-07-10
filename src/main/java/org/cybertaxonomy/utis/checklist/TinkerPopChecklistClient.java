/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.cybertaxonomy.utis.checklist;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.TinkerPopClient;
import org.cybertaxonomy.utis.tnr.msg.NameType;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.Query.Request;
import org.cybertaxonomy.utis.tnr.msg.Response;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Vertex;

/**
 * @author a.kohlbecker
 * @date Nov 16, 2016
 *
 */
public abstract class TinkerPopChecklistClient extends AggregateChecklistClient<TinkerPopClient> {

    protected static final Logger logger = LoggerFactory.getLogger(EUNIS_Client.class);

    /**
     * @param checklistInfoJson
     * @throws DRFChecklistException
     */
    public TinkerPopChecklistClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    /**
     *
     */
    public TinkerPopChecklistClient() {
        super();
    }


    void updateQueriesWithResponse(List<Vertex> taxonNodes, List<Vertex> matchNodes, NameType matchType, ServiceProviderInfo ci, Query query){

        if (taxonNodes == null) {
            return;
        }

        logger.debug("matching taxon nodes:");
        int i = -1;
        for (Vertex v : taxonNodes) {
            i++;
            logger.debug("  " + v.toString());
            if(logger.isTraceEnabled()) {
                logger.trace("updateQueriesWithResponse() : printing propertyKeys to System.err");
                printPropertyKeys(v, System.err);
            }
            if(!v.getProperty("kind").equals("uri")) {
                logger.error("vertex of type 'uri' expected, but was " + v.getProperty("type").toString());
                continue;
            }
            Vertex matchNode = null;
            if(matchNodes != null) {
                matchNode = matchNodes.get(i);
            }
            Response tnrResponse = tnrResponseFromResource(v, query.getRequest(), matchNode, matchType, ci, false);
            if(tnrResponse != null) {
                query.getResponse().add(tnrResponse);
            }
        }
    }

    protected void printPropertyKeys(Vertex v, PrintStream ps) {
        StringBuilder out = new StringBuilder();
        out.append(v.toString());
        for(String key : v.getPropertyKeys()) {
            out.append(key).append(": ").append(v.getProperty(key)).append(" ");
        }
        ps.println(out.toString());
    }

    /**
     * @param vertex
     */
    protected void printEdges(Neo4j2Vertex vertex) {
        Iterable<Relationship> rels = vertex.getRawVertex().getRelationships();
        Iterator<Relationship> iterator = rels.iterator();
        if(iterator.hasNext()) {
            Relationship rel = iterator.next();
            System.err.println(rel.toString() + ": " + rel.getStartNode().toString() + "-[" +  rel.getType() + "]-" + rel.getEndNode().toString());
        }
    }



    /**
     * @param v
     * @param request
     * @param matchNode
     * @param matchType
     * @param ci
     * @param b
     * @return
     */
    protected abstract Response tnrResponseFromResource(Vertex v, Request request, Vertex matchNode, NameType matchType,
            ServiceProviderInfo ci, boolean b);



}
