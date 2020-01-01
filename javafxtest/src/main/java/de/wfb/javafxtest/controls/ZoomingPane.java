package de.wfb.javafxtest.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

public class ZoomingPane extends Pane {

	private final Node content;

	private final DoubleProperty zoomFactor = new SimpleDoubleProperty(1);

	public ZoomingPane(final Node content) {

		this.content = content;
		getChildren().add(content);
		final Scale scale = new Scale(1, 1);
		content.getTransforms().add(scale);

		zoomFactor.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
					final Number newValue) {
				scale.setX(newValue.doubleValue());
				scale.setY(newValue.doubleValue());
				requestLayout();
			}
		});
	}

	@Override
	protected void layoutChildren() {

		final Pos pos = Pos.TOP_LEFT;
		final double width = getWidth();
		final double height = getHeight();
		final double top = getInsets().getTop();
		final double right = getInsets().getRight();
		final double left = getInsets().getLeft();
		final double bottom = getInsets().getBottom();
		final double contentWidth = (width - left - right) / zoomFactor.get();
		final double contentHeight = (height - top - bottom) / zoomFactor.get();
		layoutInArea(content, left, top, contentWidth, contentHeight, 0, null, pos.getHpos(), pos.getVpos());
	}

	public final Double getZoomFactor() {
		return zoomFactor.get();
	}

	public final void setZoomFactor(final Double zoomFactor) {
		this.zoomFactor.set(zoomFactor);
	}

	public final DoubleProperty zoomFactorProperty() {
		return zoomFactor;
	}
}
