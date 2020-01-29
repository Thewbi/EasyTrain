package de.wfb.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

public class DefaultViewModel implements ViewModel {

	private static final Logger logger = LogManager.getLogger(DefaultViewModel.class);

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private final GridElement[][] viewModel = new GridElement[ViewModel.ROWS][ViewModel.COLUMNS];

	@Override
	public GridElement[][] getViewModel() {
		return viewModel;
	}

	@Override
	public void clear() {

		logger.info("Remove view model elements");

		for (int rows = 0; rows < ViewModel.ROWS; rows++) {

			for (int columns = 0; columns < ViewModel.COLUMNS; columns++) {

				final int x = rows;
				final int y = columns;

				viewModel[x][y] = null;
			}
		}
	}

}
