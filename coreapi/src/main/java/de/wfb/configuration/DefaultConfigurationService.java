package de.wfb.configuration;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class DefaultConfigurationService implements ConfigurationService {

	private boolean automatedDrivingActive = false;
	// private boolean automatedDrivingActive = true;

	// move to the config service
	private static final double DRIVING_SPEED_ABSOLUTE = 50.0d;

	// move to the config service
	private static final double DRIVING_SPEED_SLOW_PERCENTAGE = 40.0d;

	@Override
	public String getConfiguration(final String configurationKey) {

		if (StringUtils.isBlank(configurationKey)) {

			return StringUtils.EMPTY;
		}

		final String trimmedKey = StringUtils.trim(configurationKey);

		if (StringUtils.equalsIgnoreCase(trimmedKey, ConfigurationConstants.TIMED_DRIVING_THREAD_ACTIVE)) {

			return Boolean.TRUE.toString();

		} else if (StringUtils.equalsIgnoreCase(trimmedKey, ConfigurationConstants.WRITE_ROUTES_TO_FILE)) {

			return Boolean.FALSE.toString();

		} else if (StringUtils.equalsIgnoreCase(trimmedKey, ConfigurationConstants.AUTOMATED_DRIVING_ACTIVE)) {

			return Boolean.toString(automatedDrivingActive);

		} else if (StringUtils.equalsIgnoreCase(trimmedKey, ConfigurationConstants.DRIVING_SPEED_ABSOLUTE)) {

			return "50.0";

		} else if (StringUtils.equalsIgnoreCase(trimmedKey, ConfigurationConstants.DRIVING_SPEED_SLOW_PERCENTAGE)) {

			return "40.0";
		}

		throw new IllegalArgumentException("Unknown configuration key: " + configurationKey);
	}

	@Override
	public double getConfigurationAsDouble(final String configurationKey) {

		final String configurationValue = getConfiguration(configurationKey);

		final double result = NumberUtils.toDouble(configurationValue);

		return result;
	}

	@Override
	public boolean getConfigurationAsBoolean(final String configurationKey) {

		final String configurationValue = getConfiguration(configurationKey);

		final Boolean booleanObject = BooleanUtils.toBooleanObject(configurationValue);

		if (booleanObject == null) {
			throw new IllegalArgumentException("Configuration key: " + configurationKey + " is not a boolean value!");
		}

		return booleanObject;
	}

	@Override
	public void setConfigurationAsBoolean(final String configurationKey, final boolean value) {

		if (StringUtils.isBlank(configurationKey)) {

			return;
		}

		final String trimmedKey = StringUtils.trim(configurationKey);

		if (StringUtils.equalsIgnoreCase(trimmedKey, ConfigurationConstants.AUTOMATED_DRIVING_ACTIVE)) {

			automatedDrivingActive = value;

		} else {

			throw new IllegalArgumentException("Unknown configuration key: " + configurationKey);

		}
	}

	public boolean isAutomatedDrivingActive() {
		return automatedDrivingActive;
	}

	public void setAutomatedDrivingActive(final boolean automatedDrivingActive) {
		this.automatedDrivingActive = automatedDrivingActive;
	}

}
