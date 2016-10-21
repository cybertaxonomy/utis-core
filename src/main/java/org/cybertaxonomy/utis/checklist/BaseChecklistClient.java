package org.cybertaxonomy.utis.checklist;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.cybertaxonomy.utis.client.AbstractClient;
import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.query.IQueryClient;
import org.cybertaxonomy.utis.tnr.msg.Query;
import org.cybertaxonomy.utis.tnr.msg.TnrMsg;
import org.cybertaxonomy.utis.utils.TnrMsgUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseChecklistClient<QC extends IQueryClient> extends AbstractClient<QC> {

    private static final Logger logger = LoggerFactory.getLogger(BaseChecklistClient.class);

    protected final static String CHECKLIST_KEY = "checklist";
    protected final static String CHECKLIST_URL_KEY = "checklist_url";
    protected final static String COPYRIGHT_URL_KEY = "copyright_url";
    protected final static String CHECKLIST_LIST = "checklist_list";

    /**
     * The maximum amount of records to be returned by a checklist client
     */
    protected static final String MAX_HITS = "500";

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
     */
    public void queryChecklist(TnrMsg tnrMsg) throws DRFChecklistException {

        TnrMsgUtils.assertSearchModeSet(tnrMsg, true);

        UtisAction mode = TnrMsgUtils.getUtisAction(tnrMsg);

        preExcuteQuery(tnrMsg);

        if(!isSupportedAction(mode)){
            logger.error("Utis Action " + mode + " not supported by the ChecklistClient implementation " + this.getClass().getSimpleName());
            throw new DRFChecklistException("Unsupported Utis Action " + mode);
        }

        if (getSearchModes().contains(mode)){
            switch((SearchMode)mode){
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
               throw new DRFChecklistException("Unimplemented SearchMode: " + mode);

            }
        } else if (getClassificationActions().contains(mode)){
            switch ((ClassificationAction)mode) {
            case taxonomicChildren:
                if(checkSupportedIdentifieres(tnrMsg)){
                    taxonomicChildren(tnrMsg);
                } else {
                    logger.info("The queries contain unsupported identifier strings");
                    throw new UnsupportedIdentifierException("Queries contain unsupported identifier strings");
                }
                break;
            case higherClassification:
                if(checkSupportedIdentifieres(tnrMsg)){
                    higherClassification(tnrMsg);
                } else {
                    logger.info("The queries contain unsupported identifier strings");
                    throw new UnsupportedIdentifierException("Queries contain unsupported identifier strings");
                }
                break;
            default:
                throw new DRFChecklistException("Unimplemented ClassificationAction " + mode);
            }
        } else {
            throw new DRFChecklistException("Unknown Utis Action type " + mode);
        }

        postExcuteQuery(tnrMsg);
    }

    /**
     * @param tnrMsg
     */
    private void postExcuteQuery(TnrMsg tnrMsg) {
     // empty stub, to be implemented by subclasses if necessary
    }

    /**
     * @param tnrMsg
     */
    private void preExcuteQuery(TnrMsg tnrMsg) {
        // empty stub, to be implemented by subclasses if necessary
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
            if(! isSupportedIdentifier(q.getRequest().getQueryString())){
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

    /**
     * Lists the taxonomic children of taxa having a specific identifier
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void  taxonomicChildren(TnrMsg tnrMsg) throws DRFChecklistException;

    /**
     * Lists the taxonomic children of taxa having a specific identifier
     *
     * @param tnrMsg
     * @throws DRFChecklistException
     */
    public abstract void  higherClassification(TnrMsg tnrMsg) throws DRFChecklistException;


    public boolean isSupportedAction(UtisAction action) {
        if(action == null) {
            throw new NullPointerException("Parameter action must not be NULL");
        }
        return getSearchModes().contains(action) || getClassificationActions().contains(action);

    }

    public abstract EnumSet<SearchMode> getSearchModes();

    public abstract EnumSet<ClassificationAction> getClassificationActions();

    public abstract boolean isSupportedIdentifier(String value);

}
