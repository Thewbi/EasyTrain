package de.wbi.model.serializer;

import org.junit.Test;

import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.rail.service.Route;
import junit.framework.Assert;

public class DefaultRouteSerializerTest {

	@Test
	public void testSerialize() {

		final DefaultLocomotive locomotive = new DefaultLocomotive();
		locomotive.setId(99);

		final Route route = new Route();
		route.setLocomotive(locomotive);

		GraphNode graphNode = new GraphNode();
		graphNode.setId(1);
		route.getGraphNodes().add(graphNode);

		graphNode = new GraphNode();
		graphNode.setId(2);
		route.getGraphNodes().add(graphNode);

		graphNode = new GraphNode();
		graphNode.setId(3);
		route.getGraphNodes().add(graphNode);

		graphNode = new GraphNode();
		graphNode.setId(4);
		route.getGraphNodes().add(graphNode);

		graphNode = new GraphNode();
		graphNode.setId(5);
		route.getGraphNodes().add(graphNode);

		final DefaultRouteSerializer serializer = new DefaultRouteSerializer();
		final String convert = serializer.convert(route);

		System.out.println(convert);

		Assert.assertEquals("99|1,2,3,4,5", convert);
	}

}
