package org.cybertaxonomy.utis.client;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.query.IQueryClient;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.slf4j.Logger;

public abstract class AbstractClient<QC extends IQueryClient> {

    protected Logger logger;

    private ServiceProviderInfo spInfo;

    protected QC queryClient;

    /**
     * @param queryClient the queryClient to set
     */
    public abstract void initQueryClient();

    public AbstractClient() {
        initQueryClient();
        spInfo = buildServiceProviderInfo();
    }

    public AbstractClient(String checklistInfoJson) throws DRFChecklistException {
        initQueryClient();
        setChecklistInfo(JSONUtils.convertJsonToObject(checklistInfoJson, ServiceProviderInfo.class));
    }

    public AbstractClient(ServiceProviderInfo spInfo) throws DRFChecklistException {
        initQueryClient();
        setChecklistInfo(spInfo);
    }

    public abstract ServiceProviderInfo buildServiceProviderInfo();

    public ServiceProviderInfo getServiceProviderInfo() {
        return spInfo;
    }

    public String getChecklistInfoAsJson() throws DRFChecklistException {

        if(getServiceProviderInfo() != null) {
            return JSONUtils.convertObjectToJson(spInfo);
        }
        return null;
    }

    public void setChecklistInfo(ServiceProviderInfo checklistInfo) {
        this.spInfo = checklistInfo;
    }

    /**
     * Client implementations which retain any state information during
     * request processing in the client bean instance itself should return false.
     * Client bean which are not stateless must not be reused between multiple
     * requests. For each request a new bean must be instantiated.
     *
     * @return
     */
    public abstract boolean isStatelessClient();

}
