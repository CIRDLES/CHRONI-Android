package org.cirdles.chroni;

import java.util.SortedMap;
import java.util.TreeMap;

public class Method {

	private String variableName;
	private SortedMap<Float,ValueModel> valueModelMap = null;
	
	@Override
	public String toString() {
		return "This method has variable name: " + variableName;
	}

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
