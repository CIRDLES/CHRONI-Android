package org.cirdles.chroni;

public class Image {

	private String imageType;
	private String imageURL;
	
	public Image(String type, String url){
		this.imageType = type;
		this.imageURL = url;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
}
