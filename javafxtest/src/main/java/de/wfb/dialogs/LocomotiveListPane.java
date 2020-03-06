package de.wfb.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import de.wfb.model.facade.ModelFacade;
import de.wfb.model.locomotive.DefaultLocomotive;
import de.wfb.rail.events.LocomotiveModelChangedEvent;
import de.wfb.rail.events.LocomotiveSelectedEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://docs.oracle.com/javafx/2/ui_controls/table-view.htm
 */
public class LocomotiveListPane extends VBox implements ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LogManager.getLogger(LocomotiveListPane.class);

	private TableView<DefaultLocomotive> tableView;

	private final ObservableList<DefaultLocomotive> data = FXCollections.observableArrayList();

	@Autowired
	private ModelFacade modelFacade;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void synchronizeModel() {

		data.clear();

		final List<DefaultLocomotive> locomotives = modelFacade.getLocomotives();
		if (CollectionUtils.isNotEmpty(locomotives)) {
			data.addAll(locomotives);
		}
	}

	public void setup(final Stage stage, final boolean displayDeleteButton) {

		logger.trace("LocomotiveListPane setup()");

		tableView = new TableView<DefaultLocomotive>();
		tableView.setEditable(true);

		addColumns(stage, displayDeleteButton);

		// insert data
		tableView.setItems(data);

		// set selection listener
		tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {

			if (newSelection != null) {

				final LocomotiveSelectedEvent locomotiveSelectedEvent = new LocomotiveSelectedEvent(this, newSelection);
				applicationEventPublisher.publishEvent(locomotiveSelectedEvent);
			}
		});

		setSpacing(5);
		setPadding(new Insets(10, 10, 10, 10));

		getChildren().addAll(tableView);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addColumns(final Stage stage, final boolean displayDeleteButton) {

		// column for the name
		final TableColumn<DefaultLocomotive, String> nameTableColumn = new TableColumn<DefaultLocomotive, String>(
				"Name");
		nameTableColumn.setCellValueFactory(new PropertyValueFactory<DefaultLocomotive, String>("name"));

		// column for the address
		final TableColumn<DefaultLocomotive, Integer> addressTableColumn = new TableColumn<DefaultLocomotive, Integer>(
				"Address");
		addressTableColumn.setCellValueFactory(new PropertyValueFactory<DefaultLocomotive, Integer>("address"));

		// delete table column
		final TableColumn deleteTableColumn = new TableColumn("Action");
		deleteTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		final Callback<TableColumn<DefaultLocomotive, String>, TableCell<DefaultLocomotive, String>> deleteButtonCellFactory = new Callback<TableColumn<DefaultLocomotive, String>, TableCell<DefaultLocomotive, String>>() {

			@Override
			public TableCell call(final TableColumn<DefaultLocomotive, String> param) {

				final TableCell<DefaultLocomotive, String> cell = new TableCell<DefaultLocomotive, String>() {

					final Button deleteButton = new Button("Delete");

					@Override
					public void updateItem(final String item, final boolean empty) {

						super.updateItem(item, empty);

						if (empty) {

							setGraphic(null);
							setText(null);

							return;
						}

						deleteButton.setOnAction(event -> {

							final DefaultLocomotive locomotive = getTableView().getItems().get(getIndex());

							logger.info(locomotive.getName() + "   " + locomotive.getAddress());

							final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.setTitle("Confirm Deletion of Locomotive");
							alert.setHeaderText("Confirm deletion.");
							alert.setContentText(
									"Do you really want to remove the locomotive " + locomotive.getName() + " ?");

							alert.showAndWait().ifPresent(type -> {

								logger.info(type);

								if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
									logger.info("OK_DONE");
								} else if (type.getButtonData() == ButtonBar.ButtonData.YES) {
									logger.info("YES");
								} else if (type.getButtonData() == ButtonBar.ButtonData.NO) {
									logger.info("NO");
								} else if (type.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
									logger.info("CANCEL");
								} else {
									logger.info("Unknown");
								}
							});

							if (alert.getResult().equals(ButtonType.OK)) {

								System.out.println("Deleting ...");

								modelFacade.deleteLocomotive(locomotive);

								// write the model to disk
								modelFacade.storeLocomotiveModel(modelFacade.getCurrentLocomotivesModel());

							} else if (alert.getResult().equals(ButtonType.YES)) {

								System.out.println("Deleting ...");

								modelFacade.deleteLocomotive(locomotive);

								// write the model to disk
								modelFacade.storeLocomotiveModel(modelFacade.getCurrentLocomotivesModel());

							} else {

								System.out.println("Not Deleting ...");

							}

						});

						setGraphic(deleteButton);
						setText(null);
					}
				};

				return cell;
			}
		};

		deleteTableColumn.setCellFactory(deleteButtonCellFactory);

		// Image cell factory
		final Callback<TableColumn<DefaultLocomotive, String>, TableCell<DefaultLocomotive, String>> imageCellFactory = new Callback<TableColumn<DefaultLocomotive, String>, TableCell<DefaultLocomotive, String>>() {

			@Override
			public TableCell call(final TableColumn<DefaultLocomotive, String> param) {

				final TableCell<DefaultLocomotive, String> tableCell = new TableCell<DefaultLocomotive, String>() {

					@Override
					public void updateItem(final String item, final boolean empty) {

						super.updateItem(item, empty);

						if (empty) {

							setGraphic(null);
							setText(null);

							return;

						}

						final DefaultLocomotive locomotive = getTableView().getItems().get(getIndex());

						final String imageFilename = locomotive.getImageFilename();

						logger.info("imageFilename: " + imageFilename);

						Image image = null;
						if (StringUtils.isEmpty(imageFilename)) {

							try {
								final InputStream selectedInputStream = new FileInputStream(
										"src/main/resources/default_locomotive_image.png");
								image = new Image(selectedInputStream);
							} catch (final FileNotFoundException e) {
								logger.error(e.getMessage(), e);
							}

						} else {
							image = new Image(imageFilename);
						}

						setGraphic(null);
						if (image != null) {

							final ImageView imageView = new ImageView();
							imageView.setImage(image);
							imageView.setFitHeight(100);
							imageView.setFitWidth(100);

							setGraphic(imageView);
						}

						setText(null);
						setUserData(locomotive);
					}

				};

				final EventHandler<MouseEvent> oneClickHandler = new EventHandler<MouseEvent>() {

					@Override
					public void handle(final MouseEvent event) {

						final Parent parent = (Parent) event.getSource();

						final TableCell<DefaultLocomotive, String> tableCell = (TableCell<DefaultLocomotive, String>) parent;

						logger.info("Hello parent: " + parent + " this: " + tableCell);

						final Object graphicAsObject = tableCell.getGraphic();

						if (graphicAsObject != null) {

							final FileChooser fileChooser = new FileChooser();
							fileChooser.setTitle("Open Locomotive Image");
							final File file = fileChooser.showOpenDialog(stage);
							if (file != null) {

								logger.info("Uri: " + file.toURI().toString());

								final Image image = new Image(file.toURI().toString());

								final ImageView imageView = (ImageView) graphicAsObject;
								imageView.setImage(image);

								logger.info("UserData: " + tableCell.getUserData());

								final DefaultLocomotive locomotive = (DefaultLocomotive) tableCell.getUserData();
								logger.info("locomotive: " + locomotive);

								locomotive.setImageFilename(file.toURI().toString());

								// write the model to disk
								modelFacade.storeLocomotiveModel(modelFacade.getCurrentLocomotivesModel());
							}
						}
					}
				};

				tableCell.addEventHandler(MouseEvent.MOUSE_CLICKED, oneClickHandler);

				return tableCell;
			}
		};

		// image column
		final TableColumn<DefaultLocomotive, String> imageTableColumn = new TableColumn<DefaultLocomotive, String>(
				"Image");
		imageTableColumn.setCellFactory(imageCellFactory);

		// add all columns
		tableView.getColumns().addAll(nameTableColumn, addressTableColumn);
		tableView.getColumns().add(imageTableColumn);
		if (displayDeleteButton) {
			tableView.getColumns().add(deleteTableColumn);
		}
	}

	public void clear() {

		if (tableView != null) {

			getChildren().remove(tableView);
			tableView = null;
		}
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {

		if (event instanceof LocomotiveModelChangedEvent) {

			final LocomotiveModelChangedEvent locomotiveModelChangedEvent = (LocomotiveModelChangedEvent) event;

			final DefaultLocomotive locomotive = locomotiveModelChangedEvent.getLocomotive();

			switch (locomotiveModelChangedEvent.getOperationType()) {

			case ADDED:
				data.add(locomotive);
				break;

			case REMOVED:
				data.remove(locomotive);
				break;

			default:
				break;
			}
		}
	}

}
