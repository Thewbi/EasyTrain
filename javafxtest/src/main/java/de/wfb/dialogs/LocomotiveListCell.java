package de.wfb.dialogs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.wfb.model.locomotive.Locomotive;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LocomotiveListCell extends ListCell<Locomotive> {

	private static final Logger logger = LogManager.getLogger(LocomotiveListCell.class);

	@Override
	protected void updateItem(final Locomotive item, final boolean empty) {

		super.updateItem(item, empty);
		setGraphic(null);
		setText(null);

		if (item != null) {

			final ImageView imageView = new ImageView(retrieveImage(item.getImageFilename()));
			imageView.setFitWidth(40);
			imageView.setFitHeight(40);
			setGraphic(imageView);
			setText(item.getName());
		}
	}

	private Image retrieveImage(final String imageFilename) {

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

		return image;
	}

}
