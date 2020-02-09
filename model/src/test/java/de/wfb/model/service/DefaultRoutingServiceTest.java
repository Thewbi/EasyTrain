package de.wfb.model.service;

import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.model.DefaultModel;
import de.wfb.model.Model;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.DefaultRailNodeFactory;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.RailNode;
import de.wfb.rail.service.Route;
import de.wfb.rail.service.TurnoutService;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRoutingServiceTest {

	@Spy
	private DefaultModelService modelService;

	@Mock
	private TurnoutService turnoutService;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Test
	public void testLearning() throws Exception {

		final String path = "src/test/resources/DefaultRoutingServiceTest/modelLearning.json";
		final Model model = new DefaultModel();

		final DefaultRoutingService routingService = (DefaultRoutingService) buildRoutingTables(path, model);

//		for (final GraphNode switchingGraphNode : model.getSwitchingNodes()) {
//
//
//		}

		final GraphNode graphNodeById = routingService.getModelService().getGraphNodeById(2);
		System.out.println(graphNodeById);

	}

	@Test
	public void testRoutingTables() throws Exception {

		// final String path =
		// "src/test/resources/DefaultRoutingServiceTest/model.json";
		final String path = "../Startup/persistence/model.json";
		final Model model = new DefaultModel();

		final DefaultRoutingService defaultRoutingService = (DefaultRoutingService) buildRoutingTables(path, model);

		for (final GraphNode switchingGraphNode : model.getSwitchingNodes()) {

//			System.out.println(
//					"RN-ID: " + switchingGraphNode.getRailNode().getId() + " GN-ID: " + switchingGraphNode.getId());

			if (switchingGraphNode.getId() == 48) {

				System.out.println(switchingGraphNode.dumpSwitchingTable());
			}
		}

		final GraphNode graphNodeStart = model.getGraphNodeById(766);
		final GraphNode graphNodeEnd = model.getGraphNodeById(2412);
		final DefaultLocomotive locomotive = null;
		final boolean routeOverReservedGraphNodes = true;
		final boolean routeOverBlockedFeedbackBlocks = true;

		final Route route = defaultRoutingService.route(locomotive, graphNodeStart, graphNodeEnd,
				routeOverReservedGraphNodes, routeOverBlockedFeedbackBlocks);

		System.out.println(route);
	}

	@Test
	public void testBlockedRailNodeRoute() throws Exception {

		final String path = "src/test/resources/DefaultRoutingServiceTest/model.json";
		final Model model = new DefaultModel();

		final DefaultRoutingService defaultRoutingService = (DefaultRoutingService) buildRoutingTables(path, model);

		final RailNode railNode1 = (RailNode) modelService.getNodeById(1);
		final String routingTableAsString1 = railNode1.getGraphNodeOne().dumpRoutingTable();
		System.out.println("");
		System.out.println("Switching GraphNode " + railNode1.getGraphNodeOne().getId());
		System.out.println(routingTableAsString1);
		System.out.println("");
		System.out.println(railNode1.getGraphNodeOne().dumpSwitchingTable());

		final RailNode railNode4 = (RailNode) modelService.getNodeById(2);
		final String routingTableAsString4 = railNode4.getGraphNodeOne().dumpRoutingTable();
		System.out.println("");
		System.out.println("Switching GraphNode " + railNode4.getGraphNodeOne().getId());
		System.out.println(routingTableAsString4);
		System.out.println("");
		System.out.println(railNode4.getGraphNodeOne().dumpSwitchingTable());

		final RailNode railNodeStart = (RailNode) modelService.getNodeById(0);
		final RailNode railNodeEnd = (RailNode) modelService.getNodeById(8);

		final DefaultLocomotive locomotive = null;
		final boolean routeOverReservedNodes = false;
		final boolean routeOverBlockedFeedbackBlocks = false;
		final Route route = defaultRoutingService.route(locomotive, railNodeStart.getGraphNodeOne(),
				railNodeEnd.getGraphNodeOne(), routeOverReservedNodes, routeOverBlockedFeedbackBlocks);

		System.out.println(route);
	}

	private RoutingService buildRoutingTables(final String path, final Model model) throws IOException {

		doNothing().when(modelService).sendModelChangedEvent(any(RailNode.class));
		modelService.setApplicationEventPublisher(applicationEventPublisher);
		modelService.setModel(model);

		final DefaultRailNodeFactory nodePathFactory = new DefaultRailNodeFactory();
		nodePathFactory.setTurnoutService(turnoutService);
		final IdService idService = new DefaultIdService();

		final DefaultModelPersistenceService modelPersistenceService = new DefaultModelPersistenceService();
		modelPersistenceService.setNodeFactory(nodePathFactory);
		modelPersistenceService.setIdService(idService);
		modelPersistenceService.setModelService(modelService);
		modelPersistenceService.loadModel(model, path);

		model.connectModel();

//		final DefaultRoutingService defaultRoutingService = new DefaultRoutingService();
		final DefaultRoutingService routingService = new DefaultRoutingService();
		routingService.setModelService(modelService);
		routingService.buildRoutingTables();

		return routingService;
	}

}
