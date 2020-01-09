package de.wfb.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.rail.facade.ProtocolFacade;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class ThrottlePane extends GridPane {

	private static final double THROTTLE_MIN_VALUE = 0.0d;

	private static final double THROTTLE_MAX_VALUE = 127.0d;

	private static final Logger logger = LogManager.getLogger(ThrottlePane.class);

	private TextField textfield;

	private Button changeDirectionButton;

	private Slider slider;

	private double throttleValue = 0.0d;

	private boolean dirForward = true;

	@Autowired
	private ProtocolFacade protocolFacade;

	public void setup() {

		logger.info("setup()");

		textfield = new TextField();
		textfield.setText("74");
		GridPane.setColumnIndex(textfield, 1);
		GridPane.setRowIndex(textfield, 1);
		getChildren().add(textfield);

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
			public void handle(final MouseEvent e) {

				final double newValue = slider.getValue();

				processEventThrottle(newValue);
			}
		});
		getChildren().add(slider);

		changeDirectionButton = new Button();
		changeDirectionButton.setText("<->");
		GridPane.setColumnIndex(changeDirectionButton, 1);
		GridPane.setRowIndex(changeDirectionButton, 3);
		changeDirectionButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				logger.info("ChangeDirectionButton");
				processChangeDirectionEvent();

			}
		});
		getChildren().add(changeDirectionButton);
	}

	protected void processChangeDirectionEvent() {

		// stop the train
		final double newThrottleValue = 0;
		processEventThrottle(newThrottleValue);

		// change the direction
		dirForward = !dirForward;
	}

	private void processEventThrottle(final double newThrottleValue) {

		if (throttleValue == newThrottleValue) {
			return;
		}

		throttleValue = Math.round(newThrottleValue);

		slider.setValue(throttleValue);

		logger.trace("newValue = " + throttleValue);

		final String textFieldContent = textfield.getText();

		final short locomotiveAddress = getLocomotiveAddress(textFieldContent);

		if (locomotiveAddress >= 0) {
			protocolFacade.throttleLocomotive(locomotiveAddress, throttleValue, dirForward);
		}
	}

	private short getLocomotiveAddress(final String locomotiveAddressAsString) {

		if (StringUtils.isBlank(locomotiveAddressAsString)) {
			return -1;
		}

		if (!NumberUtils.isCreatable(locomotiveAddressAsString)) {
			return -1;
		}

		final Integer addressAsInteger = NumberUtils.createInteger(locomotiveAddressAsString);

		return addressAsInteger.shortValue();
	}

	public void clear() {

		logger.trace("clear");

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
