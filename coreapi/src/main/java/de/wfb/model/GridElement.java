package de.wfb.model;

public interface GridElement<P, T> {

	P getPath();

	void setPath(P path);

	T getText();

	void setText(T text);

}
