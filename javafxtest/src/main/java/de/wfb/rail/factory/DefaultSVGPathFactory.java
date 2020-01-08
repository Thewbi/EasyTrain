package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.ui.ShapeType;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;

public class DefaultSVGPathFactory implements Factory<SVGPath> {

	private static final Color STANDARD_STROKE_COLOR = Color.BLUE;

	private static final Color STANDARD_FILL_COLOR = Color.ALICEBLUE;

	private static final Color HIGHLIGHT_FILL_COLOR = Color.ORANGE;

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DefaultSVGPathFactory.class);

	@Override
	public SVGPath create(final Object... args) {

		final ShapeType shapeType = (ShapeType) args[0];

		final int size = (int) args[1];

		final boolean thrown = (boolean) args[2];

		final boolean highlighted = (boolean) args[3];

		switch (shapeType) {

		case NONE:
		case REMOVE:
			return null;

		case SQUARE:
			return createSquare(size, highlighted);

		case TRIANGLE:
			return createTriangle(size, highlighted);

		case STRAIGHT_HORIZONTAL:
			return createStraightHorizontal(size, highlighted);
		case STRAIGHT_VERTICAL:
			return createStraightVertical(size, highlighted);

		case TURN_BOTTOM_LEFT:
			return createTurnBottomLeft(size, highlighted);
		case TURN_LEFT_TOP:
			return createTurnLeftTop(size, highlighted);
		case TURN_TOP_RIGHT:
			return createTurnTopRight(size, highlighted);
		case TURN_RIGHT_BOTTOM:
			return createTurnRightBottom(size, highlighted);

		case SWITCH_LEFT_0:
			return createSwitchLeft(size, 0, true, thrown, highlighted);
		case SWITCH_LEFT_90:
			return createSwitchLeft(size, 90, true, thrown, highlighted);
		case SWITCH_LEFT_180:
			return createSwitchLeft(size, 180, true, thrown, highlighted);
		case SWITCH_LEFT_270:
			return createSwitchLeft(size, 270, true, thrown, highlighted);

		case SWITCH_RIGHT_0:
			return createSwitchRight(size, 0, false, thrown, highlighted);
		case SWITCH_RIGHT_90:
			return createSwitchRight(size, 90, false, thrown, highlighted);
		case SWITCH_RIGHT_180:
			return createSwitchRight(size, 180, false, thrown, highlighted);
		case SWITCH_RIGHT_270:
			return createSwitchRight(size, 270, false, thrown, highlighted);

		default:
			throw new IllegalArgumentException("Uknown ShapeType " + shapeType);
		}
	}

	private SVGPath createSwitchRight(final int size, final double rotateAngle, final boolean left,
			final boolean thrown, final boolean highlighted) {

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
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
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

		// svgPath.setScaleY(left ? 1 : -1);

		return svgPath;
	}

	private SVGPath createSwitchLeft(final int size, final double rotateAngle, final boolean left, final boolean thrown,
			final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		if (thrown) {

			// @formatter:off
			stringBuffer.append("M0,3").append("L")
//				.append(" ").append(3).append(",").append(0)
//				.append(" ").append(7).append(",").append(0)
//				.append(" ").append(7).append(",").append(0)
//				.append(" ").append(4).append(",").append(3)
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
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
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

	private SVGPath createTurnRightBottom(final int i, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M10,3").append("L")
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(7).append(",").append(10)
			.append(" ").append(3).append(",").append(10)
			.append(" ").append(10).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnTopRight(final int i, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(10).append(",").append(3)
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnBottomLeft(final int size, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnLeftTop(final int size, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(3).append(",").append(0)
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTriangle(final int size, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createSquare(final int size, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightHorizontal(final int size, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(size).append(",").append(3)
			.append(" ").append(size).append(",").append(7)
			.append(" ").append(0).append(",").append(7)
		    .append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightVertical(final int size, final boolean highlighted) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(highlighted ? HIGHLIGHT_FILL_COLOR : STANDARD_FILL_COLOR);
		svgPath.setStroke(STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

}
