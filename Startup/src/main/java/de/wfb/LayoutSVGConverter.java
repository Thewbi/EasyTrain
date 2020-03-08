package de.wfb;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import de.wfb.model.Model;
import de.wfb.model.node.Direction;
import de.wfb.model.node.Edge;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.converter.Converter;

public class LayoutSVGConverter implements Converter<Model, String> {

	private static final Logger logger = LogManager.getLogger(LayoutSVGConverter.class);

	private static final int BLOCK_WIDTH = 100;

	private static final int BLOCK_AMOUNT = 100;

	@Override
	public void convert(final Model model, final String pathToSVGFile) {

		final SVGGraphics2D svgGraphics2D = new SVGGraphics2D(BLOCK_AMOUNT * BLOCK_WIDTH, BLOCK_AMOUNT * BLOCK_WIDTH);
		for (final RailNode railNode : model.getAllRailNodes()) {

			// DEBUG
//			logger.trace(railNode.toString());

			final int x = railNode.getX() * BLOCK_WIDTH;
			final int y = railNode.getY() * BLOCK_WIDTH;

			final GraphNode graphNodeOne = railNode.getGraphNodeOne();
			final GraphNode graphNodeTwo = railNode.getGraphNodeTwo();

			assignCoordinates(railNode, x, y, graphNodeOne, graphNodeTwo);
		}

		for (final RailNode railNode : model.getAllRailNodes()) {

			for (int i = 0; i < 4; i++) {

				final Edge edge = railNode.getEdges()[i];
				renderEdge(edge, svgGraphics2D);
			}

			renderGraphNode(railNode.getGraphNodeOne(), 0, svgGraphics2D);
			renderGraphNode(railNode.getGraphNodeTwo(), 0, svgGraphics2D);
		}

		try {

			final String svgElement = svgGraphics2D.getSVGElement();

			final File outputFile = new File(pathToSVGFile);
			outputFile.getParentFile().mkdirs();

			SVGUtils.writeToSVG(outputFile, svgElement);

		} catch (final Exception e) {

			logger.error(e.getMessage(), e);

		} finally {

			if (svgGraphics2D != null) {
				svgGraphics2D.dispose();
			}
		}

	}

	private void assignCoordinates(final RailNode railNode, final int x, final int y, final GraphNode graphNodeOne,
			final GraphNode graphNodeTwo) {
		switch (railNode.getShapeType()) {

		case SIGNAL_HORIZONTAL:
		case STRAIGHT_HORIZONTAL:
			graphNodeOne.setX(x + 50);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 50);
			graphNodeTwo.setY(y + 80);
			break;

		case STRAIGHT_VERTICAL:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 50);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 50);
			break;

		case TURN_TOP_RIGHT:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 80);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 20);
			break;

		case TURN_RIGHT_BOTTOM:
			graphNodeOne.setX(x + 80);
			graphNodeOne.setY(y + 80);
			graphNodeTwo.setX(x + 20);
			graphNodeTwo.setY(y + 20);
			break;

		case TURN_BOTTOM_LEFT:
			graphNodeOne.setX(x + 80);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 20);
			graphNodeTwo.setY(y + 80);
			break;

		case TURN_LEFT_TOP:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 80);
			break;

		case SWITCH_LEFT_0:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 80);
			break;

		case SWITCH_LEFT_90:
			graphNodeOne.setX(x + 80);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 20);
			graphNodeTwo.setY(y + 80);
			break;

		case SWITCH_LEFT_180:
			graphNodeOne.setX(x + 80);
			graphNodeOne.setY(y + 80);
			graphNodeTwo.setX(x + 20);
			graphNodeTwo.setY(y + 20);
			break;

		case SWITCH_LEFT_270:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 80);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 20);
			break;

		case SWITCH_RIGHT_0:
			graphNodeOne.setX(x + 80);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 20);
			graphNodeTwo.setY(y + 80);
			break;

		case SWITCH_RIGHT_90:
			graphNodeOne.setX(x + 80);
			graphNodeOne.setY(y + 80);
			graphNodeTwo.setX(x + 20);
			graphNodeTwo.setY(y + 20);
			break;

		case SWITCH_RIGHT_180:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 80);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 20);
			break;

		case SWITCH_RIGHT_270:
			graphNodeOne.setX(x + 20);
			graphNodeOne.setY(y + 20);
			graphNodeTwo.setX(x + 80);
			graphNodeTwo.setY(y + 80);
			break;

		default:
			break;
		}
	}

	private void renderEdge(final Edge edge, final SVGGraphics2D svgGraphics2D) {

		if (edge == null) {
			return;
		}

//		logger.trace("Dir: " + edge.getDirection() + " OutGN: " + edge.getOutGraphNode().getId() + " NextOutGN: "
//				+ (edge.getNextOutGraphNode() == null ? "NULL" : edge.getNextOutGraphNode().getId()));

		final Direction direction = edge.getDirection();
		final GraphNode outGraphNode = edge.getOutGraphNodes().get(0);
		final GraphNode nextOutGraphNode = edge.getNextOutGraphNode();

		final int arrowLength = 4;

		if (edge.getNextOutGraphNode() == null) {
			return;
		}

		switch (direction) {

		case NORTH:
			svgGraphics2D.drawLine(outGraphNode.getX(), outGraphNode.getY(), nextOutGraphNode.getX(),
					nextOutGraphNode.getY());
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() - arrowLength, nextOutGraphNode.getY() + arrowLength);
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() + arrowLength, nextOutGraphNode.getY() + arrowLength);
			break;

		case EAST:
			svgGraphics2D.drawLine(outGraphNode.getX(), outGraphNode.getY(), nextOutGraphNode.getX(),
					nextOutGraphNode.getY());
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() - arrowLength, nextOutGraphNode.getY() - arrowLength);
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() - arrowLength, nextOutGraphNode.getY() + arrowLength);
			break;

		case SOUTH:
			svgGraphics2D.drawLine(outGraphNode.getX(), outGraphNode.getY(), nextOutGraphNode.getX(),
					nextOutGraphNode.getY());
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() - arrowLength, nextOutGraphNode.getY() - arrowLength);
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() + arrowLength, nextOutGraphNode.getY() - arrowLength);
			break;

		case WEST:
			svgGraphics2D.drawLine(outGraphNode.getX(), outGraphNode.getY(), nextOutGraphNode.getX(),
					nextOutGraphNode.getY());
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() + arrowLength, nextOutGraphNode.getY() - arrowLength);
			svgGraphics2D.drawLine(nextOutGraphNode.getX(), nextOutGraphNode.getY(),
					nextOutGraphNode.getX() + arrowLength, nextOutGraphNode.getY() + arrowLength);
			break;

		default:
			break;
		}

	}

	private void renderGraphNode(final GraphNode graphNode, final int yOffset, final SVGGraphics2D svgGraphics2D) {

		final String graphNodeId = Integer.toString(graphNode.getId());
		final String railNodeId = "[" + graphNode.getRailNode().getId() + "]";

		svgGraphics2D.drawString(graphNodeId, graphNode.getX() + 6, graphNode.getY() - 3 + yOffset);
		svgGraphics2D.drawString(railNodeId, graphNode.getX() + 6, graphNode.getY() - 17 + yOffset);

	}

	@Override
	public String convert(final Model source) {
		throw new RuntimeException("Not implemented yet!");
	}

}
