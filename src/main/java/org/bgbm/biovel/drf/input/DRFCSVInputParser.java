/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bgbm.biovel.drf.input;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.bgbm.biovel.drf.tnr.msg.AtomisedName;
import org.bgbm.biovel.drf.tnr.msg.Query;
import org.bgbm.biovel.drf.tnr.msg.TaxonName;
import org.bgbm.biovel.drf.tnr.msg.TnrMsg;
import org.bgbm.biovel.drf.utils.BiovelUtils;
import org.bgbm.biovel.drf.utils.SynCheckConstants;
import org.bgbm.biovel.drf.utils.TnrMsgException;
import org.bgbm.biovel.drf.utils.TnrMsgUtils;
import org.gbif.nameparser.NameParser;
import org.w3c.dom.Node;

import au.com.bytecode.opencsv.CSVReader;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;


/**
 *
 * @author c.mathew
 */

public class DRFCSVInputParser {

    public enum MessageType {
        DOM_NODE,
        JSON
    }

    private List<TnrMsg> tnrMsgs = new ArrayList<TnrMsg>();

    private static StringBuilder incorrectRecordsBldr = new StringBuilder();

    public String getIncorrectRecords() {
        return incorrectRecordsBldr.toString();
    }

    public List<String> parseToJsonList(String csvData) throws DRFInputException {
        List<String> tnrMsgJsonList = new ArrayList<String>();
        List<TnrMsg> tnrRequests = parse(csvData);
        Iterator<TnrMsg> itr = tnrRequests.iterator();
        while(itr.hasNext()) {
            TnrMsg tnrMsg = itr.next();
            try {
                String json = TnrMsgUtils.convertTnrMsgToJson(tnrMsg);
                System.out.println(json);
                tnrMsgJsonList.add(json);
            } catch (JsonGenerationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new DRFInputException(e);
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new DRFInputException(e);
            }
        }
        return tnrMsgJsonList;
    }

    public List<Node> parseToDomNodes(String csvData) throws DRFInputException {
        List<Node> tnrMsgNodes = new ArrayList<Node>();
        List<TnrMsg> tnrRequests = parse(csvData);
        Iterator<TnrMsg> itr = tnrRequests.iterator();
        while(itr.hasNext()) {
            TnrMsg tnrMsg = itr.next();
            try {
                Node doc = TnrMsgUtils.convertTnrMsgToNode(tnrMsg);
                tnrMsgNodes.add(doc);

            } catch (JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new DRFInputException(e);
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new DRFInputException(e);
            }
        }
        return tnrMsgNodes;
    }

    public List<String> parseToXML(String csvData) throws DRFInputException {
        List<String> tnrMsgXMLList = new ArrayList<String>();
        List<TnrMsg> tnrRequests = parse(csvData);
        Iterator<TnrMsg> itr = tnrRequests.iterator();
        while(itr.hasNext()) {
            TnrMsg tnrMsg = itr.next();

            String xml;
            try {
                xml = TnrMsgUtils.convertTnrMsgToXML(tnrMsg);
                tnrMsgXMLList.add(xml);
            } catch (TnrMsgException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new DRFInputException(e);
            }
        }
        return tnrMsgXMLList;
    }

    public List<TnrMsg> parse(String csvData) {

        tnrMsgs.clear();

        int authorship_index = -1;
        int genus_part_index = -1;
        int infrageneric_epithet_index = -1;
        int specific_epithet_index = -1;
        int infraspecific_epithet_index = -1;
        int name_complete_index = -1;
        int uninomial_index = -1;
        int taxon_name_index = -1;

        int line_count = 0;
        int nbOfElements = 0;

        TaxonName taxonName;
        AtomisedName atomisedName;
        CSVReader reader;
        try {
            reader = new CSVReader(new StringReader(csvData));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length != 0) {
                    try {
                        List<String> headers = Arrays.asList(nextLine);

                        if (line_count == 0) {
                            if (BiovelUtils.containsWhitespace(headers)) {
                                throw new DRFInputException(
                                        "Header line contains empty fields which is not allowed");
                            }

                            nbOfElements = headers.size();

                            authorship_index = headers
                                    .indexOf(SynCheckConstants.AUTHORSHIP_HDR);

                            genus_part_index = headers
                                    .indexOf(SynCheckConstants.GENUSPART_HDR);

                            infrageneric_epithet_index = headers
                                    .indexOf(SynCheckConstants.INFRAGENERICEPITHET_HDR);

                            specific_epithet_index = headers
                                    .indexOf(SynCheckConstants.SPECIFICEPITHET_HDR);

                            infraspecific_epithet_index = headers
                                    .indexOf(SynCheckConstants.INFRASPECIFICEPITHET_HDR);

                            name_complete_index = headers
                                    .indexOf(SynCheckConstants.NAMECOMPLETE_HDR);

                            taxon_name_index = headers
                                    .indexOf(SynCheckConstants.TAXONNAME_HDR);

                        } else {
                            if (nextLine.length != nbOfElements) {
                                throw new DRFInputException(
                                        "No. of elements in record does not match no. of header elements");
                            }

                            taxonName = new TaxonName();

                            if (genus_part_index >= 0
                                    && !nextLine[genus_part_index].equals("")
                                    && ((infrageneric_epithet_index >= 0 && !nextLine[infrageneric_epithet_index]
                                            .equals(""))
                                            || (specific_epithet_index >= 0 && !nextLine[specific_epithet_index]
                                                    .equals("")) || (infraspecific_epithet_index >= 0 && !nextLine[infraspecific_epithet_index]
                                                            .equals("")))) {
                                atomisedName = new AtomisedName();
                                atomisedName.setGenusOrUninomial(nextLine[genus_part_index]);
                                atomisedName.setInfragenericEpithet((infrageneric_epithet_index >= 0) ? nextLine[infrageneric_epithet_index]
                                        : "");
                                atomisedName.setSpecificEpithet((specific_epithet_index >= 0) ? nextLine[specific_epithet_index]
                                        : "");
                                atomisedName.setInfraspecificEpithet((infrageneric_epithet_index >= 0) ? nextLine[infraspecific_epithet_index]
                                        : "");
                                taxonName.setAtomisedName(atomisedName);
                            } else if (uninomial_index >= 0
                                    && !nextLine[uninomial_index].equals("")) {
                                atomisedName = new AtomisedName();
                                atomisedName.setGenusOrUninomial(nextLine[uninomial_index]);
                                taxonName.setAtomisedName(atomisedName);
                            } else if (name_complete_index >= 0
                                    && !nextLine[name_complete_index]
                                            .equals("")) {
                                taxonName.setFullName(nextLine[name_complete_index]);
                                NameParser ecatParser = new NameParser();
                                String nameCanonical = ecatParser.parseToCanonical(taxonName.getFullName());
                                taxonName.setCanonicalName(nameCanonical);
                            } else if (taxon_name_index >= 0
                                    && !nextLine[taxon_name_index].equals("")) {
                                taxonName.setFullName(nextLine[taxon_name_index]);
                                NameParser ecatParser = new NameParser();
                                String nameCanonical = ecatParser.parseToCanonical(taxonName.getFullName());
                                taxonName.setCanonicalName(nameCanonical);
                            }
                            if (authorship_index >= 0
                                    && !nextLine[authorship_index].equals("")) {
                                taxonName.setAuthorship(nextLine[authorship_index]);
                            }

                            if(taxonName.getAtomisedName() != null || taxonName.getFullName() != null) {

                                Query.Request tnrRequest = new Query.Request();
                                tnrRequest.setName(taxonName.getFullName());

                                Query query = new Query();
                                query.setRequest(tnrRequest);

                                TnrMsg tnrMsg = new TnrMsg();
                                List<Query> queries = tnrMsg.getQuery();
                                queries.add(query);

                                tnrMsgs.add(tnrMsg);
                            }
                        }
                        line_count++;
                    } catch (DRFInputException drfie) {
                        String csvString = BiovelUtils.convertArrayToString(
                                nextLine, SynCheckConstants.DELIMITER);
                        incorrectRecordsBldr.append(csvString).append(":")
                        .append(drfie.getMessage()).append("\n");
                    }
                }
            }
            if(reader != null) {
                reader.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(DRFCSVInputParser.class.getName()).log(
                    Level.SEVERE, "", ex);
        }

        return tnrMsgs;
    }


}
