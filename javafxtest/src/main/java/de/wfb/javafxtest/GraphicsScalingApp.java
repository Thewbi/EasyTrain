package de.wfb.javafxtest;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;

public class GraphicsScalingApp extends Application {

	private Stage stage;

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) {

		final Group group = createGroup();

		final Parent zoomPane = createZoomPane(group);

		final VBox layout = new VBox();
		layout.getChildren().setAll(createMenuBar(stage, group), zoomPane);

		VBox.setVgrow(zoomPane, Priority.ALWAYS);

		final Scene scene = new Scene(layout);

		stage.setTitle("Zoomy ScaleX: " + group.getScaleX() + " ScaleY: " + group.getScaleY());
		stage.getIcons().setAll(new Image(APP_ICON));
		stage.setScene(scene);
		stage.show();

		this.stage = stage;
	}

	private Parent createZoomPane(final Group group) {

		final double SCALE_DELTA = 1.1;

		final StackPane stackPane = new StackPane();
		stackPane.getChildren().add(group);
		stackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent e) {

				System.out.println(e.getSource());

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

				// System.out.println("MouseClick");
				System.out.println("");
				System.out.println(" sceneX: " + sceneX + "  sceneY: " + sceneY);
				System.out.println("      X: " + x + "       Y: " + y + "       Z: " + z);
				System.out.println("screenX: " + screenX + " screenY: " + screenY);
			}
		});

		final Group scrollContentGroup = new Group(stackPane);

		final ScrollPane scrollPane = new ScrollPane();

		scrollPane.setContent(scrollContentGroup);

		scrollPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
			@Override
			public void changed(final ObservableValue<? extends Bounds> observable, final Bounds oldValue,
					final Bounds newValue) {
				stackPane.setMinSize(newValue.getWidth(), newValue.getHeight());
			}
		});

		final int width_height = 256;

		final int width = 800;
		final int height = 600;

		scrollPane.setPrefViewportWidth(width);
		scrollPane.setPrefViewportHeight(height);

		stackPane.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(final ScrollEvent event) {

				event.consume();

				if (event.getDeltaY() == 0) {
					return;
				}

				// either shrink or enlarge depending of mouse wheel direction
				final double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;

				group.setScaleX(group.getScaleX() * scaleFactor);
				group.setScaleY(group.getScaleY() * scaleFactor);

				stage.setTitle("Zoomy ScaleX: " + group.getScaleX() + " ScaleY: " + group.getScaleY());

				// amount of scrolling in each direction in scrollContent coordinate
				// units
				final Point2D scrollOffset = figureScrollOffset(scrollContentGroup, scrollPane);

				// move viewport so that old center remains in the center after the
				// scaling
				repositionScroller(scrollContentGroup, scrollPane, scaleFactor, scrollOffset);
			}
		});

		// Panning via drag....
		final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<Point2D>();
		scrollContentGroup.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent event) {

				lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
			}
		});

		scrollContentGroup.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent event) {

				final Point2D lastMouseCoordinate = lastMouseCoordinates.get();

				final double deltaX = event.getX() - lastMouseCoordinate.getX();
				final double extraWidth = scrollContentGroup.getLayoutBounds().getWidth()
						- scrollPane.getViewportBounds().getWidth();

				if (extraWidth > 0.0f) {

					final double deltaH = deltaX * (scrollPane.getHmax() - scrollPane.getHmin()) / extraWidth;
					final double desiredH = scrollPane.getHvalue() - deltaH;
					final double hValue = Math.max(0, Math.min(scrollPane.getHmax(), desiredH));

					scrollPane.setHvalue(hValue);
				}

				final double deltaY = event.getY() - lastMouseCoordinate.getY();
				final double extraHeight = scrollContentGroup.getLayoutBounds().getHeight()
						- scrollPane.getViewportBounds().getHeight();

				if (extraHeight > 0.0f) {

					final double deltaV = deltaY * (scrollPane.getVmax() - scrollPane.getVmin()) / extraHeight;
					final double desiredV = scrollPane.getVvalue() - deltaV;
					final double vValue = Math.max(0, Math.min(scrollPane.getVmax(), desiredV));

					scrollPane.setVvalue(vValue);
				}

			}
		});

		return scrollPane;
	}

	private Point2D figureScrollOffset(final Node scrollContent, final ScrollPane scroller) {

		final double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
		final double hScrollProportion = (scroller.getHvalue() - scroller.getHmin())
				/ (scroller.getHmax() - scroller.getHmin());
		final double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
		final double extraHeight = scrollContent.getLayoutBounds().getHeight()
				- scroller.getViewportBounds().getHeight();
		final double vScrollProportion = (scroller.getVvalue() - scroller.getVmin())
				/ (scroller.getVmax() - scroller.getVmin());
		final double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);

		return new Point2D(scrollXOffset, scrollYOffset);
	}

	private void repositionScroller(final Node scrollContent, final ScrollPane scroller, final double scaleFactor,
			final Point2D scrollOffset) {

		final double scrollXOffset = scrollOffset.getX();
		final double scrollYOffset = scrollOffset.getY();
		final double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();

		if (extraWidth > 0) {
			final double halfWidth = scroller.getViewportBounds().getWidth() / 2;
			final double newScrollXOffset = (scaleFactor - 1) * halfWidth + scaleFactor * scrollXOffset;
			scroller.setHvalue(
					scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
		} else {
			scroller.setHvalue(scroller.getHmin());
		}

		final double extraHeight = scrollContent.getLayoutBounds().getHeight()
				- scroller.getViewportBounds().getHeight();
		if (extraHeight > 0) {
			final double halfHeight = scroller.getViewportBounds().getHeight() / 2;
			final double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
			scroller.setVvalue(
					scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
		} else {
			scroller.setHvalue(scroller.getHmin());
		}
	}

	private Group createGroup() {

		final Group group = new Group();
		group.getChildren().add(createBox(0, 0));

		return group;
	}

	private SVGPath createCurve() {

		final SVGPath ellipticalArc = new SVGPath();
		ellipticalArc.setContent("M10,150 A15 15 180 0 1 70 140 A15 25 180 0 0 130 130 A15 55 180 0 1 190 120");
		ellipticalArc.setStroke(Color.LIGHTGREEN);
		ellipticalArc.setStrokeWidth(4);
		ellipticalArc.setFill(null);

		return ellipticalArc;
	}

	private SVGPath createStar() {

		final SVGPath star = new SVGPath();
		star.setContent("M100,10 L100,10 40,180 190,60 10,60 160,180 z");
		star.setStrokeLineJoin(StrokeLineJoin.ROUND);
		star.setStroke(Color.BLUE);
		star.setFill(Color.DARKBLUE);
		star.setStrokeWidth(4);

		return star;
	}

	private SVGPath createBox(final double x, final double y) {

		final SVGPath svgPath = new SVGPath();
		svgPath.setContent("M0,0 L100,0 100,100 0,100 0,0 z");
		svgPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
		svgPath.setStroke(Color.BLUE);
		svgPath.setFill(Color.DARKBLUE);
		svgPath.setStrokeWidth(4);

		svgPath.setTranslateX(x);
		svgPath.setTranslateY(y);

		return svgPath;
	}

	private MenuBar createMenuBar(final Stage stage, final Group group) {

		final MenuItem exitMenuItem = new MenuItem("E_xit");
		exitMenuItem.setGraphic(new ImageView(new Image(CLOSE_ICON)));
		exitMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				stage.close();
			}
		});

		final MenuItem zoomResetMenuItem = new MenuItem("Zoom _Reset");
		zoomResetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
		zoomResetMenuItem.setGraphic(new ImageView(new Image(ZOOM_RESET_ICON)));
		zoomResetMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				group.setScaleX(1);
				group.setScaleY(1);
			}
		});

		final MenuItem zoomInMenuItem = new MenuItem("Zoom _In");
		zoomInMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I));
		zoomInMenuItem.setGraphic(new ImageView(new Image(ZOOM_IN_ICON)));
		zoomInMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				group.setScaleX(group.getScaleX() * 1.5);
				group.setScaleY(group.getScaleY() * 1.5);
			}
		});

		final MenuItem zoomOutMenuItem = new MenuItem("Zoom _Out");
		zoomOutMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O));
		zoomOutMenuItem.setGraphic(new ImageView(new Image(ZOOM_OUT_ICON)));
		zoomOutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				group.setScaleX(group.getScaleX() * 1 / 1.5);
				group.setScaleY(group.getScaleY() * 1 / 1.5);
			}
		});

		final Menu fileMenu = new Menu("_File");
		fileMenu.getItems().setAll(exitMenuItem);

		final Menu zoomMenu = new Menu("_Zoom");
		zoomMenu.getItems().setAll(zoomResetMenuItem, zoomInMenuItem, zoomOutMenuItem);

		final MenuBar menuBar = new MenuBar();
		menuBar.getMenus().setAll(fileMenu, zoomMenu);

		return menuBar;
	}

	// icons source from:
	// http://www.iconarchive.com/show/soft-scraps-icons-by-deleket.html
	// icon license: CC Attribution-Noncommercial-No Derivate 3.0 =?
	// http://creativecommons.org/licenses/by-nc-nd/3.0/
	// icon Commercial usage: Allowed (Author Approval required -> Visit artist
	// website for details).

	public static final String APP_ICON = "http://icons.iconarchive.com/icons/deleket/soft-scraps/128/Zoom-icon.png";
	public static final String ZOOM_RESET_ICON = "http://icons.iconarchive.com/icons/deleket/soft-scraps/24/Zoom-icon.png";
	public static final String ZOOM_OUT_ICON = "http://icons.iconarchive.com/icons/deleket/soft-scraps/24/Zoom-Out-icon.png";
	public static final String ZOOM_IN_ICON = "http://icons.iconarchive.com/icons/deleket/soft-scraps/24/Zoom-In-icon.png";
	public static final String CLOSE_ICON = "http://icons.iconarchive.com/icons/deleket/soft-scraps/24/Button-Close-icon.png";
}
