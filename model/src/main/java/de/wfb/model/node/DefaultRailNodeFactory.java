package de.wfb.model.node;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.service.IdService;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.service.TurnoutService;
import de.wfb.rail.ui.ShapeType;

public class DefaultRailNodeFactory implements Factory<Node> {

	private static final Logger logger = LogManager.getLogger(DefaultRailNodeFactory.class);

	private final Factory<GraphNode> graphNodeFactory = new DefaultGraphNodeFactory();

	@Autowired
	private IdService idService;

	@Autowired
	private TurnoutService turnoutService;

	@Autowired
	private ModelFacade modelFacade;

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

		final int id = jsonNode.getId();
		final int x = jsonNode.getX();
		final int y = jsonNode.getY();

		final String shapeTypeAsString = jsonNode.getShapeType();
		ShapeType shapeType = ShapeType.NONE;
		if (StringUtils.isNotBlank(shapeTypeAsString)) {

			shapeType = ShapeType.valueOf(shapeTypeAsString);

		} else {

			logger.error("jsonNode ID: " + id + " has no shape type!");

		}

		final Integer feebackBlockNumber = jsonNode.getFeedbackBlockNumber();
		final Direction traverse = jsonNode.getTraverse();
		final Boolean isFlipped = jsonNode.isFlipped();

		final RailNode node = createFromParametersInternal(id, x, y, shapeType, feebackBlockNumber, traverse,
				isFlipped);

		if (ShapeType.isTurnout(shapeType) || ShapeType.isSignal(shapeType)) {

			if (jsonNode.getProtocolTurnoutId() != null) {

				node.setProtocolTurnoutId(jsonNode.getProtocolTurnoutId());

				// queue initial status query for this turn out
				turnoutService.queueStateRequest(node);
			}
		}

		return node;
	}

	private RailNode createFromParameters(final Object... args) throws Exception {

		// 0
		final int x = (int) args[0];

		// 1
		final int y = (int) args[1];

		// 2
		final ShapeType shapeType = (ShapeType) args[2];

		// 3
		final int feedbackBlockNumber = (int) args[3];

		// 4
		Direction traverse = null;
		if (args.length > 4) {
			traverse = (Direction) args[4];
		}

		// 5
		boolean flipped = false;
		if (args.length > 5) {
			flipped = (boolean) args[5];
		}

		return createFromParametersInternal(idService.getNextId(), x, y, shapeType, feedbackBlockNumber, traverse,
				flipped);
	}

	private RailNode createFromParametersInternal(final int id, final int x, final int y, final ShapeType shapeType,
			final Integer feedbackBlockNumber, final Direction traverse, final Boolean flipped) throws Exception {

		RailNode railNode = null;
		GraphNode graphNodeOne = null;
		GraphNode graphNodeTwo = null;
		GraphNode graphNodeThree = null;
		GraphNode graphNodeFour = null;

		Edge northEdge = null;
		Edge eastEdge = null;
		Edge southEdge = null;
		Edge westEdge = null;

		Edge temp = null;

		switch (shapeType) {

		case SWITCH_DOUBLECROSS_LEFT_TOP:
			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setTraverse(traverse);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			graphNodeThree = graphNodeFactory.create(railNode);
			railNode.setGraphNodeThree(graphNodeThree);

			graphNodeFour = graphNodeFactory.create(railNode);
			railNode.setGraphNodeFour(graphNodeFour);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeOne);
			// northEdge.setOutGraphNode(graphNodeThree);
			northEdge.getOutGraphNodes().add(graphNodeThree);
			northEdge.getOutGraphNodes().add(graphNodeFour);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
//			eastEdge.setOutGraphNode(graphNodeThree);
			eastEdge.getOutGraphNodes().add(graphNodeThree);
			eastEdge.getOutGraphNodes().add(graphNodeFour);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeThree);
//			southEdge.setOutGraphNode(graphNodeTwo);
			southEdge.getOutGraphNodes().add(graphNodeOne);
			southEdge.getOutGraphNodes().add(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeFour);
//			westEdge.setOutGraphNode(graphNodeOne);
			westEdge.getOutGraphNodes().add(graphNodeOne);
			westEdge.getOutGraphNodes().add(graphNodeTwo);

			break;

		case SWITCH_DOUBLECROSS_TOP_RIGHT:
			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setTraverse(traverse);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			graphNodeThree = graphNodeFactory.create(railNode);
			railNode.setGraphNodeThree(graphNodeThree);

			graphNodeFour = graphNodeFactory.create(railNode);
			railNode.setGraphNodeFour(graphNodeFour);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeOne);
//			northEdge.setOutGraphNode(graphNodeTwo);
			northEdge.getOutGraphNodes().add(graphNodeTwo);
			northEdge.getOutGraphNodes().add(graphNodeThree);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
//			eastEdge.setOutGraphNode(graphNodeOne);
			eastEdge.getOutGraphNodes().add(graphNodeOne);
			eastEdge.getOutGraphNodes().add(graphNodeFour);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeThree);
//			southEdge.setOutGraphNode(graphNodeFour);
			southEdge.getOutGraphNodes().add(graphNodeOne);
			southEdge.getOutGraphNodes().add(graphNodeFour);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeFour);
//			westEdge.setOutGraphNode(graphNodeThree);
			westEdge.getOutGraphNodes().add(graphNodeTwo);
			westEdge.getOutGraphNodes().add(graphNodeThree);

			break;

		case SIGNAL_HORIZONTAL:
		case STRAIGHT_HORIZONTAL:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setTraverse(traverse);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
			eastEdge.getOutGraphNodes().add(graphNodeOne);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
			westEdge.getOutGraphNodes().add(graphNodeTwo);

			break;

		case STRAIGHT_VERTICAL:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setTraverse(traverse);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
//			northEdge.setOutGraphNode(graphNodeOne);
			northEdge.getOutGraphNodes().add(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeOne);
//			southEdge.setOutGraphNode(graphNodeTwo);
			southEdge.getOutGraphNodes().add(graphNodeTwo);

			break;

		case TURN_TOP_RIGHT:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
//			northEdge.setOutGraphNode(graphNodeOne);
			northEdge.getOutGraphNodes().add(graphNodeOne);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeOne);
//			eastEdge.setOutGraphNode(graphNodeTwo);
			eastEdge.getOutGraphNodes().add(graphNodeTwo);

			break;

		case TURN_RIGHT_BOTTOM:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeOne);
//			eastEdge.setOutGraphNode(graphNodeTwo);
			eastEdge.getOutGraphNodes().add(graphNodeTwo);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
//			southEdge.setOutGraphNode(graphNodeOne);
			southEdge.getOutGraphNodes().add(graphNodeOne);

			break;

		case TURN_BOTTOM_LEFT:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
//			westEdge.setOutGraphNode(graphNodeTwo);
			westEdge.getOutGraphNodes().add(graphNodeTwo);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
//			southEdge.setOutGraphNode(graphNodeOne);
			southEdge.getOutGraphNodes().add(graphNodeOne);

			break;

		case TURN_LEFT_TOP:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
//			westEdge.setOutGraphNode(graphNodeTwo);
			westEdge.getOutGraphNodes().add(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
//			northEdge.setOutGraphNode(graphNodeOne);
			northEdge.getOutGraphNodes().add(graphNodeOne);

			break;

		case SWITCH_LEFT_0:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setFlipped(flipped);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
//			northEdge.setOutGraphNode(graphNodeOne);
			northEdge.getOutGraphNodes().add(graphNodeOne);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
//			eastEdge.setOutGraphNode(graphNodeOne);
			eastEdge.getOutGraphNodes().add(graphNodeOne);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeOne);
//			westEdge.setOutGraphNode(graphNodeTwo);
			westEdge.getOutGraphNodes().add(graphNodeTwo);

			break;

		case SWITCH_RIGHT_0:
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_0, feedbackBlockNumber, traverse,
					flipped);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_0);
			temp = railNode.getNorthEdge();
			temp.setDirection(Direction.SOUTH);
			railNode.setNorthEdge(null);
			railNode.setSouthEdge(temp);
			break;

		case SWITCH_LEFT_90:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setFlipped(flipped);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeOne);
//			northEdge.setOutGraphNode(graphNodeTwo);
			northEdge.getOutGraphNodes().add(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeTwo);
//			eastEdge.setOutGraphNode(graphNodeOne);
			eastEdge.getOutGraphNodes().add(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
//			southEdge.setOutGraphNode(graphNodeOne);
			southEdge.getOutGraphNodes().add(graphNodeOne);

			break;

		case SWITCH_RIGHT_90:
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_90, feedbackBlockNumber, traverse,
					flipped);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_90);
			temp = railNode.getEastEdge();
			temp.setDirection(Direction.WEST);
			railNode.setEastEdge(null);
			railNode.setWestEdge(temp);
			break;

		case SWITCH_LEFT_180:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setFlipped(flipped);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// east
			eastEdge = new DefaultEdge();
			eastEdge.setDirection(Direction.EAST);
			railNode.setEdge(Direction.EAST, eastEdge);
			eastEdge.setInGraphNode(graphNodeOne);
//			eastEdge.setOutGraphNode(graphNodeTwo);
			eastEdge.getOutGraphNodes().add(graphNodeTwo);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeTwo);
//			southEdge.setOutGraphNode(graphNodeOne);
			southEdge.getOutGraphNodes().add(graphNodeOne);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeTwo);
//			westEdge.setOutGraphNode(graphNodeOne);
			westEdge.getOutGraphNodes().add(graphNodeOne);

			break;

		case SWITCH_RIGHT_180:
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_180, feedbackBlockNumber, traverse,
					flipped);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_180);
			temp = railNode.getSouthEdge();
			temp.setDirection(Direction.NORTH);
			railNode.setSouthEdge(null);
			railNode.setNorthEdge(temp);
			break;

		case SWITCH_LEFT_270:

			railNode = new DefaultRailNode();
			railNode.setModelFacade(modelFacade);
			railNode.setX(x);
			railNode.setY(y);
			railNode.setId(id);
			railNode.setShapeType(shapeType);
			railNode.setFeedbackBlockNumber(feedbackBlockNumber);
			railNode.setFlipped(flipped);

			graphNodeOne = graphNodeFactory.create(railNode);
			railNode.setGraphNodeOne(graphNodeOne);

			graphNodeTwo = graphNodeFactory.create(railNode);
			railNode.setGraphNodeTwo(graphNodeTwo);

			// north
			northEdge = new DefaultEdge();
			northEdge.setDirection(Direction.NORTH);
			railNode.setEdge(Direction.NORTH, northEdge);
			northEdge.setInGraphNode(graphNodeTwo);
//			northEdge.setOutGraphNode(graphNodeOne);
			northEdge.getOutGraphNodes().add(graphNodeOne);

			// south
			southEdge = new DefaultEdge();
			southEdge.setDirection(Direction.SOUTH);
			railNode.setEdge(Direction.SOUTH, southEdge);
			southEdge.setInGraphNode(graphNodeOne);
//			southEdge.setOutGraphNode(graphNodeTwo);
			southEdge.getOutGraphNodes().add(graphNodeTwo);

			// west
			westEdge = new DefaultEdge();
			westEdge.setDirection(Direction.WEST);
			railNode.setEdge(Direction.WEST, westEdge);
			westEdge.setInGraphNode(graphNodeTwo);
//			westEdge.setOutGraphNode(graphNodeOne);
			westEdge.getOutGraphNodes().add(graphNodeOne);

			break;

		case SWITCH_RIGHT_270:
			railNode = createFromParametersInternal(id, x, y, ShapeType.SWITCH_LEFT_270, feedbackBlockNumber, traverse,
					flipped);
			railNode.setShapeType(ShapeType.SWITCH_RIGHT_270);
			temp = railNode.getWestEdge();
			temp.setDirection(Direction.EAST);
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

	/**
	 * For testing.
	 *
	 * @param turnoutService
	 */
	public void setTurnoutService(final TurnoutService turnoutService) {
		this.turnoutService = turnoutService;
	}

}
