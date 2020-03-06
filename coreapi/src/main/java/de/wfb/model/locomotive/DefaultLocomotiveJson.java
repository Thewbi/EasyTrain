package de.wfb.model.locomotive;

public class DefaultLocomotiveJson {

	private int id;

	private short address;

	private String name;

	private String imageFilename;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public short getAddress() {
		return address;
	}

	public void setAddress(final short address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getImageFilename() {
		return imageFilename;
	}

	public void setImageFilename(final String imageFilename) {
		this.imageFilename = imageFilename;
	}

}
