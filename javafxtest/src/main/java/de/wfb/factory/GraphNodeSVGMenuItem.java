package de.wfb.factory;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.wfb.model.Model;
import de.wfb.model.facade.ModelFacade;
import de.wfb.rail.converter.Converter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class GraphNodeSVGMenuItem extends MenuItem {

	private static final Logger logger = LogManager.getLogger(GraphNodeSVGMenuItem.class);

	@Autowired
	private Converter<Model, String> modelToSVGConverter;

	@Autowired
	private ModelFacade modelFacade;

	private Stage stage;

	/**
	 * ctor
	 *
	 * @param title
	 */
	public GraphNodeSVGMenuItem(final String title) {

		super(title);

		setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event) {

				logger.info("GraphNodeSVGMenuItem");

				final FileChooser fileChooser = new FileChooser();

				// set extension filter for text files
				final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)",
						"*.svg");
				fileChooser.getExtensionFilters().add(extFilter);

				// show save file dialog
				final File file = fileChooser.showSaveDialog(stage);
				if (file != null) {

					modelToSVGConverter.convert(modelFacade.getModel(), file.getAbsolutePath());
				}
			}
		});
	}

	public void setStage(final Stage stage) {
		this.stage = stage;
	}
}
