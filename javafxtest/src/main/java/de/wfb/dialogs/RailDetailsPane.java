package de.wfb.dialogs;

import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.Locomotive;
import de.wfb.model.node.Direction;
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

	private Label traverseDirectionLabel;

	private Label currentValueLabel;

	private Label currentValue;

	private Button northButton;

	private Button eastButton;

	private Button southButton;

	private Button westButton;

	private Button allButton;

	private Button saveButton;

	@Autowired
	private ModelFacade modelFacade;

	public void setup(final Node node) {

		logger.trace("setup() node = " + node);

		if (node == null) {
			return;
		}

		idData(node);
		feedbackBlockData(node);
		traverseDirectionData(node);
		saveButton(node);
	}

	private void saveButton(final Node node) {

		saveButton = new Button();
		saveButton.setText("Save");
		GridPane.setColumnIndex(saveButton, 1);
		GridPane.setRowIndex(saveButton, 10);
		saveButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				// feedback block number
				final String feedbackBlockNumberTextfieldContent = feedbackBlockNumberTextfield.getText();
				final int feedbackBlockNumber = NumberUtils.toInt(feedbackBlockNumberTextfieldContent,
						Integer.MIN_VALUE);
				if (feedbackBlockNumber != Integer.MIN_VALUE) {

					logger.info("Saving feedbackBlockNumber " + feedbackBlockNumber);
					node.setFeedbackBlockNumber(feedbackBlockNumber);
				}

				// direction
				final RailNode railNode = (RailNode) node;
				railNode.updateBlockedGraphNode();

			}
		});
		getChildren().add(saveButton);
	}

	private void traverseDirectionData(final Node node) {

		// header label
		traverseDirectionLabel = new Label("Traversable Direction:");
		GridPane.setColumnIndex(traverseDirectionLabel, 1);
		GridPane.setRowIndex(traverseDirectionLabel, 3);
		getChildren().addAll(traverseDirectionLabel);

		// current value label
		currentValueLabel = new Label("Current Value: ");
		GridPane.setColumnIndex(currentValueLabel, 1);
		GridPane.setRowIndex(currentValueLabel, 4);
		getChildren().addAll(currentValueLabel);

		// current value
		currentValue = new Label();
		GridPane.setColumnIndex(currentValue, 2);
		GridPane.setRowIndex(currentValue, 4);
		getChildren().addAll(currentValue);

		if (node.getTraverse() == null) {
			currentValue.setText("");
		} else {
			currentValue.setText(node.getTraverse().name());
		}

		// NORTH
		northButton = new Button("North");
		GridPane.setColumnIndex(northButton, 1);
		GridPane.setRowIndex(northButton, 5);
		northButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				logger.trace("NORTH");
				node.setTraverse(Direction.NORTH);
				currentValue.setText("NORTH");
			}
		});
		getChildren().add(northButton);

		// EAST
		eastButton = new Button("East");
		GridPane.setColumnIndex(eastButton, 2);
		GridPane.setRowIndex(eastButton, 5);
		eastButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				logger.trace("EAST");
				node.setTraverse(Direction.EAST);
				currentValue.setText("EAST");
			}
		});
		getChildren().add(eastButton);

		// SOUTH
		southButton = new Button("South");
		GridPane.setColumnIndex(southButton, 3);
		GridPane.setRowIndex(southButton, 5);
		southButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				logger.trace("SOUTH");
				node.setTraverse(Direction.SOUTH);
				currentValue.setText("SOUTH");
			}
		});
		getChildren().add(southButton);

		// WEST
		westButton = new Button("West");
		GridPane.setColumnIndex(westButton, 4);
		GridPane.setRowIndex(westButton, 5);
		westButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				logger.trace("WEST");
				node.setTraverse(Direction.WEST);
				currentValue.setText("WEST");
			}
		});
		getChildren().add(westButton);

		// ALL
		allButton = new Button("All");
		GridPane.setColumnIndex(allButton, 1);
		GridPane.setRowIndex(allButton, 8);
		allButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				logger.trace("ALL");
				node.setTraverse(null);
				currentValue.setText("");
			}
		});
		getChildren().add(allButton);
	}

	private void feedbackBlockData(final Node node) {

		GridPane.setColumnIndex(feedbackBlockNumberTextfieldLabel, 1);
		GridPane.setRowIndex(feedbackBlockNumberTextfieldLabel, 2);

		feedbackBlockNumberTextfield = new TextField();
		if (node.getFeedbackBlockNumber() != null && node.getFeedbackBlockNumber() > -1) {
			feedbackBlockNumberTextfield.setText(Integer.toString(node.getFeedbackBlockNumber()));
		}
		GridPane.setColumnIndex(feedbackBlockNumberTextfield, 2);
		GridPane.setRowIndex(feedbackBlockNumberTextfield, 2);

		getChildren().addAll(feedbackBlockNumberTextfieldLabel, feedbackBlockNumberTextfield);
	}

	private void idData(final Node node) {

		// ID label
		GridPane.setColumnIndex(idLabel, 1);
		GridPane.setRowIndex(idLabel, 1);

		// ID data
		idValueLabel = new Label();
		idValueLabel.setMinHeight(200);
		final String idValue = retrieveRailNodeLabel(node);

		logger.info(idValue);

		idValueLabel.setText(idValue);
		GridPane.setColumnIndex(idValueLabel, 2);
		GridPane.setRowIndex(idValueLabel, 1);

		getChildren().addAll(idLabel, idValueLabel);
	}

	private String retrieveRailNodeLabel(final Node node) {

		final boolean showConnectionData = true;

		final StringBuffer stringBuffer = new StringBuffer();

		final RailNode railNode = (RailNode) node;

		// manual connections
		if (CollectionUtils.isNotEmpty(railNode.getManualConnections())) {

			for (final RailNode manuallyConnectedNode : railNode.getManualConnections()) {

				stringBuffer.append("ManuallyConnected to: " + manuallyConnectedNode.getId() + "\n");
			}
		}

		// @formatter:off

		// GraphNode ONE and children
		stringBuffer.append(node.getId()).append("\n");

		logger.info("GraphNodeOne: " + node.getGraphNodeOne());
		outputGraphNode(node.getGraphNodeOne(), showConnectionData, stringBuffer);

		logger.info("GraphNodeTwo: " + node.getGraphNodeTwo());
		outputGraphNode(node.getGraphNodeTwo(), showConnectionData, stringBuffer);

		logger.info("GraphNodeThree: " + node.getGraphNodeThree());
		outputGraphNode(node.getGraphNodeThree(), showConnectionData, stringBuffer);

		logger.info("GraphNodeFour: " + node.getGraphNodeFour());
		outputGraphNode(node.getGraphNodeFour(), showConnectionData, stringBuffer);

//		final GraphNode graphNodeTwo = node.getGraphNodeTwo();
//
//		// GraphNode TWO and children
//		stringBuffer
//			.append(" [")
//			.append(graphNodeTwo.getId())
//			.append(" x: ").append(graphNodeTwo.getX()).append(" y: ").append(graphNodeTwo.getY());
////			.append(" ")
////			.append(node.getGraphNodeTwo().getColor().name())
//
//		if (showConnectionData) {
//
//			stringBuffer.append(" -> \n");
//			if (CollectionUtils.isNotEmpty(graphNodeTwo.getChildren())) {
//
//				for (final GraphNode graphNode : graphNodeTwo.getChildren()) {
//
//					stringBuffer
//					.append(graphNode.getId())
//	//				.append(" ")
//	//				.append(graphNode.getColor().name())
//					.append(", ");
//				}
//			}
//		}
//
//		stringBuffer.append("]\n");

		// @formatter:on

		stringBuffer.append("Highlighted: ").append(node.isHighlighted()).append("\n");

		// graphnode blocked
		stringBuffer.append("GN 1 Blocked: ").append(node.getGraphNodeOne().isBlocked()).append("\n");
		stringBuffer.append("GN 2 Blocked: ").append(node.getGraphNodeTwo().isBlocked()).append("\n");

		// railnode feedbackblock used
		stringBuffer.append("RN FeedbackBlockUsed: ").append(node.isFeedbackBlockUsed()).append("\n");

		// locomotive data
		stringBuffer.append("Reserved: ").append(node.isReserved()).append("\n");
		stringBuffer.append("ReservedByLocomotiveId: ").append(node.getReservedLocomotiveId()).append("\n");

		final Optional<Locomotive> locomotive = modelFacade.getLocomotiveById(node.getReservedLocomotiveId());
		if (locomotive.isPresent()) {
			stringBuffer.append("Locomotive Address: ").append(locomotive.get().getAddress()).append("\n");
		}

		return stringBuffer.toString();
	}

	private void outputGraphNode(final GraphNode graphNode, final boolean showConnectionData,
			final StringBuffer stringBuffer) {

		if (graphNode == null) {

			return;
		}

		// graphnode one
		stringBuffer.append("[").append(graphNode.getId()).append(" x: ").append(graphNode.getX()).append(" y: ")
				.append(graphNode.getY());
//			.append(" ")
//			.append(node.getGraphNodeOne().getColor().name())

		if (showConnectionData) {

			stringBuffer.append(" -> ");
			if (CollectionUtils.isNotEmpty(graphNode.getChildren())) {

				for (final GraphNode tempGraphNode : graphNode.getChildren()) {

					stringBuffer.append(tempGraphNode.getId()).append(" ");
					// .append(graphNode.getColor().name()).append(", ");
				}
			}
		}

		stringBuffer.append("]\n");
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

		if (saveButton != null) {

			getChildren().remove(saveButton);
			saveButton = null;
		}

		if (northButton != null) {

			getChildren().remove(northButton);
			northButton = null;
		}
		if (eastButton != null) {

			getChildren().remove(eastButton);
			eastButton = null;
		}
		if (southButton != null) {

			getChildren().remove(southButton);
			southButton = null;
		}
		if (westButton != null) {

			getChildren().remove(westButton);
			westButton = null;
		}
		if (allButton != null) {

			getChildren().remove(allButton);
			allButton = null;
		}

		if (traverseDirectionLabel != null) {

			getChildren().remove(traverseDirectionLabel);
			traverseDirectionLabel = null;
		}

		if (currentValueLabel != null) {

			getChildren().remove(currentValueLabel);
			currentValueLabel = null;
		}
		if (currentValue != null) {

			getChildren().remove(currentValue);
			currentValue = null;
		}

	}

}
