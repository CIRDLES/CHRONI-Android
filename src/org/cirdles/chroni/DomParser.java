package org.cirdles.chroni;

import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/*
 * This class contains methods for parsing an XML file.
 * @since October 29, 2012
 */
public class DomParser{
	
	/*
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
 
/*
 * This method returns the TEXT_NODE contained in a specific node.
 * @param node this is a Node variable that stores the node with the desired text value.
 * @return data.getNodeValue() returns the child TEXT_NODE of specified node.
 * @return "" if node doesn't contain a TEXT_NODE, nothing is returned.
 */
protected String getNodeValue( Node node ) {
    NodeList childNodes = node.getChildNodes();
    for (int x = 0; x < childNodes.getLength(); x++ ) {
        Node data = childNodes.item(x);
        if ( data.getNodeType() == Node.TEXT_NODE )
            return data.getNodeValue();
    }
    return "";
}
 
/*
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
 
/*
 * This method returns the ATTRIBUTE_NODE contained in a specific node.
 * @param attrName this is a String variable that stores the specified attribute name desired of a node
 * @param nodes this is a NodeList variable that stores the nodes that need to be looped through to obtain the specific node to be parsed for information
 * @return attr.getNodeValue() returns the child ATTRIBUTE_NODE of specified node.
 * @return "" if node doesn't contain an ATTRIBUTE_NODE equal to attrName, nothing is returned.
 */
protected String getNodeAttr(String attrName, Node node ) {
    NamedNodeMap attrs = node.getAttributes();
    for (int y = 0; y < attrs.getLength(); y++ ) {
        Node attr = attrs.item(y);
        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
            return attr.getNodeValue();
        }
    }
    return "";
}
 
/*
 * This method returns the ATTRIBUTE_NODE contained in a specific node.
 * @param tagName this is a String variable that stores the specified tag name desired of a node
 * @param attrName this is a String variable that stores the specified attribute name desired of a node
 * @param nodes this is a NodeList variable that stores the nodes that need to be looped through to obtain the specific node to be parsed for information
 * @return data.getNodeValue() returns the child ATTRIBUTE_NODE of specified node.
 * @return "" if node doesn't contain an ATTRIBUTE_NODE, nothing is returned.
 */
protected String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
    for ( int x = 0; x < nodes.getLength(); x++ ) {
        Node node = nodes.item(x);
        if (node.getNodeName().equalsIgnoreCase(tagName)) {
            NodeList childNodes = node.getChildNodes();
            for (int y = 0; y < childNodes.getLength(); y++ ) {
                Node data = childNodes.item(y);
                if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
                    if ( data.getNodeName().equalsIgnoreCase(attrName) )
                        return data.getNodeValue();
                }
            }
        }
    }
 
    return "";
}

/*
 * Returns the Value Model with the name requested
 * @param fraction the fraction to be parsed for the correct Value Model
 * @param name the variable name the Value Model should have
 * @return the Value Model with the specific name and null if no Value models contain the name
 */
//protected static ValueModel getValueModelByName(Fraction fraction, String name){
//		Iterator<Entry<String, ValueModel>> valueModelIterator = fraction.getValueModelMap().entrySet().iterator();
//		while (valueModelIterator.hasNext()) {
//			Entry<String, ValueModel> specificValueModel = valueModelIterator.next();
//			String valueModelName = specificValueModel.getValue().getName();
//
//			if (valueModelName.equals(name)){
//				return specificValueModel.getValue();
//			}
//		}
//	return null;
//}

/*
 * Allows us to easily get the contents of a Node object
 */
protected String getTagValue(String sTag, Element eElement) {
	try {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		String solution = nValue.getNodeValue();
		return solution;
	} catch (Exception NullPointerException) {
		// spaces are printed if a null in found in any of the display name
	}
	return "";
}

/*
 * Rounds a number to a number of sig figs
 * @source http://stackoverflow.com/questions/202302/rounding-to-an-arbitrary-number-of-significant-digits
 */
public static double roundToSignificantFigures(double num, int n) {
    if(num == 0) {
        return 0;
    }

    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
    final int power = n - (int) d;

    final double magnitude = Math.pow(10, power);
    final long shifted = Math.round(num*magnitude);
    return shifted/magnitude;
}

/*
 * Puts spaces into the columns so they can be properly aligned. 
 * @param visibleColumn the column with the information to be aligned
 * @param visibleCategory the category with the information to be aligned    
 */
//protected static void alignColumn(Column visibleColumn, Category visibleCategory)   {
//    	int spaces = visibleColumn.getDisplayName().length();
//    	String displayName1 = visibleColumn.getDisplayName1();	    	
//    	String displayName2 = visibleColumn.getDisplayName2();
//    	String displayName3 = visibleColumn.getDisplayName3();
//    	int currentSpace1 = displayName1.length();
//    	int currentSpace2 = displayName2.length();
//    	int currentSpace3 = displayName3.length();
//    	
//		String categoryName = visibleColumn.getDisplayName();
//
//     	if(currentSpace1 < spaces){
//    		int spaceLeft = spaces - currentSpace1;
//    		for (int i = 0; i < spaceLeft; i++){
//    			displayName1 += " ";
//    		}	    	visibleColumn.setDisplayName1(displayName1);
//
//    	}
//    	
//     	if(currentSpace2 < spaces){
//    		int spaceLeft = spaces - currentSpace2;
//    		for (int i = 0; i < spaceLeft; i++){
//    			displayName2 += " ";
//    		}	    	visibleColumn.setDisplayName2(displayName2);
//
//    	}
//    	
//    	if(currentSpace3 < spaces){
//    		int spaceLeft = spaces - currentSpace3;
//    		for (int i = 0; i < spaceLeft; i++){
//    			displayName3 += " ";
//    		}	    	visibleColumn.setDisplayName3(displayName3);
//
//    	}
//    	
//    	if(spaces < currentSpace1){
//    		int spaceLeft = currentSpace1 - spaces;
//    		for (int i = 0; i < spaceLeft; i++){
//    			categoryName += " ";
//    			displayName2 += " ";
//    			displayName3 += " ";
//    			}	    	
//    		visibleCategory.setDisplayName(categoryName);
//    		visibleColumn.setDisplayName2(displayName2);
//    		visibleColumn.setDisplayName3(displayName3);
//    	}
//    	
//    	if(spaces < currentSpace2){
//    		int spaceLeft = currentSpace2 - spaces;
//    		for (int i = 0; i < spaceLeft; i++){
//    			categoryName += " ";
//    			displayName1 += " ";
//    			displayName3 += " ";
//    			}	    	
//    		visibleCategory.setDisplayName(categoryName);
//    		visibleColumn.setDisplayName1(displayName1);
//    		visibleColumn.setDisplayName3(displayName3);
//    	}
//    	
//    	if(spaces < currentSpace3){
//    		int spaceLeft = currentSpace3 - spaces;
//    		for (int i = 0; i < spaceLeft; i++){
//    			categoryName += " ";
//    			displayName1 += " ";
//    			displayName2 += " ";
//    			}	    	
//    		visibleCategory.setDisplayName(categoryName);
//    		visibleColumn.setDisplayName1(displayName1);
//    		visibleColumn.setDisplayName2(displayName2);
//    	}
//    	
//    }

}