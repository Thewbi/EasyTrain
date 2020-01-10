package de.wfb.model.locomotive;

public class DefaultLocomotive {

	private int id;

	/** the protocol address by which the locomotive can be controlled */
	private int address;

	private String name;

	public DefaultLocomotive() {
		super();
	}

	public DefaultLocomotive(final int id, final String name, final int address) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(final int address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DefaultLocomotive other = (DefaultLocomotive) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
