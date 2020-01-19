package de.wfb.dialogs;

import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.model.node.GraphNode;
import de.wfb.model.node.Node;
import de.wfb.model.node.RailNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class RailDetailsPane extends GridPane {

	private static final Logger logger = LogManager.getLogger(RailDetailsPane.class);

	private final Label idLabel = new Label("ID:");

	private Label idValueLabel;

	private final Label feedbackBlockNumberTextfieldLabel = new Label("FeedbackBlock:");

	private TextField feedbackBlockNumberTextfield;

	private Button savebutton;

	@Autowired
	private ModelFacade modelFacade;

	public void setup(final Node node) {

		logger.trace("setup() node = " + node);

		if (node == null) {
			return;
		}

		GridPane.setColumnIndex(idLabel, 1);
		GridPane.setRowIndex(idLabel, 1);

		idValueLabel = new Label();
		final String idValue = retrieveRailNodeLabel(node);
		idValueLabel.setText(idValue);
		GridPane.setColumnIndex(idValueLabel, 2);
		GridPane.setRowIndex(idValueLabel, 1);

		getChildren().addAll(idLabel, idValueLabel);

		GridPane.setColumnIndex(feedbackBlockNumberTextfieldLabel, 1);
		GridPane.setRowIndex(feedbackBlockNumberTextfieldLabel, 2);

		feedbackBlockNumberTextfield = new TextField();
		if (node.getFeedbackBlockNumber() != -1) {
			feedbackBlockNumberTextfield.setText(Integer.toString(node.getFeedbackBlockNumber()));
		}
		GridPane.setColumnIndex(feedbackBlockNumberTextfield, 2);
		GridPane.setRowIndex(feedbackBlockNumberTextfield, 2);

		getChildren().addAll(feedbackBlockNumberTextfieldLabel, feedbackBlockNumberTextfield);

		savebutton = new Button();
		savebutton.setText("Save");
		GridPane.setColumnIndex(savebutton, 1);
		GridPane.setRowIndex(savebutton, 3);
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

	private String retrieveRailNodeLabel(final Node node) {

		final StringBuffer stringBuffer = new StringBuffer();

		final RailNode railNode = (RailNode) node;

		if (CollectionUtils.isNotEmpty(railNode.getManualConnections())) {

			for (final RailNode manuallyConnectedNode : railNode.getManualConnections()) {

				stringBuffer.append("ManuallyConnected to: " + manuallyConnectedNode.getId() + "\n");
			}
		}

		stringBuffer.append("Reserved: ").append(node.isReserved()).append("\n");
		stringBuffer.append("ReservedByLocomotiveId: ").append(node.getReservedLocomotiveId()).append("\n");

		final Optional<DefaultLocomotive> locomotive = modelFacade.getLocomotiveById(node.getReservedLocomotiveId());
		if (locomotive.isPresent()) {
			stringBuffer.append("Locomotive Address: ").append(locomotive.get().getAddress()).append("\n");
		}

		// @formatter:off

		// GraphNode ONE and children
		stringBuffer.append(node.getId()).append(" [").append(node.getGraphNodeOne().getId()).append(" ").append(node.getGraphNodeOne().getColor().name()).append(" -> \n");

		if (CollectionUtils.isNotEmpty(node.getGraphNodeOne().getChildren())) {

			for (final GraphNode graphNode : node.getGraphNodeOne().getChildren()) {

				stringBuffer.append(graphNode.getId()).append(" ").append(graphNode.getColor().name()).append(", ");
			}
		}
		stringBuffer.append("]");

		// GraphNode TWO and children
		stringBuffer.append(" [").append(node.getGraphNodeTwo().getId()).append(" ").append(node.getGraphNodeTwo().getColor().name()).append(" -> \n");

		if (CollectionUtils.isNotEmpty(node.getGraphNodeTwo().getChildren())) {

			for (final GraphNode graphNode : node.getGraphNodeTwo().getChildren()) {

				stringBuffer.append(graphNode.getId()).append(" ").append(graphNode.getColor().name()).append(", ");
			}
		}
		stringBuffer.append("]");

		// @formatter:on

		return stringBuffer.toString();
	}

	public void clear() {

		logger.trace("clear");

		if (idValueLabel != null) {

			getChildren().remove(idValueLabel);
			getChildren().remove(idLabel);
			idValueLabel = null;
		}

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
