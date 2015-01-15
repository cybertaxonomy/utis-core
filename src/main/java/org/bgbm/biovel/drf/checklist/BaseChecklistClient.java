package org.bgbm.biovel.drf.checklist;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.rest.TaxoRESTClient;
import org.bgbm.biovel.drf.tnr.msg.Query.TnrRequest;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseChecklistClient extends TaxoRESTClient {

    protected Logger logger = LoggerFactory.getLogger(BaseChecklistClient.class);

    public final static String QUERY_PLACEHOLDER = "{q}";

    protected final static String CHECKLIST_KEY = "checklist";
    protected final static String CHECKLIST_URL_KEY = "checklist_url";
    protected final static String COPYRIGHT_URL_KEY = "copyright_url";
    protected final static String CHECKLIST_LIST = "checklist_list";

    public BaseChecklistClient() {
        super();
    }

    public BaseChecklistClient(String checklistInfoJson) throws DRFChecklistException {
        super(checklistInfoJson);
    }

    public BaseChecklistClient(ServiceProviderInfo spInfo) throws DRFChecklistException {
        super(spInfo);
    }

    public void queryChecklist(TnrMsg tnrMsg, SearchMode searchMode) throws DRFChecklistException {
        resolveNames(tnrMsg, searchMode);
    }

    public TnrMsg queryChecklist(List<TnrMsg> tnrMsgs) throws DRFChecklistException {

        TnrMsg finalTnrMsg = new TnrMsg();
        Iterator<TnrMsg> itrTnrMsg = tnrMsgs.iterator();
        while(itrTnrMsg.hasNext()) {
            Iterator<Query> itrQuery = itrTnrMsg.next().getQuery().iterator();
            while(itrQuery.hasNext()) {
                finalTnrMsg.getQuery().add(itrQuery.next());
            }
        }

        resolveScientificNamesExact(finalTnrMsg);

        return finalTnrMsg;
    }

    /**
     *
     * @param queryList
     * @param endpointSuffix
     * @param queryKey
     * @param likeModeWildcard the wildcard to add to the query string in case of like search modes
     * @param paramMap
     * @return
     */
    public URI buildUriFromQueryList(List<Query> queryList,
            String endpointSuffix,
            String queryKey,
            String likeModeWildcard,
            Map<String, String> paramMap) {

        List<String> queries = new ArrayList<String>();

        EnumSet<SearchMode> likeModes = EnumSet.of(SearchMode.scientificNameLike);

        for(Query query : queryList) {
            TnrRequest tnrRequest = query.getTnrRequest();
            String queryString = tnrRequest.getTaxonName().getFullName();
            if(likeModes.contains(SearchMode.valueOf(tnrRequest.getSearchMode()))){
                queryString += likeModeWildcard;
            }
            queries.add(queryString);
        }

        logger.debug("Query size : " + queries.size());

        return buildUriFromQueryStringList(queries,
                endpointSuffix,
                queryKey,
                paramMap);
    }

    public URI buildUriFromQuery(Query query,
            String endpointSuffix,
            String queryKey,
            Map<String, String> paramMap) {
        return buildUriFromQueryString(query.getTnrRequest().getTaxonName().getFullName(),
                endpointSuffix,
                queryKey,
                paramMap);
    }

    public URI buildUriFromQuery(Query query,
            String regexpUrl,
            Map<String, String> paramMap) {
        String url = regexpUrl.replace(QUERY_PLACEHOLDER, query.getTnrRequest().getTaxonName().getFullName());
        return buildUriFromQueryString(url, paramMap);
    }


    /**
     * @param tnrMsg
     * @return
     * @throws DRFChecklistException
     */
    protected Query singleQueryFrom(TnrMsg tnrMsg) throws DRFChecklistException {
        List<Query> queryList = tnrMsg.getQuery();
        if(queryList.size() ==  0) {
            throw new DRFChecklistException("query list is empty");
        }

        if(queryList.size() > 1) {
            throw new DRFChecklistException("query list has more than one query");
        }
        Query query = queryList.get(0);
        return query;
    }

    public void resolveNames(TnrMsg tnrMsg, SearchMode mode) throws DRFChecklistException {

        if(!getSearchModes().contains(mode)){
            throw new DRFChecklistException("Unsupported SearchMode");
        }
        if (getSearchModes().contains(mode)){
            switch(mode){
            case scientificNameExact:
                 resolveScientificNamesExact(tnrMsg);
                 break;
            case scientificNameLike:
                resolveScientificNamesLike(tnrMsg);
                break;
            case vernacularNameExact:
                resolveVernacularNamesExact(tnrMsg);
                break;
            case vernacularNameLike:
                resolveVernacularNamesLike(tnrMsg);
                break;
            default:
                throw new DRFChecklistException("Unimplemented SearchMode");
            }
        } else {
            logger.info("Search mode " + mode + " not supported by this ChecklistClient implementation");
        }
    }

    /**
     * Searches for scientific names which exactly match the given query string.
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void resolveScientificNamesExact(TnrMsg tnrMsg) throws DRFChecklistException;

    /**
     * Searches for scientific names which start with the given query string.
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void resolveScientificNamesLike(TnrMsg tnrMsg) throws DRFChecklistException;

    /**
     * Searches for taxa with an vernacular name that exactly match the given query string.
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void resolveVernacularNamesExact(TnrMsg tnrMsg) throws DRFChecklistException;

    /**
     * Searches for taxa with an vernacular name that contains the given query string.
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void resolveVernacularNamesLike(TnrMsg tnrMsg) throws DRFChecklistException;

    public abstract EnumSet<SearchMode> getSearchModes();



}
