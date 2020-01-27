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
import de.wfb.model.node.DefaultRailNodeFactory;
import de.wfb.model.node.RailNode;
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
	public void testBlockedRailNodeRoute() throws IOException {

		final Model model = new DefaultModel();

//		Mockito.doNothing().when(modelService).sendModelChangedEvent(any(RailNode.class));
//		when(modelService.sendModelChangedEvent(any(RailNode.class))).doNothing();

		doNothing().when(modelService).sendModelChangedEvent(any(RailNode.class));
		modelService.setApplicationEventPublisher(applicationEventPublisher);
		modelService.setModel(model);

//		when(modelService)

//				Mockito.doNothing().when(turnoutService).queueStateRequest(any(RailNode.class));

		final DefaultRailNodeFactory nodePathFactory = new DefaultRailNodeFactory();
		nodePathFactory.setTurnoutService(turnoutService);
		final IdService idService = new DefaultIdService();

		final DefaultModelPersistenceService modelPersistenceService = new DefaultModelPersistenceService();
		modelPersistenceService.setNodeFactory(nodePathFactory);
		modelPersistenceService.setIdService(idService);
		modelPersistenceService.setModelService(modelService);

		final String path = "src/test/resources/DefaultRoutingServiceTest/model.json";
		modelPersistenceService.loadModel(model, path);

		model.connectModel();

		final DefaultRoutingService defaultRoutingService = new DefaultRoutingService();
		defaultRoutingService.setModelService(modelService);

		defaultRoutingService.buildRoutingTables();

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
	}

}
