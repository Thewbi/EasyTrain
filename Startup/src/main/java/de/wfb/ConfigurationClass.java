package de.wfb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import de.wfb.dialogs.BlockNavigationPane;
import de.wfb.dialogs.LayoutElementSelectionPane;
import de.wfb.dialogs.LocomotiveAddPane;
import de.wfb.dialogs.LocomotiveListPane;
import de.wfb.dialogs.LocomotiveListStage;
import de.wfb.dialogs.PlaceLocomotivePane;
import de.wfb.dialogs.PlaceLocomotiveStage;
import de.wfb.dialogs.RailDetailsPane;
import de.wfb.dialogs.SidePane;
import de.wfb.dialogs.ThrottlePane;
import de.wfb.dialogs.ThrottleStage;
import de.wfb.dialogs.TurnoutDetailsPane;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.model.DefaultModel;
import de.wfb.model.Model;
import de.wfb.model.facade.DefaultModelFacade;
import de.wfb.model.node.DefaultRailNodeFactory;
import de.wfb.model.service.DefaultBlockService;
import de.wfb.model.service.DefaultIdService;
import de.wfb.model.service.DefaultModelPersistenceService;
import de.wfb.model.service.DefaultModelService;
import de.wfb.model.service.DefaultRoutingService;
import de.wfb.model.service.ModelService;
import de.wfb.model.strategy.DefaultGraphColorStrategy;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.factory.DefaultSVGPathFactory;
import de.wfb.rail.factory.DefaultSerialPortFactory;
import de.wfb.rail.service.DefaultDrivingService;
import de.wfb.rail.service.DefaultProtocolService;
import de.wfb.threads.TimedDrivingThread;

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
@EnableScheduling
@Configuration
public class ConfigurationClass implements SchedulingConfigurer {

	/**
	 * https://stackoverflow.com/questions/39066898/how-to-use-scheduled-annotation-in-non-spring-boot-projects/39072611
	 *
	 * @return
	 */
	@Bean
	public CustomThreadPoolScheduler taskScheduler() {

		final CustomThreadPoolScheduler scheduler = new CustomThreadPoolScheduler();
		scheduler.setPoolSize(10);
		scheduler.setThreadGroupName("threadgroupname");
		scheduler.setThreadNamePrefix("threadPrefix");
		scheduler.setAwaitTerminationSeconds(1);

		return scheduler;
	}

	/**
	 * https://stackoverflow.com/questions/39066898/how-to-use-scheduled-annotation-in-non-spring-boot-projects/39072611
	 */
	@Override
	public void configureTasks(final ScheduledTaskRegistrar registrar) {

		final TaskScheduler scheduler = this.taskScheduler();

		registrar.setTaskScheduler(scheduler);
	}

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
	public DefaultRailNodeFactory DefaultRailNodeFactory() {
		return new DefaultRailNodeFactory();
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

	@Bean
	public RailDetailsPane RailDetailsPane() {
		return new RailDetailsPane();
	}

	@Bean
	public ThrottleStage ThrottleStage() {
		return new ThrottleStage();
	}

	@Bean
	public ThrottlePane ThrottlePane() {
		return new ThrottlePane();
	}

	@Bean
	public EvtSenCommandThread EvtSenCommandThread() {
		return new EvtSenCommandThread();
	}

	@Bean
	public DefaultRoutingService DefaultRoutingService() {
		return new DefaultRoutingService();
	}

	@Bean
	public LayoutGridController LayoutGridController() {
		return new LayoutGridController();
	}

	@Bean
	public DefaultDebugFacade DefaultDebugFacade() {
		return new DefaultDebugFacade();
	}

//	@Bean
//	public StaticGraphColorStrategy StaticGraphColorStrategy() {
//		return new StaticGraphColorStrategy();
//	}

	@Bean
	public DefaultGraphColorStrategy DefaultGraphColorStrategy() {
		return new DefaultGraphColorStrategy();
	}

	@Bean
	public LocomotiveListPane LocomotiveListPane() {
		return new LocomotiveListPane();
	}

	@Bean
	public LocomotiveListStage LocomotiveListStage() {
		return new LocomotiveListStage();
	}

	@Bean
	public LocomotiveAddPane LocomotiveAddPane() {
		return new LocomotiveAddPane();
	}

	@Bean
	public DefaultBlockService DefaultBlockService() {
		return new DefaultBlockService();
	}

	@Bean
	public PlaceLocomotiveStage PlaceLocomotiveStage() {
		return new PlaceLocomotiveStage();
	}

	@Bean
	public PlaceLocomotivePane PlaceLocomotivePane() {
		return new PlaceLocomotivePane();
	}

	@Bean
	public BlockNavigationPane BlockNavigationPane() {
		return new BlockNavigationPane();
	}

	@Bean
	public DefaultDrivingService DefaultDrivingService() {
		return new DefaultDrivingService();
	}

	@Bean
	public TimedDrivingThread TimedDrivingThread() {
		return new TimedDrivingThread();
	}

}
