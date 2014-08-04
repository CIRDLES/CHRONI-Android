package org.cirdles.chroni;

import java.util.SortedMap;

public class MapTuple {
	
	private final SortedMap<String, Fraction> fractionMap; // Collects the fractions in the Aliquot XML file
	private final SortedMap<String,Image> imageMap; // The given images in an aliquot
	  
	
	public MapTuple(SortedMap<String, Fraction> fractionMap, SortedMap<String,Image> imageMap){
		 this.fractionMap = fractionMap;
		 this.imageMap = imageMap;
	 }

	public SortedMap<String, Fraction> getFractionMap() {
		return fractionMap;
	}

	public SortedMap<String,Image> getImageMap() {
		return imageMap;
	}
	 
	}