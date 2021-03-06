/*
 * Copyright 2016 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.chroni;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class parses Aliquot XML files.
 */

public class AliquotParser {

	private static String aliquotName; // the Aliquot name

	private static SortedMap<String, Fraction> fractionMap; // Collects the fractions in the Aliquot XML file
	private static SortedMap<String,Image> imageMap; // The given images in an aliquot

	public static MapTuple runAliquotParser(String fileName){

		try {
			// Begins the parsing of the file
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			DomParser parser = new DomParser();
			Document doc = dBuilder.parse(fXmlFile);

			// gets the type of the XML based on the first node and continues only if it is Aliquot
			if (doc.getDocumentElement().getNodeName().equals("Aliquot")) {

				// Get the document's root XML nodes to get aliquot name for file
				NodeList root = doc.getChildNodes();
				Node rootNode = parser.getNode("Aliquot", root);
				NodeList rootNodes = rootNode.getChildNodes();
				aliquotName = parser.getNodeValue("aliquotName", rootNodes);

				// Gets the document's image data
				imageMap = new TreeMap<String, Image>();
				NodeList analysisImages = doc.getElementsByTagName("AnalysisImage"); // Creates a list of all different images
				for (int n = 0; n < analysisImages.getLength(); n++) {
					// Creates a NodeList of the data found under the specific Image column
					Node analysisImage = analysisImages.item(n);
					NodeList analysisImageNodes = analysisImage.getChildNodes();
					String imageType = parser.getNodeValue("imageType", analysisImageNodes);
					String imageUrl = parser.getNodeValue("imageURL", analysisImageNodes);
					Image image = new Image(imageType, imageUrl);
					imageMap.put(imageType, image);
				}

				// Creates a NodeList of all the Analysis Fraction columns within the "analysisFractions" node
				NodeList analysisFractionColumns = doc.getElementsByTagName("AnalysisFraction");

				// Instantiates the map needed to store the visible categories
				fractionMap = new TreeMap<String, Fraction>();

				// Loops through each Analysis Fraction Column to gather information
				for (int i = 0; i < analysisFractionColumns.getLength(); i++) {
					//Creates a NodeList of the data found under the specific Analysis Fraction column
					Node analysisFraction = analysisFractionColumns.item(i);
					Element specificAnalysisFraction = (Element) analysisFraction;

					NodeList analysisFractionNodes = analysisFraction.getChildNodes();

					// Collects the overall info about the fraction necessary for parsing
					String fractionID = parser.getNodeValue("fractionID", analysisFractionNodes);

					String numberOfGrains = parser.getNodeValue("numberOfGrains", analysisFractionNodes);


					// Creates a new Fraction with the information parsed from the XML file and puts it in the map
					Fraction newFraction = new Fraction(fractionID, numberOfGrains);
					fractionMap.put(fractionID, newFraction);

					// Gets all the value models in the specific fraction
					NodeList fractionValueModels = specificAnalysisFraction.getElementsByTagName("ValueModel");

					for (int m = 0; m < fractionValueModels.getLength(); m++) {
						Node valueModelNode = fractionValueModels.item(m); // makes a node of the current Value Model
						Element specificValueModel = (Element) valueModelNode;
						NodeList valueModelNodes = specificValueModel.getChildNodes(); // list of nodes that contain the information in the current Value Model

						String name = parser.getNodeValue("name", valueModelNodes); // Used to link to a column's variable name
						String value = parser.getNodeValue("value", valueModelNodes); // Used to display numerical value of normal columns
						String uncertaintyType = parser.getNodeValue("uncertaintyType", valueModelNodes); // Used to determine how to calculate uncertainty value
						String oneSigma = parser.getNodeValue("oneSigma", valueModelNodes); // Used to display numerical value of uncertainty column

						// Creates a new value model for the fraction
						// Every fraction has several value models used to build the table
						ValueModel valueModel = new ValueModel(name, Float.parseFloat(value), uncertaintyType, Float.parseFloat(oneSigma));
						newFraction.getValueModelMap().put(valueModel.getName(), valueModel);
					}
				}

			} else	// if the XML file is invalid, return null
				return null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Returns the MapTuple object containing both maps
		return new MapTuple(fractionMap, imageMap);

	}
		
	public static String getAliquotName() {
		return aliquotName;
	}
	
}