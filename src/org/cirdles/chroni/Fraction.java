package org.cirdles.chroni;

import java.util.SortedMap;
import java.util.TreeMap;

/*
Structures the Fraction class for Aliquot file parsing.
 */
public class Fraction {

	private String fractionID;
	private SortedMap<String,ValueModel> valueModelMap = new TreeMap<String, ValueModel>();
	
	public Fraction(String fractionID){
		this.fractionID = fractionID;
	}

		public String getFractionID() {
		return fractionID;
	}

	public void setFractionID(String fractionID) {
		this.fractionID = fractionID;
	}


	public SortedMap<String,ValueModel> getValueModelMap() {
		return valueModelMap;
	}

	public void setValueModelMap(SortedMap<String, ValueModel> valueModelMap) {
		this.valueModelMap = valueModelMap;
	}


	@Override
	public String toString() {
		return "This fraction has fraction ID: " + fractionID + "." + "\n";
	}
}
