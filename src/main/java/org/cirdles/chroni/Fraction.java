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
import java.util.TreeMap;

/**
 * Structures the Fraction class for Aliquot file parsing.
 */
public class Fraction {

	private String fractionID;
	private String numberOfGrains;

	private SortedMap<String, ValueModel> valueModelMap = new TreeMap<String, ValueModel>();

	public Fraction(String fractionID, String numberOfGrains) {
		this.fractionID = fractionID;
		this.numberOfGrains = numberOfGrains;
	}

	public String getFractionID() {
		return fractionID;
	}

	public SortedMap<String, ValueModel> getValueModelMap() {
		return valueModelMap;
	}

	@Override
	public String toString() {
		return "This fraction has fraction ID: " + fractionID + "." + "\n";
	}


	public String getNumberOfGrains() {
		return numberOfGrains;
	}

}