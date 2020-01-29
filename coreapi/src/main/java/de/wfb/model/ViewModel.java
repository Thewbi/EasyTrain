package de.wfb.model;

public interface ViewModel<P, T> {

	final int COLUMNS = 100;

	final int ROWS = 100;

	GridElement<P, T>[][] getViewModel();

	void clear();

}
