package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.ui.ShapeType;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;

public class DefaultSVGPathFactory implements Factory<SVGPath> {

	private static final Logger logger = LogManager.getLogger(DefaultSVGPathFactory.class);

	@Override
	public SVGPath create(final Object... args) {

		logger.info("SVG CREATE");

		final ShapeType shapeType = (ShapeType) args[0];

		final int size = (int) args[1];

		final boolean thrown = (boolean) args[2];

		final boolean highlighted = (boolean) args[3];

		final boolean blocked = (boolean) args[4];

		final boolean selected = (boolean) args[5];

		switch (shapeType) {

		case NONE:
		case REMOVE:
			return null;

		case SQUARE:
			return createSquare(size, highlighted, blocked, selected);

		case TRIANGLE:
			return createTriangle(size, highlighted, blocked, selected);

		case STRAIGHT_HORIZONTAL:
			return createStraightHorizontal(size, highlighted, blocked, selected);
		case STRAIGHT_VERTICAL:
			return createStraightVertical(size, highlighted, blocked, selected);

		case TURN_BOTTOM_LEFT:
			return createTurnBottomLeft(size, highlighted, blocked, selected);
		case TURN_LEFT_TOP:
			return createTurnLeftTop(size, highlighted, blocked, selected);
		case TURN_TOP_RIGHT:
			return createTurnTopRight(size, highlighted, blocked, selected);
		case TURN_RIGHT_BOTTOM:
			return createTurnRightBottom(size, highlighted, blocked, selected);

		case SWITCH_LEFT_0:
			return createSwitchLeft(size, 0, true, thrown, highlighted, blocked, selected);
		case SWITCH_LEFT_90:
			return createSwitchLeft(size, 90, true, thrown, highlighted, blocked, selected);
		case SWITCH_LEFT_180:
			return createSwitchLeft(size, 180, true, thrown, highlighted, blocked, selected);
		case SWITCH_LEFT_270:
			return createSwitchLeft(size, 270, true, thrown, highlighted, blocked, selected);

		case SWITCH_RIGHT_0:
			return createSwitchRight(size, 0, false, thrown, highlighted, blocked, selected);
		case SWITCH_RIGHT_90:
			return createSwitchRight(size, 90, false, thrown, highlighted, blocked, selected);
		case SWITCH_RIGHT_180:
			return createSwitchRight(size, 180, false, thrown, highlighted, blocked, selected);
		case SWITCH_RIGHT_270:
			return createSwitchRight(size, 270, false, thrown, highlighted, blocked, selected);

		default:
			throw new IllegalArgumentException("Uknown ShapeType " + shapeType);
		}
	}

	private Paint retrieveFillColor(final boolean highlighted, final boolean blocked, final boolean selected) {

		// if the node is selected, draw the selected color
		// if the node is not selected but blocked, draw it in blocked color
		// if the node is not selected and not block draw it in default color

		if (selected) {
			return LayoutColors.SELECTED_FILL_COLOR;
		} else if (highlighted) {
			return LayoutColors.HIGHLIGHT_FILL_COLOR;
		} else if (blocked) {
			return LayoutColors.BLOCKED_FILL_COLOR;
		}

		return LayoutColors.STANDARD_FILL_COLOR;
	}

	private SVGPath createSwitchRight(final int size, final double rotateAngle, final boolean left,
			final boolean thrown, final boolean highlighted, final boolean blocked, final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		if (thrown) {

			// @formatter:off
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(7).append(",").append(10)
			    .append(" ").append(3).append(",").append(10)
			    .append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);
			// @formatter:on

		} else {

			// @formatter:off
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(10).append(",").append(3)
				.append(" ").append(10).append(",").append(7)
				.append(" ").append(0).append(",").append(7)
				.append(" ").append(0).append(",").append(3);
			// @formatter:on

		}

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		// Creating the rotation transformation
		final Rotate rotate = new Rotate();

		// Setting the angle for the rotation (20 degrees)
		rotate.setAngle(rotateAngle);

		// Setting pivot points for the rotation
		rotate.setPivotX(5);
		rotate.setPivotY(5);

		// Adding the transformation to rectangle2
		svgPath.getTransforms().addAll(rotate);

		return svgPath;
	}

	private SVGPath createSwitchLeft(final int size, final double rotateAngle, final boolean left, final boolean thrown,
			final boolean highlighted, final boolean blocked, final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		if (thrown) {

			// @formatter:off
			stringBuffer.append("M0,3").append("L")
			    .append(" ").append(10).append(",").append(3)
			    .append(" ").append(10).append(",").append(7)
			    .append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);
			// @formatter:on

		} else {

			// @formatter:off
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(3).append(",").append(0)
				.append(" ").append(7).append(",").append(0)
			    .append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);
			// @formatter:on

		}

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		// Creating the rotation transformation
		final Rotate rotate = new Rotate();

		// Setting the angle for the rotation (20 degrees)
		rotate.setAngle(rotateAngle);

		// Setting pivot points for the rotation
		rotate.setPivotX(5);
		rotate.setPivotY(5);

		// Adding the transformation to rectangle2
		svgPath.getTransforms().addAll(rotate);

		return svgPath;
	}

	private SVGPath createTurnRightBottom(final int i, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M10,3").append("L")
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(7).append(",").append(10)
			.append(" ").append(3).append(",").append(10)
			.append(" ").append(10).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnTopRight(final int i, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(10).append(",").append(3)
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnBottomLeft(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnLeftTop(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(3).append(",").append(0)
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTriangle(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createSquare(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightHorizontal(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(size).append(",").append(3)
			.append(" ").append(size).append(",").append(7)
			.append(" ").append(0).append(",").append(7)
		    .append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightVertical(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

}
