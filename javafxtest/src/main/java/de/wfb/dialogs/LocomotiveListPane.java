package de.wfb.dialogs;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
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

	public void setup() {

		logger.trace("LocomotiveListPane setup()");

		tableView = new TableView<DefaultLocomotive>();
		tableView.setEditable(true);

		addColumns();

		tableView.setItems(data);

		tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

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
	private void addColumns() {

		// column for the name
		final TableColumn<DefaultLocomotive, String> nameTableColumn = new TableColumn<DefaultLocomotive, String>(
				"Name");
		nameTableColumn.setCellValueFactory(new PropertyValueFactory<DefaultLocomotive, String>("name"));

		// column for the address
		final TableColumn<DefaultLocomotive, Integer> addressTableColumn = new TableColumn<DefaultLocomotive, Integer>(
				"Address");
		addressTableColumn.setCellValueFactory(new PropertyValueFactory<DefaultLocomotive, Integer>("address"));

		final TableColumn deleteTableColumn = new TableColumn("Action");
		deleteTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		final Callback<TableColumn<DefaultLocomotive, String>, TableCell<DefaultLocomotive, String>> cellFactory = //
				new Callback<TableColumn<DefaultLocomotive, String>, TableCell<DefaultLocomotive, String>>() {
					@Override
					public TableCell call(final TableColumn<DefaultLocomotive, String> param) {
						final TableCell<DefaultLocomotive, String> cell = new TableCell<DefaultLocomotive, String>() {

							final Button btn = new Button("Delete");

							@Override
							public void updateItem(final String item, final boolean empty) {

								super.updateItem(item, empty);

								if (empty) {

									setGraphic(null);
									setText(null);

								} else {

									btn.setOnAction(event -> {

										final DefaultLocomotive locomotive = getTableView().getItems().get(getIndex());

										logger.info(locomotive.getName() + "   " + locomotive.getAddress());

										final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
										alert.setTitle("Confirm Deletion of Locomotive");
										alert.setHeaderText("Confirm deletion.");
										alert.setContentText("Do you really want to remove the locomotive "
												+ locomotive.getName() + " ?");

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
									setGraphic(btn);
									setText(null);
								}
							}
						};
						return cell;
					}
				};

		deleteTableColumn.setCellFactory(cellFactory);

		tableView.getColumns().addAll(nameTableColumn, addressTableColumn, deleteTableColumn);
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
