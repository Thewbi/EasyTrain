//package de.wfb.model.node;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import de.wfb.model.service.IdService;
//import de.wfb.rail.factory.Factory;
//import de.wfb.rail.ui.ShapeType;
//
//public class DefaultNodeFactory implements Factory<Node> {
//
//	@Autowired
//	private IdService idService;
//
//	@Override
//	public Node create(final Object... args) {
//
//		if (args[0] instanceof JsonNode) {
//
//			final JsonNode jsonNode = (JsonNode) args[0];
//			return populateFromJsonNode(jsonNode);
//
//		} else {
//
//			return populateFromParameters(args);
//
//		}
//	}
//
//	private Node populateFromParameters(final Object[] args) {
//
//		return null;
//
////		final int x = (Integer) args[0];
////		final int y = (Integer) args[1];
////		final ShapeType shapeType = (ShapeType) args[2];
////
////		int id = -1;
////		if (args.length >= 4) {
////			id = (Integer) args[3];
////		} else {
////			id = idService.getNextId();
////		}
////
////		// if a shape type is selected, add a node
////		final Node node = isTurnout(shapeType) ? new TurnoutNode() : new Node();
////		node.setId(id);
////		node.setX(x);
////		node.setY(y);
////		node.setShapeType(shapeType);
////		node.setHorizontal(retrieveHorizontal(shapeType));
////
////		return node;
//	}
//
//	private Boolean retrieveHorizontal(final ShapeType shapeType) {
//
//		switch (shapeType) {
//
//		case STRAIGHT_HORIZONTAL:
//			return true;
//
//		case STRAIGHT_VERTICAL:
//			return false;
//
//		default:
//			return null;
//		}
//
////		case SWITCH_LEFT_0:
////		case SWITCH_LEFT_180:
////		case SWITCH_RIGHT_0:
////		case SWITCH_RIGHT_180:
////		case TURN_BOTTOM_LEFT:
////		case TURN_LEFT_TOP:
////		case TURN_RIGHT_BOTTOM:
////		case TURN_TOP_RIGHT:
//
//	}
//
//	private boolean isTurnout(final ShapeType shapeType) {
//
//		switch (shapeType) {
//		case SWITCH:
//		case SWITCH_LEFT_0:
//		case SWITCH_LEFT_90:
//		case SWITCH_LEFT_180:
//		case SWITCH_LEFT_270:
//		case SWITCH_RIGHT_0:
//		case SWITCH_RIGHT_90:
//		case SWITCH_RIGHT_180:
//		case SWITCH_RIGHT_270:
//			return true;
//
//		default:
//			return false;
//		}
//	}
//
//}
