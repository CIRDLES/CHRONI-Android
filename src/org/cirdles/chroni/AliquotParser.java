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

/*
 * This class parses Aliquot XML files.
 */

public class AliquotParser {

	private static String fileName; // Aliquot file to be parsed
	private static String aliquotName; // the Aliquot name

	private static SortedMap<String, Fraction> fractionMap; // Collects the fractions in the Aliquot XML file
	private static SortedMap<String,Image> imageMap; // The given images in an aliquot
	private static MapTuple aliquotMaps; // Contains the completed Aliquot Maps

	public static MapTuple runAliquotParser(String fileName){

		try {
			// Begins the parsing of the file
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			DomParser parser = new DomParser();
			Document doc = dBuilder.parse(fXmlFile);
		
			// Get the document's root XML nodes to get aliquot name for file
			NodeList root = doc.getChildNodes();
			Node rootNode = parser.getNode("Aliquot", root);
			NodeList rootNodes = rootNode.getChildNodes();
			aliquotName = parser.getNodeValue("aliquotName", rootNodes);
								
			// Gets the document's image data
			imageMap = new TreeMap<String, Image>();
			NodeList analysisImages = doc.getElementsByTagName("AnalysisImage"); // Creates a list of all different images
			for(int n = 0; n < analysisImages.getLength(); n++){
				// Creates a NodeList of the data found under the specific Image column
				Node analysisImage = analysisImages.item(n);
				NodeList analysisImageNodes = analysisImage.getChildNodes();
				String imageType = parser.getNodeValue("imageType",analysisImageNodes);
				String imageUrl = parser.getNodeValue("imageURL",analysisImageNodes);
				Image image = new Image(imageType, imageUrl);
				imageMap.put(imageType, image);
			}
			
			// Creates a NodeList of all the Analysis Fraction columns within the "analysisFractions" node 
			NodeList analysisFractionColumns = doc.getElementsByTagName("AnalysisFraction");			
			
			// Instantiates the map needed to store the visible categories
			fractionMap = new TreeMap<String, Fraction>();
			
			// Loops through each Analysis Fraction Column to gather information
			for(int i = 0; i < analysisFractionColumns.getLength(); i++){
				//Creates a NodeList of the data found under the specific Analysis Fraction column 
				Node analysisFraction = analysisFractionColumns.item(i);
				Element specificAnalysisFraction = (Element) analysisFraction;
				
				NodeList analysisFractionNodes = analysisFraction.getChildNodes(); 
								
				String fractionID = parser.getNodeValue("fractionID",analysisFractionNodes);
				
				// Creates a new Fraction with the information parsed from the XML file and puts it in the map
				Fraction newFraction = new Fraction(fractionID);
				fractionMap.put(fractionID, newFraction);	
								
//				Gets all the value models in the specific fraction
				NodeList fractionValueModels = specificAnalysisFraction.getElementsByTagName("ValueModel");

				for(int m = 0; m < fractionValueModels.getLength(); m++){
					Node valueModelNode = fractionValueModels.item(m); // makes a node of the current Value Model
					Element specificValueModel = (Element) valueModelNode;
					NodeList valueModelNodes = specificValueModel.getChildNodes(); // list of nodes that contain the information in the current Value Model
								
					String name = parser.getNodeValue("name",valueModelNodes);
					String value = parser.getNodeValue("value", valueModelNodes);
					String uncertaintyType = parser.getNodeValue("uncertaintyType",valueModelNodes);
					String oneSigma = parser.getNodeValue("oneSigma", valueModelNodes);
				
					ValueModel valueModel = new ValueModel(name, Float.parseFloat(value), uncertaintyType, Float.parseFloat(oneSigma));
					newFraction.getValueModelMap().put(valueModel.getName(), valueModel);
					}			
			}			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Creates the MapTuple object containing both maps
        aliquotMaps = new MapTuple(fractionMap, imageMap);
		return aliquotMaps;
	} // Closes the parsing aliquot method
		
	public static String getAliquotName() {
		return aliquotName;
	}


	public static SortedMap<String, Fraction> getFractionMap() {
		return fractionMap;
	}

	public static void setFractionMap(SortedMap<String, Fraction> fractionMap) {
		AliquotParser.fractionMap = fractionMap;
	}
	
	public static SortedMap<String,Image> getImageMap() {
		return imageMap;
	}

	public static void setImageMap(SortedMap<String,Image> imageMap) {
		AliquotParser.imageMap = imageMap;
	}

	public static void setAliquotName(String aliquotName) {
		AliquotParser.aliquotName = aliquotName;
	}

	public static void setAliquotFile(String fileName) {
		AliquotParser.fileName = fileName;
	}

	public static String getAliquotFile(String aliquotLocation) {
		// TODO Auto-generated method stub
		return fileName;
	}
	
}// Closes the class