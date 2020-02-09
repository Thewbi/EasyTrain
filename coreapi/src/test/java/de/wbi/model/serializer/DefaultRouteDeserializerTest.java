package de.wbi.model.serializer;

import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.node.GraphNode;
import de.wfb.rail.service.Route;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRouteDeserializerTest {

	@Mock
	private ModelFacade modelFacade;

	@Test
	public void testDeserialize() {

		final GraphNode graphNode1 = mock(GraphNode.class);
		when(graphNode1.getId()).thenReturn(1);
		when(modelFacade.getGraphNodeById(1)).thenReturn(graphNode1);

		final GraphNode graphNode2 = mock(GraphNode.class);
		when(graphNode2.getId()).thenReturn(2);
		when(modelFacade.getGraphNodeById(2)).thenReturn(graphNode2);

		final GraphNode graphNode3 = mock(GraphNode.class);
		when(graphNode3.getId()).thenReturn(3);
		when(modelFacade.getGraphNodeById(3)).thenReturn(graphNode3);

		final GraphNode graphNode4 = mock(GraphNode.class);
		when(graphNode4.getId()).thenReturn(4);
		when(modelFacade.getGraphNodeById(4)).thenReturn(graphNode4);

		final GraphNode graphNode5 = mock(GraphNode.class);
		when(graphNode5.getId()).thenReturn(5);
		when(modelFacade.getGraphNodeById(5)).thenReturn(graphNode5);

		final DefaultRouteDeserializer deserializer = new DefaultRouteDeserializer();
		deserializer.setModelFacade(modelFacade);

		final Route route = deserializer.convert("99|1,2,3,4,5");

		assertEquals(99, route.getLocomotiveId());

		assertEquals(1, route.getGraphNodes().get(0).getId());
		assertEquals(2, route.getGraphNodes().get(1).getId());
		assertEquals(3, route.getGraphNodes().get(2).getId());
		assertEquals(4, route.getGraphNodes().get(3).getId());
		assertEquals(5, route.getGraphNodes().get(4).getId());
	}

}
