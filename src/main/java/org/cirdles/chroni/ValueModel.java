package org.cirdles.chroni;

public class ValueModel {

	private String name;
	private Float value;
	private String uncertaintyType;
	private Float oneSigma;
	
	public ValueModel(String name, Float value, String uncertaintyType, Float oneSigma){
		this.name = name;
		this.value = value;
		this.uncertaintyType = uncertaintyType;
		this.oneSigma = oneSigma;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getUncertaintyType() {
		return uncertaintyType;
	}

	public void setUncertaintyType(String uncertaintyType) {
		this.uncertaintyType = uncertaintyType;
	}

	public Float getOneSigma() {
		return oneSigma;
	}

	public void setOneSigma(Float oneSigma) {
		this.oneSigma = oneSigma;
	}
	
	@Override
	public String toString() {
		return " Value Model has name: " + name + ", value: " + value
				+ ", uncertaintyType: " + uncertaintyType + ", and oneSigma: "
				+ oneSigma + "." + "\n";
	}

}
