package de.wfb.model.locomotive;

public class DefaultLocomotive {

	/** the protocol address by which the locomotive can be controlled */
	private int address;

	public DefaultLocomotive() {
		super();
	}

	public DefaultLocomotive(final String name, final int address) {
		super();
		this.name = name;
		this.address = address;
	}

	private String name;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
