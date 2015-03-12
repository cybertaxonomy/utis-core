package org.bgbm.biovel.drf.checklist;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bgbm.biovel.drf.rest.ServiceProviderInfo;
import org.bgbm.biovel.drf.rest.TaxoRESTClient;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.Query.Request;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
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
            Request tnrRequest = query.getRequest();
            String queryString = tnrRequest.getName();
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
        return buildUriFromQueryString(query.getRequest().getName(),
                endpointSuffix,
                queryKey,
                paramMap);
    }

    public URI buildUriFromQuery(Query query,
            String regexpUrl,
            Map<String, String> paramMap) {
        String url = regexpUrl.replace(QUERY_PLACEHOLDER, query.getRequest().getName());
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

    public void queryChecklist(List<TnrMsg> tnrMsgs) throws DRFChecklistException {

        TnrMsg finalTnrMsg = new TnrMsg();
        Iterator<TnrMsg> itrTnrMsg = tnrMsgs.iterator();
        while(itrTnrMsg.hasNext()) {
            Iterator<Query> itrQuery = itrTnrMsg.next().getQuery().iterator();
            while(itrQuery.hasNext()) {
                finalTnrMsg.getQuery().add(itrQuery.next());
            }
        }

        queryChecklist(finalTnrMsg);
    }

    /**
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     *
     * TODO remove parameter SearchMode, since it in now included in the TnrMsg.query.request
     */
    public void queryChecklist(TnrMsg tnrMsg) throws DRFChecklistException {

//        TnrMsgUtils.updateWithSearchMode(tnrMsg, mode); // ...... remove
        TnrMsgUtils.assertSearchModeSet(tnrMsg, true);

        SearchMode mode = TnrMsgUtils.getSearchMode(tnrMsg);

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
            case findByIdentifier:
                if(checkSupportedIdentifieres(tnrMsg)){
                    findByIdentifier(tnrMsg);
                } else {
                    logger.info("The queries contain unsupported identifier strings");
                    throw new UnsupportedIdentifierException("Queries contain unsupported identifier strings");
                }
                break;
            default:
                throw new DRFChecklistException("Unimplemented SearchMode");
            }
        } else {
            logger.info("Search mode " + mode + " not supported by this ChecklistClient implementation");
        }
    }

    /**
     * Checks all the queries of the <code>tnrMsg</code> of they
     * contain a query string which is supported as identifier by the
     * implementing checklist client.
     * <p>
     * Fails if only one of the query string is not supported!
     *
     * @param tnrMsg
     * @return
     */
    private boolean checkSupportedIdentifieres(TnrMsg tnrMsg) {
        for (Query q : tnrMsg.getQuery()){
            if(! isSupportedIdentifier(q.getRequest().getName())){
                return false;
            }
        }
        return true;
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

    /**
     * Searches taxa having a specific identifier
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void  findByIdentifier(TnrMsg tnrMsg) throws DRFChecklistException;

    public abstract EnumSet<SearchMode> getSearchModes();

    public abstract boolean isSupportedIdentifier(String value);

}
