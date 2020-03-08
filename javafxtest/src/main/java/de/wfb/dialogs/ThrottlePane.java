package de.wfb.dialogs;

import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.locomotive.Locomotive;
import de.wfb.rail.events.LocomotiveModelChangedEvent;
import de.wfb.rail.factory.Factory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class ThrottlePane extends GridPane implements ApplicationListener<ApplicationEvent> {

	private static final double THROTTLE_MIN_VALUE = 0.0d;

	private static final double THROTTLE_MAX_VALUE = 127.0d;

	private static final Logger logger = LogManager.getLogger(ThrottlePane.class);

	private TextField textfield;

	@SuppressWarnings("rawtypes")
	private ComboBox locomotiveComboBox;

	private Button changeDirectionButton;

	private Slider slider;

	private double throttleValue = 0.0d;

	private boolean dirForward = true;

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private Factory<Locomotive> locomotiveFactory;

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof LocomotiveModelChangedEvent) {

			final LocomotiveModelChangedEvent locomotiveModelChangedEvent = (LocomotiveModelChangedEvent) event;

//			final Locomotive locomotive = locomotiveModelChangedEvent.getLocomotive();

			switch (locomotiveModelChangedEvent.getOperationType()) {

			case ADDED:
//				data.add(locomotive);
				reloadLocomotiveSection();
				break;

			case REMOVED:
//				data.remove(locomotive);
				reloadLocomotiveSection();
				break;

			default:
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void reloadLocomotiveSection() {

		if (locomotiveComboBox != null) {

			if (CollectionUtils.isNotEmpty(locomotiveComboBox.getItems())) {
				locomotiveComboBox.getItems().clear();
			}
			getChildren().remove(locomotiveComboBox);
			locomotiveComboBox = null;
		}

		if (textfield != null) {
			getChildren().remove(textfield);
			textfield = null;
		}

		locomotiveComboBox = new ComboBox<Locomotive>();
		locomotiveComboBox.getItems().addAll(modelFacade.getLocomotives());
		locomotiveComboBox.getSelectionModel().selectFirst();
		GridPane.setColumnIndex(locomotiveComboBox, 1);
		GridPane.setRowIndex(locomotiveComboBox, 1);
		getChildren().add(locomotiveComboBox);

		locomotiveComboBox.setCellFactory(c -> new LocomotiveListCell());

		locomotiveComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

			final Locomotive locomotive = (Locomotive) newValue;
			textfield.setText(Short.toString(locomotive.getAddress()));
		});

		final DefaultLocomotive locomotive = (DefaultLocomotive) locomotiveComboBox.getValue();

		textfield = new TextField();
		textfield.setDisable(true);
		if (locomotive != null) {
			textfield.setText(Short.toString(locomotive.getAddress()));
		}
		GridPane.setColumnIndex(textfield, 2);
		GridPane.setRowIndex(textfield, 1);
		getChildren().add(textfield);
	}

	public void setup() {

		logger.trace("setup()");

		reloadLocomotiveSection();

		slider = new Slider();
		GridPane.setColumnIndex(slider, 1);
		GridPane.setRowIndex(slider, 2);
		slider.setMin(THROTTLE_MIN_VALUE);
		slider.setMax(THROTTLE_MAX_VALUE);
		slider.setMajorTickUnit(25);
		slider.setSnapToTicks(false);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		slider.setValue(throttleValue);
		slider.setPadding(new Insets(10.0d, 10.0d, 10.0d, 10.0d));
		slider.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent mouseEvent) {

				final double newValue = slider.getValue();

				try {
					processEventThrottle(newValue);
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
		getChildren().add(slider);

		changeDirectionButton = new Button();
		changeDirectionButton.setText("->");
		GridPane.setColumnIndex(changeDirectionButton, 1);
		GridPane.setRowIndex(changeDirectionButton, 3);
		changeDirectionButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent actionEvent) {

				logger.trace("ChangeDirectionButton");
				try {
					processChangeDirectionEvent();
				} catch (final Exception e) {
					logger.error(e.getMessage(), e);
				}

			}
		});
		getChildren().add(changeDirectionButton);
	}

	protected void processChangeDirectionEvent() throws Exception {

		// change the direction
		dirForward = !dirForward;

		changeDirectionButton.setText(dirForward ? "->" : "<-");

		final Locomotive locomotive = retrieveLocomotive();
		if (locomotive != null) {
			locomotive.setDirection(dirForward);
		}

		// stop the train
		final double newThrottleValue = 0;
		processEventThrottle(newThrottleValue);
	}

	private void processEventThrottle(final double newThrottleValue) throws Exception {

		if (throttleValue == newThrottleValue) {
			return;
		}

		throttleValue = Math.round(newThrottleValue);

		slider.setValue(throttleValue);

		logger.trace("newValue = " + throttleValue);

		final Locomotive locomotive = retrieveLocomotive();
		if (locomotive != null) {
			locomotive.start(throttleValue);
		}
	}

	private Locomotive retrieveLocomotive() throws Exception {

		final short locomotiveAddress = retrieveLocomotiveAddress();
		if (locomotiveAddress >= 0) {

			final Optional<Locomotive> locomotiveOptional = modelFacade.getLocomotiveByAddress(locomotiveAddress);

			if (locomotiveOptional.isPresent()) {

				return locomotiveOptional.get();

			} else {

				final Locomotive locomotive = locomotiveFactory.create(locomotiveAddress, true,
						"Loc " + locomotiveAddress, 0.0);

				modelFacade.addLocomotive(locomotive);

				return locomotive;
			}
		}

		return null;
	}

	private short retrieveLocomotiveAddress() {

		final DefaultLocomotive locomotive = (DefaultLocomotive) locomotiveComboBox.getValue();

		return locomotive.getAddress();

//		final String locomotiveAddressTextFieldContent = textfield.getText();
//		final short locomotiveAddress = convertStringToShort(StringUtils.trim(locomotiveAddressTextFieldContent));
//
//		return locomotiveAddress;
	}

	@SuppressWarnings("unused")
	private short convertStringToShort(final String dataAsString) {

		if (StringUtils.isBlank(dataAsString)) {
			return -1;
		}

		if (!NumberUtils.isCreatable(dataAsString)) {
			return -1;
		}

		final Integer addressAsInteger = NumberUtils.createInteger(dataAsString);

		return addressAsInteger.shortValue();
	}

	public void clear() {

		logger.trace("clear");

		if (locomotiveComboBox != null) {
			locomotiveComboBox.getItems().clear();
			getChildren().remove(locomotiveComboBox);
			locomotiveComboBox = null;
		}

		if (textfield != null) {
			getChildren().remove(textfield);
			textfield = null;
		}

		if (slider != null) {
			getChildren().remove(slider);
			slider = null;
		}

		if (changeDirectionButton != null) {
			getChildren().remove(changeDirectionButton);
			changeDirectionButton = null;
		}
	}

}
