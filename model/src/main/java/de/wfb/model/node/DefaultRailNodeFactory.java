package de.wfb.model.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.service.IdService;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

public class DefaultRailNodeFactory implements Factory<Node> {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DefaultRailNodeFactory.class);

	private final Factory<GraphNode> graphNodeFactory = new DefaultGraphNodeFactory();

	@Autowired
	private IdService idService;

//	@Autowired
//	private ModelService modelService;

	@Override
	public Node create(final Object... args) throws Exception {

		final Object firstParameter = args[0];

		RailNode railNode = null;

		if (firstParameter instanceof JsonNode) {

			railNode = createFromJsonNode((JsonNode) firstParameter);

		} else {

			railNode = createFromParameters(args);

		}

		return railNode;
	}

	private RailNode createFromJsonNode(final JsonNode jsonNode) throws Exception {

		final int x = jsonNode.getX();
		final int y = jsonNode.getY();
		final ShapeType shapeType = ShapeType.valueOf(jsonNode.getShapeType());
		final int id = jsonNode.getId();

		final RailNode node = createFromParametersInternal(id, x, y, shapeType);

		if (ShapeType.isTurnout(shapeType)) {

			if (jsonNode.getProtocolTurnoutId() != null) {
				node.setProtocolTurnoutId(jsonNode.getProtocolTurnoutId());
			}
		}

		return node;
	}

	private RailNode createFromParameters(final Object... args) throws Exception {

		final int x = (int) args[0];
		final int y = (int) args[1];
		final ShapeType shapeType = (ShapeType) args[2];

		return createFromParametersInternal(idService.getNextId(), x, y, shapeType);
	}

	private RailNode createFromParametersInternal(final int id, final int x, final int y, final ShapeType shapeType)
			throws Exception {

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
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
			westEdge.setOutGraphNode(graphNodeTwo);
//			westEdge.setInGraphNode(graphNodeTwo);
//			westEdge.setOutGraphNode(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
			southEdge.setOutGraphNode(graphNodeOne);
//			southEdge.setInGraphNode(graphNodeOne);
//			southEdge.setOutGraphNode(graphNodeTwo);

			break;

		case TURN_LEFT_TOP:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
			westEdge.setOutGraphNode(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
			northEdge.setOutGraphNode(graphNodeOne);

			break;

		case SWITCH_LEFT_0:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_0);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_0);
			temp = railNode.getNorthEdge();
			railNode.setNorthEdge(null);
			railNode.setSouthEdge(temp);
			break;

		case SWITCH_LEFT_90:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeOne);
			northEdge.setOutGraphNode(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
			eastEdge.setOutGraphNode(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
			southEdge.setOutGraphNode(graphNodeOne);

			break;

		case SWITCH_RIGHT_90:
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_90);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_90);
			temp = railNode.getEastEdge();
			railNode.setEastEdge(null);
			railNode.setWestEdge(temp);
			break;

		case SWITCH_LEFT_180:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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

			// west
			westEdge = new DefaultEdge();
			railNode.setEdge(EdgeDirection.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeTwo);
			westEdge.setOutGraphNode(graphNodeOne);

			break;

		case SWITCH_RIGHT_180:
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_180);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_180);
			temp = railNode.getSouthEdge();
			railNode.setSouthEdge(null);
			railNode.setNorthEdge(temp);
			break;

		case SWITCH_LEFT_270:

			railNode = new DefaultRailNode();
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
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
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_270);
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

	public IdService getIdService() {
		return idService;
	}

	public void setIdService(final IdService idService) {
		this.idService = idService;
	}

}