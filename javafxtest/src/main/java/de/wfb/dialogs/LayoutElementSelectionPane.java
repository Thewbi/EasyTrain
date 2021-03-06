package de.wfb.dialogs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import de.wfb.rail.events.ShapeTypeChangedEvent;
import de.wfb.rail.ui.ShapeType;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class LayoutElementSelectionPane extends GridPane {

	private static final Logger logger = LogManager.getLogger(LayoutElementSelectionPane.class);

	/** https://www.baeldung.com/spring-events */
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void setup() throws FileNotFoundException {

		logger.trace("setupButtons()");

		final ToggleGroup toggleGroup = new ToggleGroup();

		final ToggleButton turnTopRightButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/TURN_TOP_RIGHT_SELECTED.png", "src/main/resources/TURN_TOP_RIGHT.png",
				ShapeType.TURN_TOP_RIGHT);
		GridPane.setColumnIndex(turnTopRightButton, 1);
		GridPane.setRowIndex(turnTopRightButton, 1);
		getChildren().add(turnTopRightButton);

		final ToggleButton turnRightBottomButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/TURN_RIGHT_BOTTOM_SELECTED.png", "src/main/resources/TURN_RIGHT_BOTTOM.png",
				ShapeType.TURN_RIGHT_BOTTOM);
		GridPane.setColumnIndex(turnRightBottomButton, 2);
		GridPane.setRowIndex(turnRightBottomButton, 1);
		getChildren().add(turnRightBottomButton);

		final ToggleButton turnBottomLeftButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/TURN_BOTTOM_LEFT_SELECTED.png", "src/main/resources/TURN_BOTTOM_LEFT.png",
				ShapeType.TURN_BOTTOM_LEFT);
		GridPane.setColumnIndex(turnBottomLeftButton, 3);
		GridPane.setRowIndex(turnBottomLeftButton, 1);
		getChildren().add(turnBottomLeftButton);

		final ToggleButton turnLeftTopButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/TURN_LEFT_TOP_SELECTED.png", "src/main/resources/TURN_LEFT_TOP.png",
				ShapeType.TURN_LEFT_TOP);
		GridPane.setColumnIndex(turnLeftTopButton, 4);
		GridPane.setRowIndex(turnLeftTopButton, 1);
		getChildren().add(turnLeftTopButton);

		final ToggleButton straightVerticalButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/STRAIGHT_VERTICAL_SELECTED.png", "src/main/resources/STRAIGHT_VERTICAL.png",
				ShapeType.STRAIGHT_VERTICAL);
		GridPane.setColumnIndex(straightVerticalButton, 5);
		GridPane.setRowIndex(straightVerticalButton, 1);
		getChildren().add(straightVerticalButton);

		final ToggleButton straightHorizontalButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/STRAIGHT_HORIZONTAL_SELECTED.png", "src/main/resources/STRAIGHT_HORIZONTAL.png",
				ShapeType.STRAIGHT_HORIZONTAL);
		GridPane.setColumnIndex(straightHorizontalButton, 6);
		GridPane.setRowIndex(straightHorizontalButton, 1);
		getChildren().add(straightHorizontalButton);

		final ToggleButton removeButton = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/REMOVE_SELECTED.png", "src/main/resources/REMOVE.png", ShapeType.REMOVE);
		GridPane.setColumnIndex(removeButton, 7);
		GridPane.setRowIndex(removeButton, 1);
		getChildren().add(removeButton);

		// straightHorizontalButton.setSelected(true);

		final ToggleButton switchLeft0 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_LEFT_0_SELECTED.png", "src/main/resources/SWITCH_LEFT_0.png",
				ShapeType.SWITCH_LEFT_0);
		GridPane.setColumnIndex(switchLeft0, 1);
		GridPane.setRowIndex(switchLeft0, 2);
		getChildren().add(switchLeft0);

		final ToggleButton switchLeft90 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_LEFT_90_SELECTED.png", "src/main/resources/SWITCH_LEFT_90.png",
				ShapeType.SWITCH_LEFT_90);
		GridPane.setColumnIndex(switchLeft90, 2);
		GridPane.setRowIndex(switchLeft90, 2);
		getChildren().add(switchLeft90);

		final ToggleButton switchLeft180 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_LEFT_180_SELECTED.png", "src/main/resources/SWITCH_LEFT_180.png",
				ShapeType.SWITCH_LEFT_180);
		GridPane.setColumnIndex(switchLeft180, 3);
		GridPane.setRowIndex(switchLeft180, 2);
		getChildren().add(switchLeft180);

		final ToggleButton switchLeft270 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_LEFT_270_SELECTED.png", "src/main/resources/SWITCH_LEFT_270.png",
				ShapeType.SWITCH_LEFT_270);
		GridPane.setColumnIndex(switchLeft270, 4);
		GridPane.setRowIndex(switchLeft270, 2);
		getChildren().add(switchLeft270);

		final ToggleButton switchRight0 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_RIGHT_0_SELECTED.png", "src/main/resources/SWITCH_RIGHT_0.png",
				ShapeType.SWITCH_RIGHT_0);
		GridPane.setColumnIndex(switchRight0, 5);
		GridPane.setRowIndex(switchRight0, 2);
		getChildren().add(switchRight0);

		final ToggleButton switchRight90 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_RIGHT_90_SELECTED.png", "src/main/resources/SWITCH_RIGHT_90.png",
				ShapeType.SWITCH_RIGHT_90);
		GridPane.setColumnIndex(switchRight90, 6);
		GridPane.setRowIndex(switchRight90, 2);
		getChildren().add(switchRight90);

		final ToggleButton switchRight180 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_RIGHT_180_SELECTED.png", "src/main/resources/SWITCH_RIGHT_180.png",
				ShapeType.SWITCH_RIGHT_180);
		GridPane.setColumnIndex(switchRight180, 7);
		GridPane.setRowIndex(switchRight180, 2);
		getChildren().add(switchRight180);

		final ToggleButton switchRight270 = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_RIGHT_270_SELECTED.png", "src/main/resources/SWITCH_RIGHT_270.png",
				ShapeType.SWITCH_RIGHT_270);
		GridPane.setColumnIndex(switchRight270, 8);
		GridPane.setRowIndex(switchRight270, 2);
		getChildren().add(switchRight270);

		final ToggleButton switchDoubleCrossLeftTop = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_DOUBLECROSS_LEFT_TOP_SELECTED.png",
				"src/main/resources/SWITCH_DOUBLECROSS_LEFT_TOP.png", ShapeType.SWITCH_DOUBLECROSS_LEFT_TOP);
		GridPane.setColumnIndex(switchDoubleCrossLeftTop, 1);
		GridPane.setRowIndex(switchDoubleCrossLeftTop, 3);
		getChildren().add(switchDoubleCrossLeftTop);

		final ToggleButton switchDoubleCrossTopRight = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SWITCH_DOUBLECROSS_TOP_RIGHT_SELECTED.png",
				"src/main/resources/SWITCH_DOUBLECROSS_TOP_RIGHT.png", ShapeType.SWITCH_DOUBLECROSS_TOP_RIGHT);
		GridPane.setColumnIndex(switchDoubleCrossTopRight, 2);
		GridPane.setRowIndex(switchDoubleCrossTopRight, 3);
		getChildren().add(switchDoubleCrossTopRight);

		final ToggleButton signalHorizontal = createToggleButton(applicationEventPublisher, toggleGroup,
				"src/main/resources/SIGNAL_OPEN_SELECTED.png", "src/main/resources/SIGNAL_OPEN.png",
				ShapeType.SIGNAL_HORIZONTAL);
		GridPane.setColumnIndex(signalHorizontal, 3);
		GridPane.setRowIndex(signalHorizontal, 3);
		getChildren().add(signalHorizontal);
	}

	private ToggleButton createToggleButton(final ApplicationEventPublisher applicationEventPublisher,
			final ToggleGroup toggleGroup, final String selectedPath, final String notSelectedPath,
			final ShapeType shapeType) throws FileNotFoundException {

		final ToggleButton toggleButton = new ToggleButton();
		toggleButton.setToggleGroup(toggleGroup);
		toggleButton.setMinSize(32, 32);
		toggleButton.setMaxSize(32, 32);

		setupImages(selectedPath, notSelectedPath, toggleButton);

		toggleButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				final ToggleButton toggleButton = (ToggleButton) e.getSource();

				final ShapeType currentShapeType = toggleButton.isSelected() ? shapeType : ShapeType.NONE;

				final ShapeTypeChangedEvent event = new ShapeTypeChangedEvent(this, currentShapeType);

				applicationEventPublisher.publishEvent(event);

			}
		});

		return toggleButton;
	}

	private void setupImages(final String selectedPath, final String notSelectedPath, final ToggleButton toggleButton)
			throws FileNotFoundException {

		final InputStream selectedInputStream = new FileInputStream(selectedPath);
		final Image selectedImage = new Image(selectedInputStream);

		final InputStream notSelectedInputStream = new FileInputStream(notSelectedPath);
		final Image notSelectedImage = new Image(notSelectedInputStream);

		final ImageView selectedImageView = new ImageView();
		selectedImageView.imageProperty()
				.bind(Bindings.when(toggleButton.selectedProperty()).then(selectedImage).otherwise(notSelectedImage));
		toggleButton.setGraphic(selectedImageView);
	}
}
