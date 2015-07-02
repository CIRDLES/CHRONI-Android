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

/*
 * This class parses Report Settings XML files.
 */
public class ReportSettingsParser {

	private static SortedMap<Integer, Category> categoryMap; // Collects the visible categories of a report settings
	private ArrayList<String> outputVariableNames = new ArrayList<String>();
    public static int columnCount = 0;
	public SortedMap<Integer, Category> runReportSettingsParser(String fileName){

		try {
			// Begins the parsing of the file
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			DomParser parser = new DomParser();
			Document doc = dBuilder.parse(fXmlFile);
			
			// hardcoded array of all the category names
			String[] categoryNames = { "fractionCategory",
					"compositionCategory", "isotopicRatiosCategory",
					"datesCategory", "rhosCategory", "fractionCategory2" };

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
				String categoryDisplayName = parser.getNodeValue("displayName",categoryNodes);
				String categoryVisibility = parser.getNodeValue("visible",categoryNodes);
				String categoryPosition = parser.getNodeValue("positionIndex",categoryNodes);

				if (categoryVisibility.equals("true")) {
					// Creates a new instance of a Category and puts it in the map if the category is visible
					Category visibleCategory = new Category(categoryDisplayName, Integer.parseInt(categoryPosition)); 
					categoryMap.put(Integer.parseInt(categoryPosition),	visibleCategory);
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
						String units = parser.getNodeValue("units", columnNodes);
						String methodName = parser.getNodeValue("retrieveMethodName", columnNodes);
						String variableName = parser.getNodeValue("retrieveVariableName", columnNodes);
						String countOfSignificantDigits = parser.getNodeValue("countOfSignificantDigits", columnNodes);
						String columnVisibility = parser.getNodeValue("visible", columnNodes);
						String positionIndex = parser.getNodeValue("positionIndex", columnNodes);
						String uncertaintyType = parser.getNodeValue("uncertaintyType", columnNodes);

                        if (columnVisibility.equals("true")) {
							// Instantiates a new Column and adds visible columns to the Category's Column map
							Column visibleColumn = new Column(categoryDisplayName, displayName1, displayName2, displayName3, units, methodName, variableName, Integer.parseInt(positionIndex), Integer.parseInt(countOfSignificantDigits));
                            columnCount++;
                            if(uncertaintyType.equals("ABS")|| uncertaintyType.equals("PCT")){
								visibleColumn.setUncertaintyType(uncertaintyType);
							}
							
							visibleCategory.getCategoryColumnMap().put(Integer.parseInt(positionIndex),visibleColumn);
							outputVariableNames.add(variableName);
//							occupyAbsentInfo(visibleColumn, displayName1,displayName2, displayName3, methodName, variableName);

							// UNCERTAINTY COLUMNS
							if (categoryName.contains("datesCategory")
									|| categoryName.contains("isotopicRatios")) {
								// determines if uncertainty column is visible
								NodeList uncertaintyColumnNodes = specificReportElement.getElementsByTagName("uncertaintyColumn");

								// iterate over all uncertainty columns in the visible category to find those that are visible
								if (uncertaintyColumnNodes.getLength() > 1) {
									Node specificUncertaintyNode = uncertaintyColumnNodes.item(0); // makes a node of the current uncertainty column
									NodeList specificUncertaintyColumnNodes = specificUncertaintyNode.getChildNodes(); // makes a NodeList of the nodes under the specific uncertainty column

									String uncertaintyName1 = parser.getNodeValue("displayName1",specificUncertaintyColumnNodes);
									String uncertaintyName2 = parser.getNodeValue("displayName2",specificUncertaintyColumnNodes);
									String uncertaintyName3 = parser.getNodeValue("displayName3", specificUncertaintyColumnNodes);
									String uncertaintyUnits = parser.getNodeValue("units", columnNodes);
									String uncertaintyMethodName = parser.getNodeValue("retrieveMethodName", specificUncertaintyColumnNodes);
									String uncertaintyVariableName = parser.getNodeValue("retrieveVariableName", specificUncertaintyColumnNodes);
									String uncertaintyCountOfSignificantDigits = parser.getNodeValue("countOfSignificantDigits", specificUncertaintyColumnNodes);
									String uncertaintyVisibility = parser.getNodeValue("visible", specificUncertaintyColumnNodes);
									String uncertaintyPositionIndex = parser.getNodeValue("positionIndex", specificUncertaintyColumnNodes);

									if (uncertaintyVisibility.contains("true")) {
										// Instantiates a new Column object if the column is visible and adds it to an Uncertainty Column Map
										Column visibleUncertaintyColumn = new Column(categoryDisplayName,uncertaintyName1,uncertaintyName2,	uncertaintyName3, uncertaintyUnits, uncertaintyMethodName, uncertaintyVariableName, Integer.parseInt(uncertaintyPositionIndex), Integer.parseInt(uncertaintyCountOfSignificantDigits));
                                        columnCount++;
                                        visibleColumn.setUncertaintyColumn(visibleUncertaintyColumn);
                                        visibleColumn.setUncertaintyColumn(true); // indicates that the column is an uncertainty column
										outputVariableNames.add(uncertaintyVariableName);
//										occupyAbsentInfo(visibleUncertaintyColumn,uncertaintyName1,uncertaintyName2,uncertaintyName3, uncertaintyMethodName, uncertaintyVariableName);
									} // Closes visible uncertainty column
								} // Closes loop through uncertainty columns
							} // Closes navigation through dates and isotopic ratios categories
                        } // Closes iterations through all visible columns
                    } // Closes going into visible category for columns
                    visibleCategory.setColumnCount(columnCount);
                } // Closes going into visible category
			} // Closes loop through file for category nodes
		} // Closes try statement
		catch (Exception e) {
			e.printStackTrace();
		}
	return categoryMap;
	} // Closes the main method
	
	/*
	 * Puts a hyphen where information is absent for display name
	 * @param displayName1-3 the various display names from the XML file to be
	 * checked
	 * @param visibleColumn the column the display names will be placed into
	 */
	public static void occupyAbsentInfo(Column visibleColumn, String displayName1, String displayName2, String displayName3, String methodName, String variableName){
	    if (displayName1.equals("")){
	    	displayName1 = "---";
	    	visibleColumn.setDisplayName1(displayName1);
	    }
	   if (displayName2.equals("")){
	    	displayName2 = "---";
	    	visibleColumn.setDisplayName2(displayName2);
	    }
	    if (displayName3.equals("")){
	    	displayName3 = "---";
	    	visibleColumn.setDisplayName3(displayName3);
	    }
	    if (methodName.equals("")){
	    	methodName = "---";
	    	visibleColumn.setMethodName(methodName);
	    }
	    if (variableName.equals("")){
	    	variableName = "---";
	    	visibleColumn.setVariableName(variableName);
	    }
	 }
	
	public ArrayList<String> getOutputVariableName() {
		return outputVariableNames;
	}

	public void setOutputVariableName(ArrayList<String> outputVariableName) {
		this.outputVariableNames = outputVariableName;
	}

	public static SortedMap<Integer, Category> getCategoryMap() {
		return categoryMap;
	}

	public static void setCategoryMap(SortedMap<Integer, Category> categoryMap) {
		ReportSettingsParser.categoryMap = categoryMap;
	}

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
}// Closes the class

