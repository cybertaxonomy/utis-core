package org.cybertaxonomy.utis.checklist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cybertaxonomy.utis.client.ServiceProviderInfo;
import org.cybertaxonomy.utis.utils.JSONUtils;
import org.cybertaxonomy.utis.utils.ServiceProviderInfoUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // TODO client not officially supported in utis + test is causing problems: please check
public class ChecklistTest {

	@BeforeClass
	public static void  setup() {
	}

	@Test
	public void generateChecklistInfoList() throws DRFChecklistException {
		String checklistInfoListJson = ServiceProviderInfoUtils.generateChecklistInfoListAsJson();
		System.out.println("Json : " + checklistInfoListJson);
	}

	@Test
	public void generateChecklistInfoListFromStringArray() throws DRFChecklistException {
		List<String> ciList = new ArrayList<String>();
		ciList.add("species2000col;species2000col;Species2000 - Catalogue Of Life;http://www.catalogueoflife.org;http://www.catalogueoflife.org/col/info/copyright");
		ciList.add("edit;col;EDIT - Catalogue Of Life;http://wp5.e-taxonomy.eu/cdmlib/rest-api-name-catalogue.html;http://www.catalogueoflife.org/col/info/copyright");
		ciList.add("gbif;1028;Afromoths, online datbase of Afrotropical moth species (Lepidoptera);http://ecat-dev.gbif.org/checklist/1028;");
		ciList.add("gbif;1000;Afromoths, online datbase of Afrotropical moth species (Lepidoptera);http://ecat-dev.gbif.org/checklist/1000;");

		List<ServiceProviderInfo> ciInfoList = ServiceProviderInfoUtils.convertStringToChecklistInfo(ciList);
		Iterator<ServiceProviderInfo> ciInfoItr = ciInfoList.iterator();
		while(ciInfoItr.hasNext()) {
			System.out.println(JSONUtils.convertObjectToJson(ciInfoItr.next()));
		}

	}


}

