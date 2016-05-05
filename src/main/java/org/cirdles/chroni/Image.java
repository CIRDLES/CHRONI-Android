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

/**
 * Defines the structure of an Image object for Analysis Images found in Aliquot files.
 */
public class Image {

	private String imageType; // Type of Image: Probability Density or Concordia Plot
	private String imageURL; // the URL of the image

    /**
     * Instantiates an image given a type and URL
     */
	public Image(String type, String url){
		this.imageType = type;
		this.imageURL = url;
	}

	public String getImageURL() {
		return imageURL;
	}

}
