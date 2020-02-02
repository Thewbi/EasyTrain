package de.wfb.javafxtest.controls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.GridElement;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Node;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class GridElementFactory implements Factory<GridElement<SVGPath, Text>> {

	private static final Logger logger = LogManager.getLogger(GridElementFactory.class);

	@Autowired
	private Factory<SVGPath> svgPathFactory;

	@Override
	public GridElement<SVGPath, Text> create(final Object... args) throws Exception {

		final Node node = (Node) args[0];
		final ModelChangedEvent modelChangedEvent = (ModelChangedEvent) args[1];
		final ShapeType shapeType = (ShapeType) args[2];
		final int cellWidth = (int) args[3];

		// create new path
		final boolean thrown = node.isThrown();
		final boolean flipped = node.isFlipped() == null ? false : node.isFlipped();
		final Direction direction = node.getTraverse();
		final boolean highlighted = modelChangedEvent.isHighlighted();
		final boolean blocked = modelChangedEvent.isBlocked();
		final boolean selected = modelChangedEvent.isSelected();
		final boolean reserved = modelChangedEvent.isReserved();

		try {

			// @formatter:off

			// DEBUG
			logger.trace(
					"ProtocolTurnoutID: " + node.getProtocolTurnoutId() +
//					" TurnoutState: " + (thrown ? "THROWN" : "CLOSED") +
					" ShapeType: " + shapeType +
					" highlighted: " + highlighted +
					" blocked: " + blocked +
					" selected: " + selected +
					" reserved: " + reserved);

			final SVGPath svgPathNew = svgPathFactory.create(
					shapeType,
					cellWidth,
					thrown,
					flipped,
					highlighted,
					blocked,
					selected,
					reserved,
					direction);

			// @formatter:on

			if (svgPathNew == null) {

				logger.info("svgPathNew is null!");
				return null;
			}

			svgPathNew.setLayoutX(modelChangedEvent.getX() * cellWidth);
			svgPathNew.setLayoutY(modelChangedEvent.getY() * cellWidth);

			// render the feedback block number onto the layout
			Text text = null;
			if (node.getFeedbackBlockNumber() != null && node.getFeedbackBlockNumber() > -1) {

				text = new Text(Integer.toString(node.getFeedbackBlockNumber()));
				text.setScaleX(0.5);
				text.setScaleY(0.5);

				double x = 0;
				double y = 0;

				if (shapeType == ShapeType.STRAIGHT_HORIZONTAL) {

					x = (modelChangedEvent.getX() + 0) * cellWidth - 0;
					y = (modelChangedEvent.getY() + 1) * cellWidth + 5;

				} else if (shapeType == ShapeType.STRAIGHT_VERTICAL) {

					x = (modelChangedEvent.getX() + 0) * cellWidth + 4;
					y = (modelChangedEvent.getY() + 1) * cellWidth + 0;

				} else if (shapeType == ShapeType.TURN_TOP_RIGHT) {

					x = (modelChangedEvent.getX() + 0) * cellWidth - 0;
					y = (modelChangedEvent.getY() + 1) * cellWidth + 5;

				} else if (shapeType == ShapeType.TURN_RIGHT_BOTTOM) {

					x = (modelChangedEvent.getX() + 0) * cellWidth - 0;
					y = (modelChangedEvent.getY() + 1) * cellWidth + 5;

				} else if (shapeType == ShapeType.TURN_BOTTOM_LEFT) {

					x = (modelChangedEvent.getX() + 0) * cellWidth - 0;
					y = (modelChangedEvent.getY() + 1) * cellWidth + 5;

				} else if (shapeType == ShapeType.TURN_LEFT_TOP) {

					x = (modelChangedEvent.getX() + 0) * cellWidth - 0;
					y = (modelChangedEvent.getY() + 1) * cellWidth + 5;

				}

				logger.trace("X: " + y + " Y: " + y + " shapeType: " + shapeType + " node.getFeedbackBlockNumber() "
						+ node.getFeedbackBlockNumber());

				text.setLayoutX(x);
				text.setLayoutY(y);
			}

			final GridElement<SVGPath, Text> gridElement = new DefaultGridElement(svgPathNew, text);

			return gridElement;

		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}
}
