package de.wfb.configuration;

public interface ConfigurationService {

	String getConfiguration(String configurationKey);

	boolean getConfigurationAsBoolean(String configurationKey);

	void setConfigurationAsBoolean(String configurationKey, boolean value);

	double getConfigurationAsDouble(String configurationKey);

}
