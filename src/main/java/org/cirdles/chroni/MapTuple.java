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

import java.util.SortedMap;

/**
 * Objects of this class are used to hold relevant mapping information used in parsing Aliquot and Report Settings files.
 */

public class MapTuple {

    private SortedMap<Integer, Category> categoryMap ; // Collects the categories in the Report Settings XML file
    private SortedMap<String, Fraction> fractionMap ; // Collects the fractions in the Aliquot XML file
	private SortedMap<String,Image> imageMap; // The given images in an aliquot

    // Creates a MapTuple object for Aliquot files using fractions and images.
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

    public void setFractionMap(SortedMap<String, Fraction> fractionMap) {
        this.fractionMap = fractionMap;
    }

    public void setImageMap(SortedMap<String, Image> imageMap) {
        this.imageMap = imageMap;
    }

}