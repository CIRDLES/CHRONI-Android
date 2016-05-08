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
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class parses Report Settings XML files.
 */
public class ReportSettingsParser {

	private static SortedMap<Integer, Category> categoryMap; // Collects the visible categories of a report settings
	private ArrayList<String> outputVariableNames = new ArrayList<String>();

	public SortedMap<Integer, Category> runReportSettingsParser(String fileName) {

		try {
			// Begins the parsing of the file
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			DomParser parser = new DomParser();
			Document doc = dBuilder.parse(fXmlFile);

			// gets the type of the XML based on the first node and continues only if it is ReportSettings
			if (doc.getDocumentElement().getNodeName().equals("ReportSettings")) {

				// hardcoded array of all the category names
				String[] categoryNames = { "fractionCategory",
						"compositionCategory", "isotopicRatiosCategory", "isotopicRatiosPbcCorrCategory",
						"datesCategory", "datesPbcCorrCategory", "rhosCategory", "traceElementsCategory", "fractionCategory2" };

				// Get the document's root XML nodes to begin parsing
				NodeList root = doc.getChildNodes();
				Node rootNode = parser.getNode("ReportSettings", root);
				NodeList rootNodes = rootNode.getChildNodes();

				// Instantiates the map needed to store the visible categories
				categoryMap = new TreeMap<Integer, Category>();

				for (String categoryName : categoryNames) {
					// Creates a NodeList of the child nodes under the individual category
					Node category = parser.getNode(categoryName, rootNodes);
					NodeList categoryNodes = category.getChildNodes();

					// Gets the Category information
					String categoryDisplayName = parser.getNodeValue("displayName", categoryNodes);
					String categoryVisibility = parser.getNodeValue("visible", categoryNodes);
					String categoryPosition = parser.getNodeValue("positionIndex", categoryNodes);

					if (categoryVisibility.equals("true")) {

						// Creates a new instance of a Category and puts it in the map if the category is visible
						Category visibleCategory = new Category(categoryDisplayName, Integer.parseInt(categoryPosition));
						categoryMap.put(Integer.parseInt(categoryPosition), visibleCategory);
						int columnCount = 0; // keeps track of columns in a visible category

						// BEGINS PARSING CATEGORIES FOR ACTUAL REPORT COLUMN INFO
						NodeList categoryElements = doc.getElementsByTagName(categoryName); // list of elements that contain category info

						Node specificCategory = categoryElements.item(0); // makes a node of the current category
						Element specificCategoryElement = (Element) specificCategory; // turns the node into an element
						NodeList reportColumnElements = specificCategoryElement.getElementsByTagName("ReportColumn"); // creates a NodeList of all the Report Columns under the specific category

						// iterate over all the report columns in the visible category
						for (int n = 0; n < reportColumnElements.getLength(); n++) {
							Node specificReportNode = reportColumnElements.item(n); // makes a node of the current report column
							Element specificReportElement = (Element) specificReportNode;
							NodeList columnNodes = specificReportNode.getChildNodes(); // list of nodes that contain the category information in the current report column

							// Specific XML information for Columns
							String displayName1 = parser.getNodeValue("displayName1", columnNodes);
							String displayName2 = parser.getNodeValue("displayName2", columnNodes);
							String displayName3 = parser.getNodeValue("displayName3", columnNodes);
							String units = parser.getNodeValue("units", columnNodes); // Used to calculate correct value for table
							String methodName = parser.getNodeValue("retrieveMethodName", columnNodes); // Specifies where to retrieve value model
							String variableName = parser.getNodeValue("retrieveVariableName", columnNodes); // Used to map appropriate value model to column
							String displayedWithArbitraryDigitCount = parser.getNodeValue("displayedWithArbitraryDigitCount", columnNodes); // Used to determine appropriate number of sigfigs
							String countOfSignificantDigits = parser.getNodeValue("countOfSignificantDigits", columnNodes);
							String columnVisibility = parser.getNodeValue("visible", columnNodes);
							String positionIndex = parser.getNodeValue("positionIndex", columnNodes);
							String uncertaintyType = parser.getNodeValue("uncertaintyType", columnNodes); // Used to figure out what calculation is needed for uncertainty column

							if (columnVisibility.equals("true")) {
								// Instantiates a new Column and adds visible columns to the Category's Column map
								Column visibleColumn = new Column(categoryDisplayName, displayName1, displayName2, displayName3, units, methodName, variableName, Integer.parseInt(positionIndex), Boolean.valueOf(displayedWithArbitraryDigitCount), Integer.parseInt(countOfSignificantDigits));
								columnCount++;
								if (uncertaintyType.equals("ABS") || uncertaintyType.equals("PCT")) {
									visibleColumn.setUncertaintyType(uncertaintyType);
								}

								visibleCategory.getCategoryColumnMap().put(Integer.parseInt(positionIndex), visibleColumn);
								outputVariableNames.add(variableName);

								// UNCERTAINTY COLUMNS
								if (categoryName.contains("datesCategory")
										|| categoryName.contains("isotopicRatios")) {
									// determines if uncertainty column is visible
									NodeList uncertaintyColumnNodes = specificReportElement.getElementsByTagName("uncertaintyColumn");

									// iterate over all uncertainty columns in the visible category to find those that are visible
									if (uncertaintyColumnNodes.getLength() > 1) {
										Node specificUncertaintyNode = uncertaintyColumnNodes.item(0); // makes a node of the current uncertainty column
										NodeList specificUncertaintyColumnNodes = specificUncertaintyNode.getChildNodes(); // makes a NodeList of the nodes under the specific uncertainty column

										String uncertaintyName1 = parser.getNodeValue("displayName1", specificUncertaintyColumnNodes);
										String uncertaintyName2 = parser.getNodeValue("displayName2", specificUncertaintyColumnNodes);
										String uncertaintyName3 = parser.getNodeValue("displayName3", specificUncertaintyColumnNodes);
										String uncertaintyUnits = parser.getNodeValue("units", specificUncertaintyColumnNodes); // Used to calculate correct value in column
										String uncertaintyMethodName = parser.getNodeValue("retrieveMethodName", specificUncertaintyColumnNodes); // Used to find group of value models
										String uncertaintyVariableName = parser.getNodeValue("retrieveVariableName", specificUncertaintyColumnNodes); // Used to find specific value model for that column
										String uncertaintyDisplayedWithArbitraryDigitCount = parser.getNodeValue("displayedWithArbitraryDigitCount", specificUncertaintyColumnNodes); // used to determine appropriate number of sigfigs
										String uncertaintyCountOfSignificantDigits = parser.getNodeValue("countOfSignificantDigits", specificUncertaintyColumnNodes);
										String uncertaintyVisibility = parser.getNodeValue("visible", specificUncertaintyColumnNodes);
										String uncertaintyPositionIndex = parser.getNodeValue("positionIndex", specificUncertaintyColumnNodes);

										if (uncertaintyVisibility.equals("true")) {

											// Instantiates a new Column object if the column is visible and adds it to an Uncertainty Column Map
											Column visibleUncertaintyColumn = new Column(categoryDisplayName, uncertaintyName1, uncertaintyName2, uncertaintyName3, uncertaintyUnits, uncertaintyMethodName, uncertaintyVariableName, Integer.parseInt(uncertaintyPositionIndex), Boolean.valueOf(uncertaintyDisplayedWithArbitraryDigitCount), Integer.parseInt(uncertaintyCountOfSignificantDigits));
											columnCount++;
											visibleColumn.setUncertaintyColumn(visibleUncertaintyColumn);
											visibleColumn.setUncertaintyColumn(true); // indicates that the column is an uncertainty column
											outputVariableNames.add(uncertaintyVariableName);
										}
									}

								}
							}

						}

						visibleCategory.setColumnCount(columnCount);
					}
				}

			} else	// if the XML file is invalid, return null
				return null;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return categoryMap;
	}
	
	public ArrayList<String> getOutputVariableNames() {
		return outputVariableNames;
	}

}

