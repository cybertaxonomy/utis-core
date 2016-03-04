package org.cybertaxonomy.utis.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.checklist.SearchMode;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class ServiceProviderInfo {

    private String id;
    private String label;
    private String documentationUrl;
    private String copyrightUrl;
    private String version;
    private String defaultClassificationId;
    private final List<ServiceProviderInfo> subChecklists = new ArrayList<ServiceProviderInfo>();
    protected EnumSet<SearchMode> searchModes;

    /**
     * [SearchMode.scientificNameExact]
     */
    public static final EnumSet<SearchMode> DEFAULT_SEARCH_MODE = EnumSet.of(SearchMode.scientificNameExact);

    public ServiceProviderInfo() {
    }

    /**
     * Constructor without <code>searchModes</code> parameter, the searchModes
     * will be set to the default: <code>[SearchMode.scientificNameExact]</code>
     * @param id
     * @param label
     * @param documentationUrl
     */
    public ServiceProviderInfo(String id, String label, String documentationUrl) {
        this(id,label,documentationUrl,"", DEFAULT_SEARCH_MODE);
    }

    /**
     *
     * @param id
     * @param label
     * @param documentationUrl
     * @param copyrightUrl
     * @param searchModes TODO
     */
    public ServiceProviderInfo(String id, String label, String documentationUrl, String copyrightUrl, EnumSet<SearchMode> searchModes) {
        this(id,label,DEFAULT_SEARCH_MODE,documentationUrl, copyrightUrl, "");
    }

    /**
     *
     * @param id
     * @param label
     * @param searchModes
     * @param documentationUrl
     * @param copyrightUrl
     * @param version
     */
    public ServiceProviderInfo(String id, String label, EnumSet<SearchMode> searchModes, String documentationUrl, String copyrightUrl, String version) {
        this.id = id;
        this.label = label;
        this.searchModes = searchModes;
        this.documentationUrl = documentationUrl;
        this.copyrightUrl = copyrightUrl;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public String getCopyrightUrl() {
        return copyrightUrl;
    }


    public String getVersion() {
        return version;
    }

    public void addSubChecklist(ServiceProviderInfo ci) {
        subChecklists.add(ci);
    }

    public List<ServiceProviderInfo> getSubChecklists() {
        if(subChecklists != null && subChecklists.size() > 0) {
            Collections.sort(subChecklists,new ServiceProviderInfoComparator());
        }
        return subChecklists;
    }

    public static ServiceProviderInfo create(String[] ciArray) throws DRFChecklistException {
        if(ciArray.length != 4) {
            throw new DRFChecklistException("Not correct number of elements to create Checklist Info");
        }
        return new ServiceProviderInfo(ciArray[0],ciArray[1],ciArray[2],ciArray[3], ServiceProviderInfo.DEFAULT_SEARCH_MODE);
    }

    public class ServiceProviderInfoComparator implements Comparator<ServiceProviderInfo> {
          @Override
          public int compare(ServiceProviderInfo spia, ServiceProviderInfo spib) {
              return spia.getLabel().compareTo(spib.getLabel());
          }

    }

    @Override
    public String toString(){
        return getId();
    }

    /**
     * @return the matchModes
     */
    @ApiModelProperty("Set of the different SearchModes supported by the service provider and client implementation."
            + "Possible search modes are: scientificNameExact, scientificNameLike, vernacularName")
    public EnumSet<SearchMode> getSearchModes() {
        return searchModes;
    }

    /**
     * @param matchModes the matchModes to set
     */
    public void setSearchModes(EnumSet<SearchMode> matchModes) {
        this.searchModes = matchModes;
    }

    /**
     * @return the defaultClassificationId
     */
    public String getDefaultClassificationId() {
        return defaultClassificationId;
    }

    /**
     * @param defaultClassificationId the defaultClassificationId to set
     */
    public void setDefaultClassificationId(String defaultClassificationId) {
        this.defaultClassificationId = defaultClassificationId;
    }

}