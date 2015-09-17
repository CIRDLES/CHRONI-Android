package org.cirdles.chroni;

import java.util.SortedMap;
import java.util.TreeMap;

/*
Method objects contain information about an Aliquot method.
 */
public class Method {

	private String variableName; // Variable name of a method
	private SortedMap<Float,ValueModel> valueModelMap = null; // Holds several value models associated with a method
	
	@Override
	public String toString() {
		return "This method has variable name: " + variableName;
	}

    /*
    Instantiates a method given a variable name
     */
	public Method(String variableName) {
		super();
		this.variableName = variableName;
		this.valueModelMap = new TreeMap<Float, ValueModel>();
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public SortedMap<Float, ValueModel> getValueModelMap() {
		return valueModelMap;
	}

	public void setValueModelMap(SortedMap<Float, ValueModel> valueModelMap) {
		this.valueModelMap = valueModelMap;
	}
}
