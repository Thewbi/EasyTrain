package de.wfb.model.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.wfb.model.DefaultModel;
import de.wfb.model.Model;
import de.wfb.model.service.DefaultIdService;
import de.wfb.model.service.IdService;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;

public class StraightHorizontalRailNodeTest {

	@Test
	public void connectTest_East_Turn_Left_Top() throws Exception {

		final Factory<Node> railNodeFactory = new DefaultRailNodeFactory();

		final RailNode horizontalRailNode = (RailNode) railNodeFactory.create(1, 1, ShapeType.STRAIGHT_HORIZONTAL);
		final RailNode turnLeftTopRailNode = (RailNode) railNodeFactory.create(2, 1, ShapeType.TURN_LEFT_TOP);

		final Model model = new DefaultModel();
		model.setNode(1, 1, horizontalRailNode);
		model.setNode(2, 1, turnLeftTopRailNode);

		horizontalRailNode.connect(model);
		turnLeftTopRailNode.connect(model);

		System.out.println(horizontalRailNode.toString());
		System.out.println(turnLeftTopRailNode.toString());

		assertEquals(1, horizontalRailNode.getGraphNodeOne().getChildren().size());
		assertEquals(0, horizontalRailNode.getGraphNodeTwo().getChildren().size());

		assertEquals(0, horizontalRailNode.getGraphNodeOne().getId());
		assertEquals(2, horizontalRailNode.getGraphNodeOne().getChildren().get(0).getId());

		assertEquals(0, turnLeftTopRailNode.getGraphNodeOne().getChildren().size());
		assertEquals(1, turnLeftTopRailNode.getGraphNodeTwo().getChildren().size());

		assertEquals(3, turnLeftTopRailNode.getGraphNodeTwo().getId());
		assertEquals(1, turnLeftTopRailNode.getGraphNodeTwo().getChildren().get(0).getId());
	}

	@Test
	public void disconnectTest() throws Exception {

		final IdService idService = new DefaultIdService();

		final DefaultRailNodeFactory railNodeFactory = new DefaultRailNodeFactory();
		railNodeFactory.setIdService(idService);

		final RailNode horizontalRailNode = (RailNode) railNodeFactory.create(1, 1, ShapeType.STRAIGHT_HORIZONTAL);
		final RailNode horizontalRailNodeEast = (RailNode) railNodeFactory.create(2, 1, ShapeType.STRAIGHT_HORIZONTAL);

		final Model model = new DefaultModel();
		model.setNode(1, 1, horizontalRailNode);
		model.setNode(2, 1, horizontalRailNodeEast);

		horizontalRailNode.connect(model);
		horizontalRailNodeEast.connect(model);

		System.out.println(horizontalRailNode.toString());
		System.out.println(horizontalRailNodeEast.toString());

		horizontalRailNodeEast.disconnect(model);

		System.out.println(horizontalRailNode.toString());
		System.out.println(horizontalRailNodeEast.toString());

	}
}
