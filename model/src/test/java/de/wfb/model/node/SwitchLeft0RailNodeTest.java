package de.wfb.model.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.wfb.model.DefaultModel;
import de.wfb.model.Model;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

public class SwitchLeft0RailNodeTest {

	/**
	 * SwitchLeft0 is connected to a horizontal straight in east direction
	 *
	 * @throws Exception
	 */
	@Test
	public void connectTest_East_HorizontalStraight() throws Exception {

		final Factory<Node> railNodeFactory = new DefaultRailNodeFactory();

		final RailNode switchLeft0RailNode = (RailNode) railNodeFactory.create(1, 1, ShapeType.SWITCH_LEFT_0);
		final RailNode horizontalRailNode = (RailNode) railNodeFactory.create(0, 1, ShapeType.STRAIGHT_HORIZONTAL);

		final Model model = new DefaultModel();
		model.setNode(1, 1, switchLeft0RailNode);
		model.setNode(0, 1, horizontalRailNode);

		switchLeft0RailNode.connect(model);
		horizontalRailNode.connect(model);

		System.out.println(switchLeft0RailNode.toString());
		System.out.println(horizontalRailNode.toString());

		assertEquals(0, switchLeft0RailNode.getGraphNodeOne().getChildren().size());
		assertEquals(1, switchLeft0RailNode.getGraphNodeTwo().getChildren().size());
		assertEquals(3, switchLeft0RailNode.getGraphNodeTwo().getChildren().get(0).getId());

		assertEquals(1, horizontalRailNode.getGraphNodeOne().getChildren().size());
		assertEquals(0, horizontalRailNode.getGraphNodeTwo().getChildren().size());
		assertEquals(0, horizontalRailNode.getGraphNodeOne().getChildren().get(0).getId());
	}

	/**
	 * SwitchLeft0 is connected to a horizontal straight in west direction
	 *
	 * @throws Exception
	 */
	@Test
	public void connectTest_West_HorizontalStraight() throws Exception {

		final Factory<Node> railNodeFactory = new DefaultRailNodeFactory();

		final RailNode switchLeft0RailNode = (RailNode) railNodeFactory.create(1, 1, ShapeType.SWITCH_LEFT_0);
		final RailNode horizontalRailNode = (RailNode) railNodeFactory.create(2, 1, ShapeType.STRAIGHT_HORIZONTAL);

		final Model model = new DefaultModel();
		model.setNode(1, 1, switchLeft0RailNode);
		model.setNode(2, 1, horizontalRailNode);

		switchLeft0RailNode.connect(model);
		horizontalRailNode.connect(model);

		System.out.println(switchLeft0RailNode.toString());
		System.out.println(horizontalRailNode.toString());

		assertEquals(1, switchLeft0RailNode.getGraphNodeOne().getChildren().size());
		assertEquals(0, switchLeft0RailNode.getGraphNodeTwo().getChildren().size());
		assertEquals(2, switchLeft0RailNode.getGraphNodeOne().getChildren().get(0).getId());

		assertEquals(0, horizontalRailNode.getGraphNodeOne().getChildren().size());
		assertEquals(1, horizontalRailNode.getGraphNodeTwo().getChildren().size());
		assertEquals(1, horizontalRailNode.getGraphNodeTwo().getChildren().get(0).getId());
	}

	/**
	 * SwitchLeft0 is connected to a vertical straight in north direction
	 *
	 * @throws Exception
	 */
	@Test
	public void connectTest_North_VerticalStraight() throws Exception {

		final Factory<Node> railNodeFactory = new DefaultRailNodeFactory();

		final RailNode switchLeft0RailNode = (RailNode) railNodeFactory.create(1, 1, ShapeType.SWITCH_LEFT_0);
		final RailNode horizontalRailNode = (RailNode) railNodeFactory.create(1, 0, ShapeType.STRAIGHT_VERTICAL);

		final Model model = new DefaultModel();
		model.setNode(1, 1, switchLeft0RailNode);
		model.setNode(1, 0, horizontalRailNode);

		switchLeft0RailNode.connect(model);
		horizontalRailNode.connect(model);

		System.out.println(switchLeft0RailNode.toString());
		System.out.println(horizontalRailNode.toString());

		assertEquals(1, switchLeft0RailNode.getGraphNodeOne().getChildren().size());
		assertEquals(0, switchLeft0RailNode.getGraphNodeTwo().getChildren().size());
		assertEquals(2, switchLeft0RailNode.getGraphNodeOne().getChildren().get(0).getId());

		assertEquals(0, horizontalRailNode.getGraphNodeOne().getChildren().size());
		assertEquals(1, horizontalRailNode.getGraphNodeTwo().getChildren().size());
		assertEquals(1, horizontalRailNode.getGraphNodeTwo().getChildren().get(0).getId());
	}

	/**
	 * SwitchLeft0 is connected to a horizontal straight in north direction. No
	 * connection is established.
	 *
	 * @throws Exception
	 */
	@Test
	public void connectTest_North_HorizontalStraight() throws Exception {

		final Factory<Node> railNodeFactory = new DefaultRailNodeFactory();

		final RailNode switchLeft0RailNode = (RailNode) railNodeFactory.create(1, 1, ShapeType.SWITCH_LEFT_0);
		final RailNode horizontalRailNode = (RailNode) railNodeFactory.create(1, 0, ShapeType.STRAIGHT_HORIZONTAL);

		final Model model = new DefaultModel();
		model.setNode(1, 1, switchLeft0RailNode);
		model.setNode(1, 0, horizontalRailNode);

		switchLeft0RailNode.connect(model);
		horizontalRailNode.connect(model);

		System.out.println(switchLeft0RailNode.toString());
		System.out.println(horizontalRailNode.toString());

		assertEquals(0, switchLeft0RailNode.getGraphNodeOne().getChildren().size());
		assertEquals(0, switchLeft0RailNode.getGraphNodeTwo().getChildren().size());

		assertEquals(0, horizontalRailNode.getGraphNodeOne().getChildren().size());
		assertEquals(0, horizontalRailNode.getGraphNodeTwo().getChildren().size());
	}

}
