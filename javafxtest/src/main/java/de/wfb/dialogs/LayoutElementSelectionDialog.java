package de.wfb.dialogs;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

public class LayoutElementSelectionDialog extends Dialog<String> {

	public LayoutElementSelectionDialog() {

		setTitle("Login dialog (programmatic)");

		getDialogPane().setContent(createContentGrid());

		// without this, the close button does not close the dialog
		// https://stackoverflow.com/questions/32048348/javafx-scene-control-dialogr-wont-close-on-pressing-x
		final Window window = getDialogPane().getScene().getWindow();
		window.setOnCloseRequest(event -> window.hide());
	}

	private Node createContentGrid() {

		final TextField usernameTextField = new TextField();
		usernameTextField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(usernameTextField, Priority.ALWAYS);
		GridPane.setFillWidth(usernameTextField, true);
		Platform.runLater(() -> usernameTextField.requestFocus());

		final GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setPrefWidth(300.0);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setAlignment(Pos.CENTER_LEFT);
		gridPane.getChildren().clear();
		gridPane.add(usernameTextField, 0, 0);

		return gridPane;
	}

}
