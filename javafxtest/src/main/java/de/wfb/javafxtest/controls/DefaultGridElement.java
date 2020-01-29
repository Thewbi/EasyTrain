package de.wfb.javafxtest.controls;

import de.wfb.model.GridElement;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class DefaultGridElement implements GridElement<SVGPath, Text> {

	private SVGPath svgPath;

	private Text text;

	public DefaultGridElement(final SVGPath svgPath, final Text text) {
		super();
		this.svgPath = svgPath;
		this.text = text;
	}

	@Override
	public SVGPath getPath() {
		return svgPath;
	}

	@Override
	public void setPath(final SVGPath svgPath) {
		this.svgPath = svgPath;
	}

	@Override
	public Text getText() {
		return text;
	}

	@Override
	public void setText(final Text text) {
		this.text = text;
	}

}
