package de.wfb.rail.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.rail.ui.ShapeType;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;

public class DefaultSVGPathFactory implements Factory<SVGPath> {

	private static final Logger logger = LogManager.getLogger(DefaultSVGPathFactory.class);

	@Override
	public SVGPath create(final Object... args) {

		final ShapeType shapeType = (ShapeType) args[0];

		switch (shapeType) {

		case NONE:
		case REMOVE:
			return null;

		case SQUARE:
			return createSquare((int) args[1]);

		case TRIANGLE:
			return createTriangle((int) args[1]);

		case STRAIGHT_HORIZONTAL:
			return createStraightHorizontal((int) args[1]);
		case STRAIGHT_VERTICAL:
			return createStraightVertical((int) args[1]);

		case TURN_BOTTOM_LEFT:
			return createTurnBottomLeft((int) args[1]);
		case TURN_LEFT_TOP:
			return createTurnLeftTop((int) args[1]);
		case TURN_TOP_RIGHT:
			return createTurnTopRight((int) args[1]);
		case TURN_RIGHT_BOTTOM:
			return createTurnRightBottom((int) args[1]);

		case SWITCH_LEFT_0:
			return createSwitchLeft((int) args[1], 0, true, (boolean) args[2]);
		case SWITCH_LEFT_90:
			return createSwitchLeft((int) args[1], 90, true, (boolean) args[2]);
		case SWITCH_LEFT_180:
			return createSwitchLeft((int) args[1], 180, true, (boolean) args[2]);
		case SWITCH_LEFT_270:
			return createSwitchLeft((int) args[1], 270, true, (boolean) args[2]);

		case SWITCH_RIGHT_0:
			return createSwitchRight((int) args[1], 0, false, (boolean) args[2]);
		case SWITCH_RIGHT_90:
			return createSwitchRight((int) args[1], 90, false, (boolean) args[2]);
		case SWITCH_RIGHT_180:
			return createSwitchRight((int) args[1], 180, false, (boolean) args[2]);
		case SWITCH_RIGHT_270:
			return createSwitchRight((int) args[1], 270, false, (boolean) args[2]);

		default:
			throw new IllegalArgumentException("Uknown ShapeType " + shapeType);
		}
	}

	private SVGPath createSwitchRight(final int size, final double rotateAngle, final boolean left,
			final boolean thrown) {

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
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
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

	private SVGPath createSwitchLeft(final int size, final double rotateAngle, final boolean left,
			final boolean thrown) {

		final StringBuffer stringBuffer = new StringBuffer();

//		// @formatter:off
//		stringBuffer.append("M0,3").append("L")
//			.append(" ").append(3).append(",").append(0)
//			.append(" ").append(7).append(",").append(0)
//			.append(" ").append(7).append(",").append(0)
//			.append(" ").append(3).append(",").append(3)
//		    .append(" ").append(10).append(",").append(3)
//		    .append(" ").append(10).append(",").append(7)
//		    .append(" ").append(0).append(",").append(7)
//		    .append(" ").append(0).append(",").append(3);
//		// @formatter:on

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
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
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

		// svgPath.setRotate(rotate);

		// svgPath.setScaleY(left ? 1 : -1);

		return svgPath;
	}

//	MatrixType getMatrixForRotation(final double degree, final int cx, final int cy)
//	{
//	  final double ca = Math.cos(degree * Math.PI / 180);
//	  final double sa = Math.sin(degree * Math.PI / 180);
//
//	  final double a = ca;
//	  final double b = sa;
//	  final double c = (-sa);
//	  final double d = ca;
//	  final double e = (-ca * cx + sa * cy + cx);
//	  final double f = (-sa * cx - ca * cy + cy);
//
//	  return "matrix(" + [a,b,c,d,e,f].join(' ') + ")";
//
//	  MatrixType matrix = new MatrixType();
//
//	  return matrix;
//	}

	private SVGPath createTurnRightBottom(final int i) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M10,3").append("L")
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(7).append(",").append(10)
			.append(" ").append(3).append(",").append(10)
			.append(" ").append(10).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnTopRight(final int i) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(10).append(",").append(3)
			.append(" ").append(10).append(",").append(7)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnBottomLeft(final int size) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTurnLeftTop(final int size) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(3).append(",").append(0)
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(0).append(",").append(7)
			.append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createTriangle(final int size) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createSquare(final int size) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,0").append("L")
			.append(" ").append(0).append(",").append(0)
			.append(" ").append(size).append(",").append(0)
			.append(" ").append(size).append(",").append(size)
			.append(" ").append(0).append(",").append(size);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightHorizontal(final int size) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M0,3").append("L")
			.append(" ").append(size).append(",").append(3)
			.append(" ").append(size).append(",").append(7)
			.append(" ").append(0).append(",").append(7)
		    .append(" ").append(0).append(",").append(3);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

	private SVGPath createStraightVertical(final int size) {

		final StringBuffer stringBuffer = new StringBuffer();

		// @formatter:off
		stringBuffer.append("M3,0").append("L")
			.append(" ").append(7).append(",").append(0)
			.append(" ").append(7).append(",").append(size)
			.append(" ").append(3).append(",").append(size)
			.append(" ").append(3).append(",").append(0);
		// @formatter:on

		final SVGPath svgPath = new SVGPath();
		svgPath.setFill(Color.ALICEBLUE);
		svgPath.setStroke(Color.BLUE);
		svgPath.setContent(stringBuffer.toString());

		return svgPath;
	}

}
