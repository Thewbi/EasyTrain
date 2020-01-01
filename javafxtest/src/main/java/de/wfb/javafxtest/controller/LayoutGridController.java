package de.wfb.javafxtest.controller;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import de.wfb.model.node.Node;
import de.wfb.model.service.ModelService;
import de.wfb.rail.events.SelectionEvent;
import de.wfb.rail.events.ShapeTypeChangedEvent;
import de.wfb.rail.ui.ShapeType;

@Component
public class LayoutGridController implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(LayoutGridController.class);

	// private ShapeType currentShapeType = ShapeType.STRAIGHT_HORIZONTAL;
	private ShapeType currentShapeType = ShapeType.NONE;

	@Autowired
	private ModelService modelService;

	/**
	 * The user clicked onto a square of the layout using the left mouse button.
	 */
	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.trace("onApplicationEvent " + event.getClass().getSimpleName());

		if (event instanceof SelectionEvent) {

			final SelectionEvent selectionEvent = (SelectionEvent) event;

			switch (currentShapeType) {

			case NONE:
				// if no shape type is selected, perform a simple click
				modelService.nodeClicked(selectionEvent.getX(), selectionEvent.getY());
				break;

			default:
				// if a shape type is selected, add a node

				// if a node of the same type is here already, return
				final Optional<Node> currentNode = modelService.getNode(selectionEvent.getX(), selectionEvent.getY());
				if (currentNode.isPresent() && currentNode.get().getShapeType() == currentShapeType) {

					logger.info("Not creating another node!");
					return;
				}

				modelService.addNode(selectionEvent.getX(), selectionEvent.getY(), currentShapeType);

				modelService.storeModel();
				break;
			}

		} else if (event instanceof ShapeTypeChangedEvent) {

			final ShapeTypeChangedEvent shapeTypeChangedEvent = (ShapeTypeChangedEvent) event;

			currentShapeType = shapeTypeChangedEvent.getShapeType();
		}
	}

}
