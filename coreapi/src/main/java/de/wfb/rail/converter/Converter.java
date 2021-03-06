package de.wfb.rail.converter;

public interface Converter<S, T> {

	void convert(S source, T target);

	T convert(S source);

}
