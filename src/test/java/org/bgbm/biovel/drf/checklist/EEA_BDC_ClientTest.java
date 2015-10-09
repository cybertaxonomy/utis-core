package org.bgbm.biovel.drf.checklist;

import java.util.Iterator;
import java.util.List;

import org.bgbm.biovel.drf.input.DRFCSVInputParser;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class EEA_BDC_ClientTest {

    private static DRFCSVInputParser parser;

    @BeforeClass
    public static void  setup() {
        parser = new DRFCSVInputParser();
    }

    @Test
    public void scientificNameExactTest() throws DRFChecklistException {
        parser = new DRFCSVInputParser();

        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/eunis-scientificNameExact.csv","UTF-8"));

        EEA_BDC_Client client =  new EEA_BDC_Client();
        client.setChecklistInfo(client.buildServiceProviderInfo());

        Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
        while(tnrMsgItr.hasNext()) {
            TnrMsg tnrMsg = tnrMsgItr.next();
            TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameExact);
            client.queryChecklist(tnrMsg);
//            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
//            System.out.println(outputXML);
        }
    }

    @Test
    public void scientificNameLikeTest() throws DRFChecklistException, TnrMsgException {
        parser = new DRFCSVInputParser();

        List<TnrMsg> tnrMsgs = parser.parse(BiovelUtils.getResourceAsString("/org/bgbm/biovel/drf/tnr/eunis-scientificNameLike.csv","UTF-8"));

        EEA_BDC_Client client =  new EEA_BDC_Client();
        client.setChecklistInfo(client.buildServiceProviderInfo());

        Iterator<TnrMsg> tnrMsgItr = tnrMsgs.iterator();
        while(tnrMsgItr.hasNext()) {
            TnrMsg tnrMsg = tnrMsgItr.next();
            TnrMsgUtils.updateWithSearchMode(tnrMsg, SearchMode.scientificNameLike);
            client.queryChecklist(tnrMsg);
            String outputXML = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
            System.out.println(outputXML);
        }
    }
}

