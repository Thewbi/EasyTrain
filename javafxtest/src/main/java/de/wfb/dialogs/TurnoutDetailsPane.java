package de.wfb.dialogs;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Node;
import de.wfb.model.node.TurnoutNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class TurnoutDetailsPane extends GridPane {

	private static final Logger logger = LogManager.getLogger(TurnoutDetailsPane.class);

	private TextField textfield;

	private Button savebutton;

	public void setup(final Node node) {

		logger.info("setup() node = " + node);

		if (node == null) {
			return;
		}

		if (node instanceof TurnoutNode) {

			final TurnoutNode turnoutNode = (TurnoutNode) node;

			textfield = new TextField();
			textfield.setText(Integer.toString(turnoutNode.getProtocolTurnoutId()));
			GridPane.setColumnIndex(textfield, 1);
			GridPane.setRowIndex(textfield, 1);

			getChildren().add(textfield);

			savebutton = new Button();
			savebutton.setText("Save");
			GridPane.setColumnIndex(savebutton, 1);
			GridPane.setRowIndex(savebutton, 2);
			savebutton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(final ActionEvent e) {

					final String textFieldContent = textfield.getText();
					final int intValue = NumberUtils.toInt(textFieldContent, Integer.MIN_VALUE);
					if (intValue != Integer.MIN_VALUE) {

						logger.info("Saving protocolturnoutid " + intValue);
						turnoutNode.setProtocolTurnoutId(intValue);
					}
				}
			});
			getChildren().add(savebutton);
		}
	}

	public void clear() {

		logger.info("clear");

		getChildren().remove(textfield);
		getChildren().remove(savebutton);
	}

}
