package org.cybertaxonomy.utis.occurrences;

import java.util.List;

import org.cybertaxonomy.utis.checklist.DRFChecklistException;
import org.cybertaxonomy.utis.client.AbstractClient;
import org.cybertaxonomy.utis.query.IQueryClient;
import org.cybertaxonomy.utis.utils.BiovelUtils;

public abstract class BaseOccurrencesClient<QC extends IQueryClient> extends AbstractClient<QC> {

	public String queryOccurrenceBank(List<String> nameids ) throws DRFChecklistException {
		StringBuilder sb = new StringBuilder();
		//String header = BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/occret/occ_csvHeader.txt","UTF-8");
		//sb.append(header);

		for(String nameid : nameids) {
			sb.append(getOccurrences(nameid));
		}

		return sb.toString();
	}

	public String queryOccurrenceBank(String nameid) throws DRFChecklistException {
		StringBuilder sb = new StringBuilder();
		//String header = BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/occret/occ_csvHeader.txt","UTF-8");
		//sb.append(header);

		sb.append(getOccurrences(nameid.trim()));

		return sb.toString();
	}

	public abstract String getOccurrences(String nameid) throws DRFChecklistException;

	public static String getOccCSVHeader() {
		return BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/occret/occ_csvHeader.txt","UTF-8");
	}

}
