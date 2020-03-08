package de.wfb.dialogs;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Node;
import de.wfb.rail.ui.ShapeType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class SignalDetailsPane extends GridPane {

	private static final Logger logger = LogManager.getLogger(TurnoutDetailsPane.class);

	private final Label addressTextfieldLabel = new Label("Address:");

	private TextField addressTextfield;

	private Button savebutton;

	public void setup(final Node node) {

		logger.trace("setup() node = " + node);

		if (node == null) {
			return;
		}

		if (ShapeType.isSignal(node.getShapeType())) {

			// protocol address
			GridPane.setColumnIndex(addressTextfieldLabel, 1);
			GridPane.setRowIndex(addressTextfieldLabel, 1);

			addressTextfield = new TextField();
			if (node.getProtocolTurnoutId() != null) {
				addressTextfield.setText(Integer.toString(node.getProtocolTurnoutId()));
			}
			GridPane.setColumnIndex(addressTextfield, 2);
			GridPane.setRowIndex(addressTextfield, 1);

			getChildren().addAll(addressTextfieldLabel, addressTextfield);

			// save button
			savebutton = new Button();
			savebutton.setText("Save");
			GridPane.setColumnIndex(savebutton, 1);
			GridPane.setRowIndex(savebutton, 3);
			savebutton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent e) {

					// save protocol id to node
					final String textFieldContent = addressTextfield.getText();
					final int intValue = NumberUtils.toInt(textFieldContent, Integer.MIN_VALUE);
					if (intValue != Integer.MIN_VALUE) {

						logger.info("Saving protocolturnoutid " + intValue);
						node.setProtocolTurnoutId(intValue);
					}
				}
			});
			getChildren().add(savebutton);
		}
	}

	public void clear() {

		logger.trace("clear");

		if (addressTextfield != null) {

			getChildren().remove(addressTextfieldLabel);
			getChildren().remove(addressTextfield);
			addressTextfield = null;
		}
		if (savebutton != null) {

			getChildren().remove(savebutton);
			savebutton = null;
		}
	}

}
