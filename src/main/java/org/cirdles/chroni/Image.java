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
