package org.bgbm.biovel.drf.client;

import org.bgbm.biovel.drf.checklist.DRFChecklistException;
import org.bgbm.biovel.drf.query.IQueryClient;
import org.bgbm.biovel.drf.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClient<QC extends IQueryClient> {

    protected Logger logger = LoggerFactory.getLogger(AbstractClient.class);

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

}
