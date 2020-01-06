package de.wfb.model.node;

import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

public class DefaultRailNodeFactory implements Factory<RailNode> {

	private final Factory<GraphNode> graphNodeFactory = new DefaultGraphNodeFactory();

	@Override
	public RailNode create(final Object... args) throws Exception {

		final ShapeType shapeType = (ShapeType) args[0];

		final int x = (int) args[1];
		final int y = (int) args[2];

		RailNode railNode = null;
		GraphNode graphNodeOne = null;
		GraphNode graphNodeTwo = null;

		Edge northEdge = null;
		Edge eastEdge = null;
		Edge southEdge = null;
		Edge westEdge = null;

		Edge temp = null;

		switch (shapeType) {

		case STRAIGHT_HORIZONTAL:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
			eastEdge.setOutGraphNode(graphNodeOne);

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
			westEdge.setOutGraphNode(graphNodeTwo);

			break;

		case STRAIGHT_VERTICAL:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
			northEdge.setOutGraphNode(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeOne);
			southEdge.setOutGraphNode(graphNodeTwo);

			break;

		case TURN_TOP_RIGHT:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
			northEdge.setOutGraphNode(graphNodeOne);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeOne);
			eastEdge.setOutGraphNode(graphNodeTwo);

			break;

		case TURN_RIGHT_BOTTOM:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeOne);
			eastEdge.setOutGraphNode(graphNodeTwo);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
			southEdge.setOutGraphNode(graphNodeOne);

			break;

		case TURN_BOTTOM_LEFT:

			break;

		case TURN_LEFT_TOP:

			break;

		case SWITCH_LEFT_0:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
			northEdge.setOutGraphNode(graphNodeOne);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
			eastEdge.setOutGraphNode(graphNodeOne);

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
			westEdge.setOutGraphNode(graphNodeTwo);

			break;

		case SWITCH_RIGHT_0:
			railNode = create(ShapeType.SWITCH_LEFT_0);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_0);
			temp = railNode.getNorthEdge();
			railNode.setNorthEdge(null);
			railNode.setSouthEdge(temp);
			break;

		case SWITCH_LEFT_90:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
			northEdge.setOutGraphNode(graphNodeOne);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
			eastEdge.setOutGraphNode(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeOne);
			southEdge.setOutGraphNode(graphNodeTwo);

			break;

		case SWITCH_RIGHT_90:
			railNode = create(ShapeType.SWITCH_LEFT_90);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_90);
			temp = railNode.getEastEdge();
			railNode.setEastEdge(null);
			railNode.setWestEdge(temp);
			break;

		case SWITCH_LEFT_180:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
			eastEdge.setOutGraphNode(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeOne);
			southEdge.setOutGraphNode(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeTwo);
			westEdge.setOutGraphNode(graphNodeOne);

			break;

		case SWITCH_RIGHT_180:
			railNode = create(ShapeType.SWITCH_LEFT_180);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_180);
			temp = railNode.getSouthEdge();
			railNode.setSouthEdge(null);
			railNode.setNorthEdge(temp);
			break;

		case SWITCH_LEFT_270:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create();
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create();
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
			northEdge.setOutGraphNode(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeOne);
			southEdge.setOutGraphNode(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeTwo);
			westEdge.setOutGraphNode(graphNodeOne);

			break;

		case SWITCH_RIGHT_270:
			railNode = create(ShapeType.SWITCH_LEFT_270);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_270);
			temp = railNode.getWestEdge();
			railNode.setWestEdge(null);
			railNode.setEastEdge(temp);
			break;

		default:
			throw new IllegalArgumentException("Uknown shapetype: " + shapeType);
		}

		return railNode;
	}

}
