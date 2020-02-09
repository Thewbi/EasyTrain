package de.wfb.javafxtest.controls;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import de.wfb.model.GridElement;
import de.wfb.model.ViewModel;
import de.wfb.model.facade.ModelFacade;
import de.wfb.model.node.Node;
import de.wfb.rail.events.ModelChangedEvent;
import de.wfb.rail.events.NodeHighlightedEvent;
import de.wfb.rail.events.SelectionEvent;
import de.wfb.rail.factory.Factory;
import de.wfb.rail.factory.LayoutColors;
import de.wfb.rail.ui.ShapeType;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

@Component
public class CustomGridPane extends Pane implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(CustomGridPane.class);

	private final int CELL_WIDTH = 10;

	double scale = 1.0d;

	@Autowired
	private Factory<GridElement<SVGPath, Text>> gridElementFactory;

	/** https://www.baeldung.com/spring-events */
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private ViewModel<SVGPath, Text> viewModel;

	private boolean shiftState;

	public void Initialize() {

		setMinSize(ViewModel.ROWS * CELL_WIDTH, ViewModel.COLUMNS * CELL_WIDTH);

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

				final int xIndex = (int) x / CELL_WIDTH;
				final int yIndex = (int) y / CELL_WIDTH;

				final SelectionEvent selectionEvent = new SelectionEvent(this, sceneX + " - " + sceneY, xIndex, yIndex,
						shiftState);

				logger.trace("Sending SelectionEvent ...");
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

	/**
	 * Handler for model changed event. The grid pane updates accordingly.
	 */
	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		logger.trace("onApplicationEvent " + event.getClass().getSimpleName());

		if (event instanceof ModelChangedEvent) {

			final ModelChangedEvent modelChangedEvent = (ModelChangedEvent) event;
			processModelChangedEvent(modelChangedEvent);

		} else if (event instanceof NodeHighlightedEvent) {

			final NodeHighlightedEvent nodeHighlightedEvent = (NodeHighlightedEvent) event;
			processNodeHighlightedEvent(nodeHighlightedEvent);

		}
	}

	private void processNodeHighlightedEvent(final NodeHighlightedEvent nodeHighlightedEvent) {

		logger.trace(nodeHighlightedEvent);

		final GridElement<SVGPath, Text> gridElement = viewModel.getViewModel()[nodeHighlightedEvent
				.getX()][nodeHighlightedEvent.getY()];

		final SVGPath svgPath = gridElement.getPath();
		if (svgPath != null) {

			logger.trace("changing fill color!");
			svgPath.setFill(nodeHighlightedEvent.isHighlighted() ? LayoutColors.HIGHLIGHT_FILL_COLOR
					: LayoutColors.STANDARD_FILL_COLOR);
		}
	}

	private void processModelChangedEvent(final ModelChangedEvent modelChangedEvent) {

		logger.trace("processModelChangedEvent()");

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				final int x = modelChangedEvent.getX();
				final int y = modelChangedEvent.getY();

				logger.trace("x = " + y + " y = " + y);

				final Optional<Node> nodeOptional = modelFacade.getNode(x, y);

				if (nodeOptional.isEmpty()) {

					logger.info("Node is null!");

					// remove
					final GridElement<SVGPath, Text> gridElement = viewModel.getViewModel()[x][y];

					if (gridElement == null) {
						return;
					}

					logger.trace("gridElement = " + gridElement);

					final SVGPath svgPathOld = gridElement.getPath();
					if (svgPathOld != null) {
						logger.info("Removeing");
						getChildren().remove(svgPathOld);
					}

					final Text text = gridElement.getText();
					if (text != null) {
						logger.info("Removeing");
						getChildren().remove(text);
					}

					return;
				}

				final Node node = nodeOptional.get();

				// if the new ShapeType is none, do not add a new shape but bail here
				final ShapeType shapeType = node.getShapeType();
				if (shapeType == ShapeType.NONE) {

					logger.info("shapeType is none!");

					return;
				}

				// remove
				final GridElement<SVGPath, Text> tempGridElement = viewModel.getViewModel()[x][y];
				if (tempGridElement != null) {

					logger.trace("Removing x: " + x + " y: " + y);

					// remove SVGPath
					final SVGPath svgPathOld = tempGridElement.getPath();
					if (svgPathOld != null) {
						getChildren().remove(svgPathOld);
					}

					// remove Text
					final Text oldText = tempGridElement.getText();
					if (oldText != null) {
						getChildren().remove(oldText);
					}
				}

				try {

					// adding new element
					final GridElement<SVGPath, Text> gridElement = gridElementFactory.create(node, modelChangedEvent,
							shapeType, CELL_WIDTH);
					if (gridElement != null) {

						logger.trace("Adding x: " + x + " y: " + y);

						if (gridElement.getPath() != null) {
							getChildren().addAll(gridElement.getPath());
						}

						if (gridElement.getText() != null) {
							getChildren().addAll(gridElement.getText());
						}

						viewModel.getViewModel()[x][y] = gridElement;
					}

				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	public boolean isShiftState() {
		return shiftState;
	}

	public void setShiftState(final boolean shiftState) {
		this.shiftState = shiftState;
	}

}
