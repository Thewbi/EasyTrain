package de.wfb.model.node;

public enum Direction {

	NORTH,

	EAST,

	SOUTH,

	WEST,

	NONE;

	public static boolean isInverseDirection(final Direction lhs, final Direction rhs) {

		if (lhs == Direction.NORTH) {
			return rhs == Direction.SOUTH;
		}
		if (lhs == Direction.EAST) {
			return rhs == Direction.WEST;
		}
		if (lhs == Direction.SOUTH) {
			return rhs == Direction.NORTH;
		}
		if (lhs == Direction.WEST) {
			return rhs == Direction.EAST;
		}

		return false;
	}

}
