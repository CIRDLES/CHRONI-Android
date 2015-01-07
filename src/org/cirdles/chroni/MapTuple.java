package org.cirdles.chroni;

import java.util.SortedMap;

/*
Objects of this class are used to hold relevant mapping information used in parsing Aliquot and Report Settings files.
 */

public class MapTuple {

    private SortedMap<Integer, Category> categoryMap ; // Collects the categories in the Report Settings XML file
    private SortedMap<String, Fraction> fractionMap ; // Collects the fractions in the Aliquot XML file
	private SortedMap<String,Image> imageMap; // The given images in an aliquot
	  
//    /*
//    * Creates a MapTuple for Report Settings files
//     */
//    public MapTuple(SortedMap<Integer, Category> categoryMap){
//        this.setCategoryMap(categoryMap);
//    }

    /*
    * Creates a MapTuple object for Aliquot files using fractions and images.
     */
	public MapTuple(SortedMap<String, Fraction> fractionMap, SortedMap<String,Image> imageMap){
		 this.setFractionMap(fractionMap);
		 this.setImageMap(imageMap);
	 }

	public SortedMap<String, Fraction> getFractionMap() {
		return fractionMap;
	}

	public SortedMap<String,Image> getImageMap() {
		return imageMap;
	}

    public SortedMap<Integer, Category> getCategoryMap() {
        return categoryMap;
    }

    public void setCategoryMap(SortedMap<Integer, Category> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public void setFractionMap(SortedMap<String, Fraction> fractionMap) {
        this.fractionMap = fractionMap;
    }

    public void setImageMap(SortedMap<String, Image> imageMap) {
        this.imageMap = imageMap;
    }
}