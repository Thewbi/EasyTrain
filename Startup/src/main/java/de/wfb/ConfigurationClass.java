package de.wfb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import de.wbi.model.serializer.DefaultRouteDeserializer;
import de.wbi.model.serializer.DefaultRouteSerializer;
import de.wfb.configuration.ConfigurationService;
import de.wfb.configuration.DefaultConfigurationService;
import de.wfb.dialogs.BlockNavigationPane;
import de.wfb.dialogs.DrivingThreadControlPane;
import de.wfb.dialogs.EmergencyStopPane;
import de.wfb.dialogs.LayoutElementSelectionPane;
import de.wfb.dialogs.LocomotiveAddPane;
import de.wfb.dialogs.LocomotiveListPane;
import de.wfb.dialogs.LocomotiveListStage;
import de.wfb.dialogs.PlaceLocomotivePane;
import de.wfb.dialogs.PlaceLocomotiveStage;
import de.wfb.dialogs.RailDetailsPane;
import de.wfb.dialogs.RoutingPane;
import de.wfb.dialogs.SidePane;
import de.wfb.dialogs.SignalDetailsPane;
import de.wfb.dialogs.ThrottlePane;
import de.wfb.dialogs.ThrottleStage;
import de.wfb.dialogs.TurnoutDetailsPane;
import de.wfb.factory.DefaultMenuBarFactory;
import de.wfb.factory.DefaultSceneFactory;
import de.wfb.factory.GraphNodeSVGMenuItem;
import de.wfb.factory.ResetMenuItem;
import de.wfb.factory.RoutingControllerMenuItem;
import de.wfb.factory.StartMenuItem;
import de.wfb.factory.StopMenuItem;
import de.wfb.factory.XTrntStatusMenuItem;
import de.wfb.javafxtest.controller.LayoutGridController;
import de.wfb.javafxtest.controls.GridElementFactory;
import de.wfb.model.DefaultModel;
import de.wfb.model.DefaultViewModel;
import de.wfb.model.Model;
import de.wfb.model.driving.RandomRoutingController;
import de.wfb.model.facade.DefaultModelFacade;
import de.wfb.model.facade.DefaultRoutingFacade;
import de.wfb.model.locomotive.DefaultLocomotiveFactory;
import de.wfb.model.node.DefaultRailNodeFactory;
import de.wfb.model.service.DefaultBlockService;
import de.wfb.model.service.DefaultFeedbackBlockService;
import de.wfb.model.service.DefaultIdService;
import de.wfb.model.service.DefaultModelPersistenceService;
import de.wfb.model.service.DefaultModelService;
import de.wfb.model.service.DefaultRoutingService;
import de.wfb.model.service.ModelService;
import de.wfb.model.service.RoutingController;
import de.wfb.model.service.RoutingService;
import de.wfb.model.strategy.DefaultGraphColorStrategy;
import de.wfb.rail.controller.TimedDrivingThreadController;
import de.wfb.rail.converter.Converter;
import de.wfb.rail.facade.DefaultProtocolFacade;
import de.wfb.rail.factory.DefaultSVGPathFactory;
import de.wfb.rail.factory.NRSerialPortFactory;
import de.wfb.rail.service.DefaultDrivingService;
import de.wfb.rail.service.DefaultProtocolService;
import de.wfb.rail.service.DefaultTurnoutService;
import de.wfb.rail.service.Route;
import de.wfb.threads.DefaultTimedDrivingThreadController;
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

//	@Bean
//	public DefaultSerialPortFactory DefaultSerialPortFactory() {
//		return new DefaultSerialPortFactory();
//	}

	@Bean
	public NRSerialPortFactory NRSerialPortFactory() {
		return new NRSerialPortFactory();
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
	public SignalDetailsPane SignalDetailsPane() {
		return new SignalDetailsPane();
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
	public TimedDrivingThread TimedDrivingThread() {
		return new TimedDrivingThread();
	}

	@Bean
	public LayoutGridController LayoutGridController() {
		return new LayoutGridController();
	}

	@Bean
	public DefaultDebugFacade DefaultDebugFacade() {
		return new DefaultDebugFacade();
	}

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
	public DefaultRoutingFacade DefaultRoutingFacade() {
		return new DefaultRoutingFacade();
	}

	@Bean
	public DefaultFeedbackBlockService DefaultFeedbackBlockService() {
		return new DefaultFeedbackBlockService();
	}

	@Bean
	public DefaultMenuBarFactory DefaultMenuBarFactory() {
		return new DefaultMenuBarFactory();
	}

	@Bean
	public DefaultSceneFactory DefaultSceneFactory() {
		return new DefaultSceneFactory();
	}

	@Bean
	public DefaultTurnoutService DefaultTurnoutService() {
		return new DefaultTurnoutService();
	}

	@Bean
	public XTrntStatusMenuItem XTrntStatusMenuItem() {
		return new XTrntStatusMenuItem("XTrntStatus Command");
	}

	@Bean
	public GridElementFactory GridElementFactory() {
		return new GridElementFactory();
	}

	@Bean
	public GraphNodeSVGMenuItem GraphNodeSVGMenuItem() {
		return new GraphNodeSVGMenuItem("Export Layout-Nodes to SVG");
	}

	@Bean
	public LayoutSVGConverter LayoutSVGConverter() {
		return new LayoutSVGConverter();
	}

	@Bean
	public DefaultViewModel DefaultViewModel() {
		return new DefaultViewModel();
	}

	@Bean
	public RoutingPane RoutingPane() {
		return new RoutingPane();
	}

	@Bean
	public RoutingControllerMenuItem RoutingControllerMenuItem() {
		return new RoutingControllerMenuItem("Start");
	}

	@Bean
	public StartMenuItem StartMenuItem() {
		return new StartMenuItem("Emergency Start");
	}

	@Bean
	public StopMenuItem StopMenuItem() {
		return new StopMenuItem("Emergency Stop");
	}

	@Bean
	public ResetMenuItem ResetMenuItem() {
		return new ResetMenuItem("Reset");
	}

	@Bean
	public RoutingController RoutingController() {
		return new RandomRoutingController();
	}

	@Bean
	public Converter<Route, String> RouteSerializer() {
		return new DefaultRouteSerializer();
	}

	@Bean
	public Converter<String, Route> RouteDeserializer() {
		return new DefaultRouteDeserializer();
	}

	@Bean
	public TimedDrivingThreadController TimedDrivingThreadController() {
		return new DefaultTimedDrivingThreadController();
	}

	@Bean
	public DrivingThreadControlPane DrivingThreadControlPane() {
		return new DrivingThreadControlPane();
	}

	@Bean
	public RoutingService RoutingService() {
		return new DefaultRoutingService();
//		return new PreRecordedRoutingService();
	}

	@Bean
	public ConfigurationService ConfigurationService() {
		return new DefaultConfigurationService();
	}

	@Bean
	public DefaultLocomotiveFactory DefaultLocomotiveFactory() {
		return new DefaultLocomotiveFactory();
	}

	@Bean
	public EmergencyStopPane EmergencyStopPane() {
		return new EmergencyStopPane();
	}

}
