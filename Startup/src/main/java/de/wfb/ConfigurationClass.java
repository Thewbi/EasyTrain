package de.wfb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.wfb.dialogs.LayoutElementSelectionPane;
import de.wfb.dialogs.SidePane;
import de.wfb.dialogs.TurnoutDetailsPane;
import de.wfb.model.DefaultModel;
import de.wfb.model.Model;
import de.wfb.model.facade.DefaultModelFacade;
import de.wfb.model.node.DefaultNodeFactory;
import de.wfb.model.service.DefaultIdService;
import de.wfb.model.service.DefaultModelPersistenceService;
import de.wfb.model.service.DefaultModelService;
import de.wfb.model.service.DefaultNodeConnectorService;
import de.wfb.model.service.ModelService;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.factory.DefaultSVGPathFactory;
import de.wfb.rail.factory.DefaultSerialPortFactory;
import de.wfb.rail.service.DefaultProtocolService;

/**
 * https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-configuration-classes
 *
 * 3. Configuration Classes
 *
 * Spring Boot favors Java-based configuration. Although it is possible to use
 * SpringApplication with XML sources, we generally recommend that your primary
 * source be a single @Configuration class. Usually the class that defines the
 * main method is a good candidate as the primary @Configuration.
 */
@ComponentScan("de.wfb")
@Configuration
public class ConfigurationClass {

	@Bean
	public DefaultSVGPathFactory SVGPathFactory() {
		return new DefaultSVGPathFactory();
	}

	@Bean
	public ModelService ModelService() {
		return new DefaultModelService();
	}

	@Bean
	public Model Model() {
		return new DefaultModel();
	}

	@Bean
	public DefaultIdService DefaultIdService() {
		return new DefaultIdService();
	}

	@Bean
	public DefaultNodeFactory DefaultNodeFactory() {
		return new DefaultNodeFactory();
	}

	@Bean
	public DefaultNodeConnectorService DefaultNodeConnectorService() {
		return new DefaultNodeConnectorService();
	}

	@Bean
	public DefaultModelPersistenceService DefaultModelPersistenceService() {
		return new DefaultModelPersistenceService();
	}

	@Bean
	public DefaultProtocolFacade DefaultProtocolFacade() {
		return new DefaultProtocolFacade();
	}

	@Bean
	public DefaultProtocolService DefaultProtocolService() {
		return new DefaultProtocolService();
	}

	@Bean
	public DefaultModelFacade DefaultModelFacade() {
		return new DefaultModelFacade();
	}

	@Bean
	public DefaultSerialPortFactory DefaultSerialPortFactory() {
		return new DefaultSerialPortFactory();
	}

	@Bean
	public SidePane SidePane() {
		return new SidePane();
	}

	@Bean
	public LayoutElementSelectionPane LayoutElementSelectionPane() {
		return new LayoutElementSelectionPane();
	}

	@Bean
	public TurnoutDetailsPane TurnoutDetailsPane() {
		return new TurnoutDetailsPane();
	}

}
