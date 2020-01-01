package de.wfb.javafxtest.controls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import de.wfb.model.Model;
import de.wfb.model.node.Node;
import de.wfb.model.node.TurnoutNode;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.events.SelectionEvent;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.ui.ShapeType;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

@Component
public class CustomGridPane extends Pane implements ApplicationListener<ModelChangedEvent> {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CustomGridPane.class);

	private final int columns = 100;

	private final int rows = 100;

	private final int cell_width = 10;

	double scale = 1.0d;

	@Autowired
	private Factory<SVGPath> svgPathFactory;

	/** https://www.baeldung.com/spring-events */
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private final SVGPath[][] viewModel = new SVGPath[rows][columns];

	public void Initialize() {

		setMinSize(rows * cell_width, columns * cell_width);

//		for (int j = 0; j < rows; j++) {
//
//			for (int i = 0; i < columns; i++) {
//
//				final SVGPath svgPath = svgPathFactory.create(ShapeType.SQUARE, cell_width);
//
//				// set position
//				svgPath.setLayoutX(cell_width * i);
//				svgPath.setLayoutY(cell_width * j);
//
//				viewModel[i][j] = svgPath;
//
//				getChildren().add(svgPath);
//			}
//		}

		final double scaleX = scale;
		final double scaleY = scale;

		// scaling transformation
		setScaleX(scaleX);
		setScaleY(scaleY);

		addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent e) {

				// returns horizontal position of the event relative to the origin of the Scene
				// that contains the MouseEvent's source.
				final double sceneX = e.getSceneX();
				final double sceneY = e.getSceneY();

				// absolute screen size. Returns absolute horizontal position of the event.
				final double screenX = e.getScreenX();
				final double screenY = e.getScreenY();

				// horizontal position of the event relative to the origin of the MouseEvent's
				// source.
				final double x = e.getX();
				final double y = e.getY();
				final double z = e.getZ();

				// DEBUG
				logger.trace("");
				logger.trace(" sceneX: " + sceneX + "  sceneY: " + sceneY);
				logger.trace("      X: " + x + "       Y: " + y + "       Z: " + z);
				logger.trace("screenX: " + screenX + " screenY: " + screenY);

				final int xIndex = (int) x / cell_width;
				final int yIndex = (int) y / cell_width;

				final SelectionEvent selectionEvent = new SelectionEvent(this, sceneX + " - " + sceneY, xIndex, yIndex);

				applicationEventPublisher.publishEvent(selectionEvent);
			}
		});
	}

	public void zoomIn() {

		scale += 0.1;

		final double scaleX = scale;
		final double scaleY = scale;

		setScaleX(scaleX);
		setScaleY(scaleY);
	}

	public void zoomOut() {

		scale -= 0.1;

		final double scaleX = scale;
		final double scaleY = scale;

		setScaleX(scaleX);
		setScaleY(scaleY);
	}

	@Override
	public void onApplicationEvent(final ModelChangedEvent event) {

		logger.trace("onApplicationEvent " + event.getClass().getSimpleName());

		final Model model = event.getModel();
		final Node node = model.getNode(event.getX(), event.getY());

		final boolean turnoutStraight = turnoutState(node);

		final ShapeType shapeType = node.getShapeType();
		if (shapeType == ShapeType.NONE) {
			return;
		}

		// replace the current node with a new one
		final SVGPath svgPathOld = viewModel[event.getX()][event.getY()];
		getChildren().remove(svgPathOld);

		// create new path
		final SVGPath svgPathNew = svgPathFactory.create(shapeType, cell_width, turnoutStraight);
		if (svgPathNew == null) {
			return;
		}
		svgPathNew.setLayoutX(event.getX() * cell_width);
		svgPathNew.setLayoutY(event.getY() * cell_width);

		getChildren().add(svgPathNew);

		viewModel[event.getX()][event.getY()] = svgPathNew;
	}

	private boolean turnoutState(final Node node) {

		if (node instanceof TurnoutNode) {
			final TurnoutNode turnoutNode = (TurnoutNode) node;
			return turnoutNode.isThrown();
		}

		return false;
	}

}
