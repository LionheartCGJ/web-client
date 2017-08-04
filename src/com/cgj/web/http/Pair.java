package com.cgj.web.http;

public class Pair {
	String name;
	String value;

	public Pair(String name, String value) {
		if (name == null || value == null) {
			throw new IllegalArgumentException(
					"name and value must be not null!");
		}
		this.name = name;
		this.value = value;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object anObject) {
		if (anObject instanceof Pair) {
			return name.equalsIgnoreCase(((Pair) (anObject)).name);
		}
		return false;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString(){
		return name + Contants.HEADER_SEPARATOR + value;
	}
}
