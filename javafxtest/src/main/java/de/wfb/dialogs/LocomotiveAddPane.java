package de.wfb.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class LocomotiveAddPane extends VBox {

	private static final Logger logger = LogManager.getLogger(LocomotiveAddPane.class);

	private final TextField nameTextField = new TextField();

	private final TextField addressTextField = new TextField();

	private final Button addButton = new Button("Add");

	@Autowired
	private ModelFacade modelFacade;

	public LocomotiveAddPane() {

		nameTextField.setPromptText("Name");
		GridPane.setColumnIndex(nameTextField, 1);
		GridPane.setRowIndex(nameTextField, 1);

		addressTextField.setPromptText("Address");
		GridPane.setColumnIndex(addressTextField, 2);
		GridPane.setRowIndex(addressTextField, 1);

		addButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent e) {

				final String name = nameTextField.getText();
				final String addressAsString = addressTextField.getText();

				if (StringUtils.isBlank(name) || StringUtils.isBlank(addressAsString)
						|| !NumberUtils.isCreatable(addressTextField.getText())) {

					final String msg = "Invalid Input data! Cannot create Locomotive! Need name and numeric address";

					logger.error(msg);

					final Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Adding Locomotive Failed!");
					alert.setHeaderText("Adding Locomotive Failed!");
					alert.setContentText(msg);
					alert.showAndWait();

					return;
				}

				final short address = NumberUtils.createInteger(addressAsString).shortValue();

				// add the locomotive to the model
				final int locomotiveId = modelFacade.retrieveNextLocomotiveId();
				final DefaultLocomotive defaultLocomotive = new DefaultLocomotive(locomotiveId, nameTextField.getText(),
						address, null);
				modelFacade.addLocomotive(defaultLocomotive);

				// write the model to disk
				modelFacade.storeLocomotiveModel(modelFacade.getCurrentLocomotivesModel());

				// reset
				nameTextField.clear();
				addressTextField.clear();
			}
		});
		GridPane.setColumnIndex(addButton, 3);
		GridPane.setRowIndex(addButton, 1);

		final GridPane gridPane = new GridPane();
		gridPane.getChildren().addAll(nameTextField, addressTextField, addButton);

		setSpacing(5);
		setPadding(new Insets(0, 10, 10, 10));
		getChildren().add(gridPane);
	}

	public void setup() {

	}

	public void clear() {

	}

}
