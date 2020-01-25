package de.wfb.javafxtest.controls;

import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class GridElement {

	private SVGPath svgPath;

	public GridElement(final SVGPath svgPath, final Text text) {
		super();
		this.svgPath = svgPath;
		this.text = text;
	}

	private Text text;

	public SVGPath getSvgPath() {
		return svgPath;
	}

	public void setSvgPath(final SVGPath svgPath) {
		this.svgPath = svgPath;
	}

	public Text getText() {
		return text;
	}

	public void setText(final Text text) {
		this.text = text;
	}

}
