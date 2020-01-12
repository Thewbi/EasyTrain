package de.wfb.javafxtest.controls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import de.wfb.model.Model;
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

	private boolean shiftState;

	public void Initialize() {

		setMinSize(rows * cell_width, columns * cell_width);

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

				final SelectionEvent selectionEvent = new SelectionEvent(this, sceneX + " - " + sceneY, xIndex, yIndex,
						shiftState);

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

		final SVGPath svgPath = viewModel[nodeHighlightedEvent.getX()][nodeHighlightedEvent.getY()];
		if (svgPath != null) {

			logger.trace("changing fill color!");
			svgPath.setFill(nodeHighlightedEvent.isHighlighted() ? LayoutColors.HIGHLIGHT_FILL_COLOR
					: LayoutColors.STANDARD_FILL_COLOR);
		}
	}

	private void processModelChangedEvent(final ModelChangedEvent modelChangedEvent) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				final Model model = modelChangedEvent.getModel();
				final Node node = model.getNode(modelChangedEvent.getX(), modelChangedEvent.getY());

				logger.trace("onApplicationEvent() node = " + node);

				if (node == null) {

					// remove
					final SVGPath svgPathOld = viewModel[modelChangedEvent.getX()][modelChangedEvent.getY()];
					getChildren().remove(svgPathOld);

					return;
				}

				logger.trace("A");

				// if the new ShapeType is none, do not add a new shape but bail here
				final ShapeType shapeType = node.getShapeType();
				if (shapeType == ShapeType.NONE) {

					return;
				}

				logger.trace("B");

				// TODO: handle case, when only the color changed!

				// remove
				final SVGPath svgPathOld = viewModel[modelChangedEvent.getX()][modelChangedEvent.getY()];
				getChildren().remove(svgPathOld);

				// create new path
				final boolean turnoutState = turnoutState(node);
				logger.trace("TurnoutState: " + turnoutState);
				final boolean highlighted = modelChangedEvent.isHighlighted();
				final boolean blocked = modelChangedEvent.isBlocked();
				final boolean selected = modelChangedEvent.isSelected();

				try {
					final SVGPath svgPathNew = svgPathFactory.create(shapeType, cell_width, turnoutState, highlighted,
							blocked, selected);

					if (svgPathNew == null) {
						return;
					}
					svgPathNew.setLayoutX(modelChangedEvent.getX() * cell_width);
					svgPathNew.setLayoutY(modelChangedEvent.getY() * cell_width);

					getChildren().addAll(svgPathNew);

					if (node.getFeedbackBlockNumber() > -1) {

						final Text text = new Text(Integer.toString(node.getFeedbackBlockNumber()));
						text.setScaleX(0.5);
						text.setScaleY(0.5);

						if (shapeType == ShapeType.STRAIGHT_HORIZONTAL) {
							text.setLayoutX((modelChangedEvent.getX() + 0) * cell_width - 0);
							text.setLayoutY((modelChangedEvent.getY() + 1) * cell_width + 5);
						} else if (shapeType == ShapeType.STRAIGHT_VERTICAL) {
							text.setLayoutX((modelChangedEvent.getX() + 0) * cell_width - 4);
							text.setLayoutY((modelChangedEvent.getY() + 1) * cell_width + 0);
						}

						getChildren().addAll(text);
					}

					viewModel[modelChangedEvent.getX()][modelChangedEvent.getY()] = svgPathNew;
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		});
	}

	private boolean turnoutState(final Node node) {
		return node.isThrown();
	}

	public boolean isShiftState() {
		return shiftState;
	}

	public void setShiftState(final boolean shiftState) {
		this.shiftState = shiftState;
	}

}
