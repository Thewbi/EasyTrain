package de.wfb.dialogs;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.node.Node;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class RailDetailsPane extends GridPane {

	private static final Logger logger = LogManager.getLogger(RailDetailsPane.class);

	private TextField feedbackBlockNumberTextfield;

	private final Label feedbackBlockNumberTextfieldLabel = new Label("FeedbackBlock:");

	private Button savebutton;

	public void setup(final Node node) {

		logger.trace("setup() node = " + node);

		if (node == null) {
			return;
		}

		GridPane.setColumnIndex(feedbackBlockNumberTextfieldLabel, 1);
		GridPane.setRowIndex(feedbackBlockNumberTextfieldLabel, 1);

		feedbackBlockNumberTextfield = new TextField();
		if (node.getFeedbackBlockNumber() != -1) {
			feedbackBlockNumberTextfield.setText(Integer.toString(node.getFeedbackBlockNumber()));
		}
		GridPane.setColumnIndex(feedbackBlockNumberTextfield, 2);
		GridPane.setRowIndex(feedbackBlockNumberTextfield, 1);

		getChildren().addAll(feedbackBlockNumberTextfieldLabel, feedbackBlockNumberTextfield);

		savebutton = new Button();
		savebutton.setText("Save");
		GridPane.setColumnIndex(savebutton, 1);
		GridPane.setRowIndex(savebutton, 2);
		savebutton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				final String feedbackBlockNumberTextfieldContent = feedbackBlockNumberTextfield.getText();
				final int feedbackBlockNumber = NumberUtils.toInt(feedbackBlockNumberTextfieldContent,
						Integer.MIN_VALUE);
				if (feedbackBlockNumber != Integer.MIN_VALUE) {

					logger.info("Saving feedbackBlockNumber " + feedbackBlockNumber);
					node.setFeedbackBlockNumber(feedbackBlockNumber);
				}
			}
		});
		getChildren().add(savebutton);

	}

	public void clear() {

		logger.trace("clear");

		if (feedbackBlockNumberTextfield != null) {

			getChildren().remove(feedbackBlockNumberTextfieldLabel);
			getChildren().remove(feedbackBlockNumberTextfield);
			feedbackBlockNumberTextfield = null;
		}

		if (savebutton != null) {

			getChildren().remove(savebutton);
			savebutton = null;
		}
	}

}
