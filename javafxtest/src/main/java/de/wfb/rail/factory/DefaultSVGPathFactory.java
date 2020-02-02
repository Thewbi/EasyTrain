package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Direction;
import de.wfb.rail.ui.ShapeType;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;

public class DefaultSVGPathFactory implements Factory<SVGPath> {

	private static final Logger logger = LogManager.getLogger(DefaultSVGPathFactory.class);

	@Override
	public SVGPath create(final Object... args) {

		logger.trace("SVG CREATE");

		final ShapeType shapeType = (ShapeType) args[0];

		final int size = (int) args[1];

		final boolean thrown = (boolean) args[2];

		final boolean flipped = (boolean) args[3];

		final boolean highlighted = (boolean) args[4];

		final boolean blocked = (boolean) args[5];

		final boolean selected = (boolean) args[6];

		final boolean reserved = (boolean) args[7];

		Direction direction = null;
		if (args[8] != null) {
			direction = (Direction) args[8];
		}

		switch (shapeType) {

		case NONE:
		case REMOVE:
			return null;

		case SQUARE:
			return createSquare(size, highlighted, blocked, selected, reserved);

		case TRIANGLE:
			return createTriangle(size, highlighted, blocked, selected, reserved);

		case STRAIGHT_HORIZONTAL:
			return createStraightHorizontal(direction, size, highlighted, blocked, selected, reserved);
		case STRAIGHT_VERTICAL:
			return createStraightVertical(direction, size, highlighted, blocked, selected, reserved);

		case TURN_BOTTOM_LEFT:
			return createTurnBottomLeft(size, highlighted, blocked, selected, reserved);
		case TURN_LEFT_TOP:
			return createTurnLeftTop(size, highlighted, blocked, selected, reserved);
		case TURN_TOP_RIGHT:
			return createTurnTopRight(size, highlighted, blocked, selected, reserved);
		case TURN_RIGHT_BOTTOM:
			return createTurnRightBottom(size, highlighted, blocked, selected, reserved);

		case SWITCH_LEFT_0:
			return createSwitchLeft(size, 0, true, thrown, flipped, highlighted, blocked, selected, reserved);
		case SWITCH_LEFT_90:
			return createSwitchLeft(size, 90, true, thrown, flipped, highlighted, blocked, selected, reserved);
		case SWITCH_LEFT_180:
			return createSwitchLeft(size, 180, true, thrown, flipped, highlighted, blocked, selected, reserved);
		case SWITCH_LEFT_270:
			return createSwitchLeft(size, 270, true, thrown, flipped, highlighted, blocked, selected, reserved);

		case SWITCH_RIGHT_0:
			return createSwitchRight(size, 0, false, thrown, flipped, highlighted, blocked, selected, reserved);
		case SWITCH_RIGHT_90:
			return createSwitchRight(size, 90, false, thrown, flipped, highlighted, blocked, selected, reserved);
		case SWITCH_RIGHT_180:
			return createSwitchRight(size, 180, false, thrown, flipped, highlighted, blocked, selected, reserved);
		case SWITCH_RIGHT_270:
			return createSwitchRight(size, 270, false, thrown, flipped, highlighted, blocked, selected, reserved);

		default:
			throw new IllegalArgumentException("Uknown ShapeType " + shapeType);
		}
	}

	private Paint retrieveFillColor(final boolean highlighted, final boolean blocked, final boolean selected,
			final boolean reserved) {

		// if the node is selected, draw the selected color
		// if the node is not selected but blocked, draw it in blocked color
		// if the node is not selected and not block draw it in default color

		// @formatter:off

		if (selected)
		{
			return LayoutColors.SELECTED_FILL_COLOR;
		}
//		else if (highlighted)
//		{
//			return LayoutColors.HIGHLIGHT_FILL_COLOR;
//		}
		else if (blocked)
		{
			return LayoutColors.BLOCKED_FILL_COLOR;
		}
		else if (reserved)
		{
			return LayoutColors.RESERVED_FILL_COLOR;
		}
		else if (highlighted)
		{
			return LayoutColors.HIGHLIGHT_FILL_COLOR;
		}

		// @formatter:on

		return LayoutColors.STANDARD_FILL_COLOR;
	}

	private SVGPath createSwitchRight(final int size, final double rotateAngle, final boolean left,
			final boolean thrown, final boolean flipped, final boolean highlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		if (flipped ? !thrown : thrown) {

			// @formatter:off

			// gebogen (left to bottom)
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(7).append(",").append(10)
			    .append(" ").append(3).append(",").append(10)
			    .append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);

			// @formatter:on

		} else {

			// @formatter:off

			// gerade (left to right)
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(10).append(",").append(3)
				.append(" ").append(10).append(",").append(7)
				.append(" ").append(0).append(",").append(7)
				.append(" ").append(0).append(",").append(3);

			// @formatter:on

		}

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.TURNOUT_STROKE_COLOR);
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
			final boolean flipped, final boolean highlighted, final boolean blocked, final boolean selected,
			final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// Eine Weiche kann entweder die Form eines geraden Gleis annehmen
		// oder die Form eines gebogenen.

		// Die gerade Form heißt closed oder Grün
		// Die gebogene Form heißt thrown oder Rot

		// Red-Thrown == gebogen
		// Green-Closed == gerade (G wie gerade)

		if (flipped ? !thrown : thrown) {

			// @formatter:off

			// gebogen (left - top)
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(3).append(",").append(0)
				.append(" ").append(7).append(",").append(0)
			    .append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);
			// @formatter:on

		} else {

			// @formatter:off

			// gerade (left to right)
			stringBuffer.append("M0,3").append("L")
			    .append(" ").append(10).append(",").append(3)
			    .append(" ").append(10).append(",").append(7)
			    .append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);
			// @formatter:on

		}

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.TURNOUT_STROKE_COLOR);
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
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M10,3").append("L")
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(7).append(",").append(10)
			.append(" ").append(3).append(",").append(10)
			.append(" ").append(10).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnTopRight(final int i, final boolean highlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(10).append(",").append(3)
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnBottomLeft(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnLeftTop(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(3).append(",").append(0)
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTriangle(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createSquare(final int size, final boolean highlighted, final boolean blocked,
			final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightHorizontal(final Direction direction, final int size, final boolean highlighted,
			final boolean blocked, final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		if (direction != null && direction == Direction.WEST) {

			// @formatter:off
			stringBuffer.append("M10,0").append("L")
				.append(" ").append(0).append(",").append(5)
				.append(" ").append(10).append(",").append(size);
			// @formatter:on

		} else if (direction != null && direction == Direction.EAST) {

			// @formatter:off
			stringBuffer.append("M0,0").append("L")
				.append(" ").append(size).append(",").append(5)
				.append(" ").append(0).append(",").append(size);
			// @formatter:on

		} else {

			// @formatter:off
			stringBuffer.append("M0,3").append("L")
				.append(" ").append(size).append(",").append(3)
				.append(" ").append(size).append(",").append(7)
				.append(" ").append(0).append(",").append(7)
			    .append(" ").append(0).append(",").append(3);
			// @formatter:on
		}

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightVertical(final Direction direction, final int size, final boolean highlighted,
			final boolean blocked, final boolean selected, final boolean reserved) {

		final StringBuffer stringBuffer = new StringBuffer();

		if (direction != null && direction == Direction.NORTH) {

			// @formatter:off
			stringBuffer.append("M0,10").append("L")
				.append(" ").append(5).append(",").append(0)
				.append(" ").append(10).append(",").append(10);
			// @formatter:on

		} else if (direction != null && direction == Direction.SOUTH) {

			// @formatter:off
			stringBuffer.append("M0,0").append("L")
				.append(" ").append(5).append(",").append(10)
				.append(" ").append(10).append(",").append(0);
			// @formatter:on

		} else {

			// @formatter:off
			stringBuffer.append("M3,0").append("L")
				.append(" ").append(7).append(",").append(0)
				.append(" ").append(7).append(",").append(size)
				.append(" ").append(3).append(",").append(size)
				.append(" ").append(3).append(",").append(0);
			// @formatter:on

		}

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(retrieveFillColor(highlighted, blocked, selected, reserved));
		svgPath.setStroke(LayoutColors.STANDARD_STROKE_COLOR);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

}
