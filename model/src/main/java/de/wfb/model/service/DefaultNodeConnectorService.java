package de.wfb.model.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.node.Node;

public class DefaultNodeConnectorService implements NodeConnectorService {

	private static final Logger logger = LogManager.getLogger(DefaultNodeConnectorService.class);

	@Autowired
	private ModelService modelService;

	@Override
	public void connect(final Node node) {

		logger.info("DefaultNodeConnectorService.connect() shapeType = " + node.getShapeType());

		switch (node.getShapeType()) {

		case STRAIGHT_HORIZONTAL:
			connectStraightHorizontal(node);
			break;

		case STRAIGHT_VERTICAL:
			connectStraightVertical(node);
			break;

		case TURN_LEFT_TOP:
			connectTurnLeftTop(node);
			break;

		case TURN_TOP_RIGHT:
			connectTurnTopRight(node);
			break;

		case TURN_RIGHT_BOTTOM:
			connectTurnRightBottom(node);
			break;

		case TURN_BOTTOM_LEFT:
			connectTurnBottomLeft(node);
			break;

		case SWITCH_LEFT_0:
			connectSwitchLeft0(node);
			break;

		case SWITCH_LEFT_90:
			connectSwitchLeft90(node);
			break;

		case SWITCH_LEFT_180:
			connectSwitchLeft180(node);
			break;

		case SWITCH_LEFT_270:
			connectSwitchLeft270(node);
			break;

		case SWITCH_RIGHT_0:
			connectSwitchRight0(node);
			break;

		case SWITCH_RIGHT_90:
			connectSwitchRight90(node);
			break;

		case SWITCH_RIGHT_180:
			connectSwitchRight180(node);
			break;

		case SWITCH_RIGHT_270:
			connectSwitchRight270(node);
			break;

		default:
			break;

		}
	}

	private void connectSwitchLeft0(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().add(topNode);
			topNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void connectSwitchLeft90(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x, y + 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().add(topNode);
			topNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void connectSwitchLeft180(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> bottomNodeOptional = modelService.getNode(x, y + 1);
		if (bottomNodeOptional.isPresent()) {

			final Node rightNode = bottomNodeOptional.get();
			node.getLeftList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}
	}

	private void connectSwitchLeft270(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getLeftList().add(topNode);
			topNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + topNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

		// bottom
		final Optional<Node> bottomNodeOptional = modelService.getNode(x, y + 1);
		if (bottomNodeOptional.isPresent()) {

			final Node bottomNode = bottomNodeOptional.get();
			node.getRightList().add(bottomNode);
			bottomNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + bottomNode.getId());
		}
	}

	private void connectSwitchRight0(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> bottomNodeOptional = modelService.getNode(x, y + 1);
		if (bottomNodeOptional.isPresent()) {

			final Node bottomNode = bottomNodeOptional.get();
			node.getRightList().add(bottomNode);
			bottomNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + bottomNode.getId());
		}
	}

	private void connectSwitchRight90(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getLeftList().add(topNode);
			topNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + topNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

		// bottom
		final Optional<Node> bottomNodeOptional = modelService.getNode(x, y + 1);
		if (bottomNodeOptional.isPresent()) {

			final Node bottomNode = bottomNodeOptional.get();
			node.getRightList().add(bottomNode);
			bottomNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + bottomNode.getId());
		}
	}

	private void connectSwitchRight180(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getLeftList().add(topNode);
			topNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + topNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}
	}

	private void connectSwitchRight270(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// bottom
		final Optional<Node> bottomNodeOptional = modelService.getNode(x, y + 1);
		if (bottomNodeOptional.isPresent()) {

			final Node bottomNode = bottomNodeOptional.get();
			node.getRightList().add(bottomNode);
			bottomNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + bottomNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getLeftList().add(topNode);
			topNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	@Override
	public void disconnect(final Node node) {

		logger.info("DefaultNodeConnectorService.disconnect() shapeType = " + node.getShapeType());

		switch (node.getShapeType()) {

		case STRAIGHT_HORIZONTAL:
			disconnectStraightHorizontal(node);
			break;

		case STRAIGHT_VERTICAL:
			disconnectStraightVertical(node);
			break;

		case TURN_LEFT_TOP:
			disconnectTurnLeftTop(node);
			break;

		case TURN_TOP_RIGHT:
			disconnectTurnTopRight(node);
			break;

		case TURN_RIGHT_BOTTOM:
			disconnectTurnRightBottom(node);
			break;

		case TURN_BOTTOM_LEFT:
			disconnectTurnBottomLeft(node);
			break;

		case SWITCH_LEFT_0:
			disconnectSwitchLeft0(node);
			break;

		case SWITCH_LEFT_90:
			disconnectSwitchLeft90(node);
			break;

		case SWITCH_LEFT_180:
			disconnectSwitchLeft180(node);
			break;

		case SWITCH_LEFT_270:
			disconnectSwitchLeft270(node);
			break;

		case SWITCH_RIGHT_0:
			disconnectSwitchRight0(node);
			break;

		case SWITCH_RIGHT_90:
			disconnectSwitchRight90(node);
			break;

		case SWITCH_RIGHT_180:
			disconnectSwitchRight180(node);
			break;

		case SWITCH_RIGHT_270:
			disconnectSwitchRight270(node);
			break;

		default:
			break;

		}
	}

	private void disconnectTurnLeftTop(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y - 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + rightNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + leftNode.getId());
		}

		node.getLeftList().clear();
		node.getRightList().clear();
	}

	private void disconnectTurnTopRight(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y - 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + rightNode.getId());
		}

		// right
		final Optional<Node> leftNodeOptional = modelService.getNode(x + 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + leftNode.getId());
		}

		node.getLeftList().clear();
		node.getRightList().clear();
	}

	private void disconnectTurnRightBottom(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + leftNode.getId());
		}

		node.getLeftList().clear();
		node.getRightList().clear();
	}

	private void disconnectTurnBottomLeft(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// bottom
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y + 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + rightNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + leftNode.getId());
		}

		node.getLeftList().clear();
		node.getRightList().clear();
	}

	private void disconnectStraightHorizontal(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + rightNode.getId());
		}

		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + leftNode.getId());
		}

		node.getLeftList().clear();
		node.getRightList().clear();
	}

	private void disconnectStraightVertical(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y + 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " from node " + leftNode.getId());
		}

		node.getLeftList().clear();
		node.getRightList().clear();
	}

	private void connectTurnLeftTop(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y - 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

	}

	private void connectTurnTopRight(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// top
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y - 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// right
		final Optional<Node> leftNodeOptional = modelService.getNode(x + 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}
	}

	private void connectTurnRightBottom(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y + 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}
	}

	private void connectTurnBottomLeft(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// bottom
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y + 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}
	}

	private void connectStraightHorizontal(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

	}

	private void connectStraightVertical(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		final Optional<Node> rightNodeOptional = modelService.getNode(x, y + 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().add(rightNode);
			rightNode.getLeftList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + rightNode.getId());
		}

		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().add(leftNode);
			leftNode.getRightList().add(node);

			logger.info("Connected node " + node.getId() + " to node " + leftNode.getId());
		}

	}

	private void disconnectSwitchLeft0(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchLeft90(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x, y + 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchLeft180(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x + 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y + 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x + 1, y);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchLeft270(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchRight0(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x - 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// top
		final Optional<Node> topNodeOptional = modelService.getNode(x, y + 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchRight90(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y - 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x - 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x, y + 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchRight180(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x + 1, y);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x, y - 1);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x + 1, y);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

	private void disconnectSwitchRight270(final Node node) {

		final int x = node.getX();
		final int y = node.getY();

		// left
		final Optional<Node> leftNodeOptional = modelService.getNode(x, y + 1);
		if (leftNodeOptional.isPresent()) {

			final Node leftNode = leftNodeOptional.get();
			node.getLeftList().remove(leftNode);
			leftNode.getRightList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + leftNode.getId());
		}

		// right
		final Optional<Node> rightNodeOptional = modelService.getNode(x + 1, y);
		if (rightNodeOptional.isPresent()) {

			final Node rightNode = rightNodeOptional.get();
			node.getRightList().remove(rightNode);
			rightNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + rightNode.getId());
		}

		// bottom
		final Optional<Node> topNodeOptional = modelService.getNode(x, y - 1);
		if (topNodeOptional.isPresent()) {

			final Node topNode = topNodeOptional.get();
			node.getRightList().remove(topNode);
			topNode.getLeftList().remove(node);

			logger.info("Disconnected node " + node.getId() + " to node " + topNode.getId());
		}
	}

}
