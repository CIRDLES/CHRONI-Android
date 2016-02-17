package org.cirdles.chroni;

import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains methods for parsing an XML file.
 * @since October 29, 2012
 */
public class DomParser{
	
	/**
	 * This method returns the node with the specified tag name. 
	 * @param tagName this is a String variable that stores the specified tag name desired of a node
	 * @param nodes this is a NodeList variable that stores the nodes that need to be looped through to obtain the specific node to be parsed for information
	 * @return node if the node has the specific tag, it is returned
	 * @return null if nodes doesn't contain a node that matches the specific tagName, nothing is returned 
	 */
	protected Node getNode(String tagName, NodeList nodes) {
		for ( int x = 0; x < nodes.getLength(); x++ ) {
			Node node = nodes.item(x);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				return node;
			}
		}
		 return null;
	}
 
	/**
	 * This method returns the TEXT_NODE contained in a specific node.
	 * @param tagName this is a String variable that stores the specified tag name desired of a node
	 * @param nodes this is a NodeList variable that stores the nodes that need to be looped through to obtain the specific node to be parsed for information
	 * @return data.getNodeValue() returns the child TEXT_NODE of specified node.
	 * @return "" if node doesn't contain a TEXT_NODE, nothing is returned.
	 */
	protected String getNodeValue(String tagName, NodeList nodes ) {
		for ( int x = 0; x < nodes.getLength(); x++ ) {
			Node node = nodes.item(x);
			if (node.getNodeName().equalsIgnoreCase(tagName)) {
				NodeList childNodes = node.getChildNodes();
				for (int y = 0; y < childNodes.getLength(); y++ ) {
					Node data = childNodes.item(y);
					if ( data.getNodeType() == Node.TEXT_NODE )
						return data.getNodeValue();
				}
			}
		}
		return "";
	}

	/**
	 * Returns the Value Model with the name requested
	 * @param fraction the fraction to be parsed for the correct Value Model
	 * @param name the variable name the Value Model should have
	 * @return the Value Model with the specific name and null if no Value models contain the name
	 */
	protected static ValueModel getValueModelByName(Fraction fraction, String name) {

			for (Entry<String, ValueModel> specificValueModel : fraction.getValueModelMap().entrySet()) {
				String valueModelName = specificValueModel.getValue().getName();

				if (valueModelName.equals(name)){
					return specificValueModel.getValue();
				}
			}

		return null;
	}
    
}