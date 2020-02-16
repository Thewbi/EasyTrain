package de.wfb.rail.ui;

public enum ShapeType {

	NONE,

	REMOVE,

	SQUARE,

	TRIANGLE,

	STRAIGHT_HORIZONTAL,

	STRAIGHT_VERTICAL,

	TURN_TOP_RIGHT,

	TURN_RIGHT_BOTTOM,

	TURN_BOTTOM_LEFT,

	TURN_LEFT_TOP,

	SWITCH,

	SWITCH_LEFT_0,

	SWITCH_LEFT_90,

	SWITCH_LEFT_180,

	SWITCH_LEFT_270,

	SWITCH_RIGHT_0,

	SWITCH_RIGHT_90,

	SWITCH_RIGHT_180,

	SWITCH_RIGHT_270,

	SWITCH_DOUBLECROSS_LEFT_TOP,

	SWITCH_DOUBLECROSS_TOP_RIGHT;

	public static boolean isTurnout(final ShapeType shapeType) {

		switch (shapeType) {
		case SWITCH:
		case SWITCH_LEFT_0:
		case SWITCH_LEFT_90:
		case SWITCH_LEFT_180:
		case SWITCH_LEFT_270:
		case SWITCH_RIGHT_0:
		case SWITCH_RIGHT_90:
		case SWITCH_RIGHT_180:
		case SWITCH_RIGHT_270:
		case SWITCH_DOUBLECROSS_LEFT_TOP:
		case SWITCH_DOUBLECROSS_TOP_RIGHT:
			return true;

		default:
			return false;
		}
	}

	public static boolean isNotTurnout(final ShapeType shapeType) {

		return !isTurnout(shapeType);
	}

}
