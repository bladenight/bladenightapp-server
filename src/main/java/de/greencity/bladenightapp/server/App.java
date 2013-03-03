package de.greencity.bladenightapp.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.WebSocket;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventsList;
import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.keyvaluestore.KeyValueStoreSingleton;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.procession.tasks.ComputeScheduler;
import de.greencity.bladenightapp.procession.tasks.ParticipantCollector;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;

public class App 
{
	static final int port = 8081;

	public static void main( String[] args )
	{
		initializeApplicationConfiguration();
		initializeLogger();
		initializeRouteStore();
		initializeEventsList();
		initializeProcession();
		tryStartServer();
	}

	public static void tryStartServer() {
		try {
			startServer();
		} catch (Exception e) {
			log.error("Failed to start:", e);
		}
	}

	public static void startServer() throws Exception {
		fr.ocroquette.wampoc.server.WampServer wampocServer = new BladenightWampServer();

		BladenightJettyServerHandler wampocHandler = new BladenightJettyServerHandler(wampocServer) {

			@Override
			public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
				log.info("Got new connection from " + request.getRemoteAddr());
				return super.doWebSocketConnect(request, protocol);
			}

		};


        String httpdocsPath = FileUtils.toFile(App.class.getResource("/httpdocs")).getAbsolutePath();

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase(httpdocsPath);
		wampocHandler.setHandler(resourceHandler);

		int port = getPortToListenTo();
		org.eclipse.jetty.server.Server server = new Server(port);
		server.setHandler(wampocHandler);
		server.start();
		log.info("The server is now listening for HTTP and WebSocket connections on port " + port);
		server.join();
	}

	private static void initializeApplicationConfiguration() {
		String propertyName = "bnserver.configuration";
		String path = System.getProperty(propertyName);
		if ( path == null ) {
			System.err.println("fatal error: please provide the path of the configuration file as Java system property (-D"+propertyName+"=/path/...)");
			System.exit(1);
		}

		if ( ! KeyValueStoreSingleton.readFromFile(path) ) {
			System.err.println("fatal error: Unable to read the configuration file at:\n"+path+"\n");
			System.exit(1);
		}
	}

	private static Integer getPortToListenTo() {
		return Integer.valueOf(KeyValueStoreSingleton.getString("bnserver.port"));
	}
	
	private static void initializeLogger() {

		String log4jConfiguration = System.getProperty("log4j.properties");

		if ( log4jConfiguration == null ) {
			log4jConfiguration = KeyValueStoreSingleton.getPath("bnserver.log4j.configurationpath", "log4j.properties");
			log4jConfiguration = "file://" + log4jConfiguration;
			System.setProperty("log4j.configuration",log4jConfiguration);
		}
		
		log = LogFactory.getLog(App.class);
		log.debug("Logger initinalized, log4j.properties="+log4jConfiguration);
	}
	
	private static void initializeRouteStore() {
		String configurationKey = "bnserver.routes.path";
		String asString = KeyValueStoreSingleton.getPath(configurationKey);
		File asFile = new File(asString);
		if ( ! asFile.isDirectory() ) {
			log.error("Invalid path for route files: " + configurationKey + "=" + asString);
		}
		RouteStore routeStore = new RouteStore(asFile);
		RouteStoreSingleton.setInstance(routeStore);
		log.info("Route store initialized, there are " + routeStore.getAvailableRoutes().size() + " different routes available.");
	}

	private static void initializeEventsList() {
		String configurationKey = "bnserver.events.file";
		String asString = KeyValueStoreSingleton.getPath(configurationKey);
		File asFile = new File(asString);
		if ( ! asFile.isFile() ) {
			log.error("Invalid file for the events: " + configurationKey + "=" + asString);
		}
		EventsList eventsList;
		try {
			eventsList = EventsList.newFromFile(asFile);
			EventsListSingleton.setEventsList(eventsList);
			log.info("Events list initialized with " + eventsList.size() + " events.");
		} catch (IOException e) {
			log.error("Could not load event list:",e);
		}
	}

	private static void initializeProcession() {
		Procession procession = new Procession();
		Event nextEvent = EventsListSingleton.getInstance().getNextEvent();
		Route route = RouteStoreSingleton.getInstance().getRoute(nextEvent.getRouteName());
		procession.setRoute(route);
		ProcessionSingleton.setProcession(procession);
		
		double smoothingFactor = KeyValueStoreSingleton.getDouble("bnserver.procession.smoothing", 0.0);
		procession.setUpdateSmoothingFactor(smoothingFactor);
		
		new Thread(new ComputeScheduler(procession, 1000)).start();
		new Thread(new ParticipantCollector(procession, 50.0, 1000)).start();
	}

	private static Log log;

}
