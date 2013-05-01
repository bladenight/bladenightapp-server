package de.greencity.bladenightapp.server;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.WebSocket;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.keyvaluestore.KeyValueStoreSingleton;
import de.greencity.bladenightapp.persistence.ListPersistor;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.procession.tasks.ComputeScheduler;
import de.greencity.bladenightapp.procession.tasks.ParticipantCollector;
import de.greencity.bladenightapp.protocol.Protocol;
import de.greencity.bladenightapp.relationships.Relationship;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.relationships.RelationshipStoreSingleton;
import de.greencity.bladenightapp.relationships.tasks.RelationshipCollector;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;
import de.greencity.bladenightapp.security.PasswordSafe;
import de.greencity.bladenightapp.security.PasswordSafeSingleton;
import fr.ocroquette.wampoc.server.TextFrameEavesdropper;
import fr.ocroquette.wampoc.server.WampServer;

public class App 
{
	static final int port = 8081;

	public static void main( String[] args ) throws IOException
	{
		initializeApplicationConfiguration();
		initializeLogger();
		initializeRouteStore();
		initializeEventsList();
		initializeProcession();
		initializeRelationshipStore();
		initializePasswordSafe();
		tryStartServer();
	}

	public static void tryStartServer() {
		try {
			startServer();
		} catch (Exception e) {
			getLog().error("Failed to start:", e);
		}
	}

	public static void startServer() throws Exception {
		fr.ocroquette.wampoc.server.WampServer wampocServer = new BladenightWampServer();

		initializeProtocol(wampocServer);

		BladenightJettyServerHandler wampocHandler = new BladenightJettyServerHandler(wampocServer) {

			@Override
			public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
				getLog().info("Got new connection from " + request.getRemoteAddr());
				return super.doWebSocketConnect(request, protocol);
			}

		};


		org.eclipse.jetty.server.Server server = createServer();

		String httpdocsConfigKey = "bnserver.httpdocs";
		String httpdocsPath = KeyValueStoreSingleton.getPath(httpdocsConfigKey, null); 
		if ( httpdocsPath == null ) {
			getLog().info("No httpdocs path has been set ("+httpdocsConfigKey+")");
		}
		else if ( ! new File(httpdocsPath).isDirectory() ) {
			getLog().fatal("The provided httpdocs path is not a valid directory: " + httpdocsPath);
			System.exit(1);
		}
		else {
			getLog().info("Config: httpdocsPath="+httpdocsPath);
			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setDirectoriesListed(true);
			resourceHandler.setResourceBase(httpdocsPath);
			wampocHandler.setHandler(resourceHandler);
		}

		server.setHandler(wampocHandler);

		try {
			server.start();
		}
		catch(Exception e) {
			getLog().error("Failed to start server: " + e);
			System.exit(1);
		}
		getLog().info("The server is now listening port " + port);
		getLog().info("SSL is" + ( useSsl() ? " " : " not " ) + "activated");
		server.join();
	}

	private static Server createServer() {
		if ( ! useSsl() ) {
			return new Server(getPortToListenTo());
		}
		Server server = new Server();

		SslContextFactory sslContextFactory = new SslContextFactory(KeyValueStoreSingleton.getPath("bnserver.network.ssl.keystore.path"));
		sslContextFactory.setKeyStorePassword(KeyValueStoreSingleton.getString("bnserver.network.ssl.keystore.password"));
		sslContextFactory.setKeyManagerPassword(KeyValueStoreSingleton.getString("bnserver.network.ssl.keystore.password"));
		sslContextFactory.setTrustStore(KeyValueStoreSingleton.getPath("bnserver.network.ssl.truststore.path"));
		sslContextFactory.setTrustStorePassword(KeyValueStoreSingleton.getString("bnserver.network.ssl.truststore.password"));
		sslContextFactory.setNeedClientAuth(true);

		SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(sslContextFactory);

		sslConnector.setPort(getPortToListenTo());

		// Add the SocketConnector to the server
		server.setConnectors(new Connector[] {sslConnector});

		return server;
	}

	private static boolean useSsl() {
		return KeyValueStoreSingleton.getInt("bnserver.network.ssl.enable", 0) != 0;
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
		String key = "bnserver.network.port";
		int port = 0 ;
		
		try {
			port = Integer.valueOf(KeyValueStoreSingleton.getString(key));
		}
		catch (Exception e) {
			getLog().error(e);
		}
		if ( port == 0 ) {
			getLog().error("Please provide a TCP port to bind ("+key+")");
			System.exit(1);
		}
		return port;
	}

	private static void initializeLogger() {

		String log4jConfiguration = System.getProperty("log4j.properties");

		if ( log4jConfiguration == null ) {
			log4jConfiguration = KeyValueStoreSingleton.getPath("bnserver.log4j.configurationpath", "log4j.properties");
			log4jConfiguration = "file://" + log4jConfiguration;
			System.setProperty("log4j.configuration",log4jConfiguration);
		}

		log = LogFactory.getLog(App.class);
		getLog().info("confog: logger initinalized, log4j.properties="+log4jConfiguration);
	}
	
	private static Log getLog() {
		if ( log == null )
			initializeLogger();
		return log;
	}

	private static void initializeRouteStore() {
		String configurationKey = "bnserver.routes.path";
		String asString = KeyValueStoreSingleton.getPath(configurationKey);
		File asFile = new File(asString);
		if ( ! asFile.isDirectory() ) {
			getLog().error("Invalid path for route files: " + configurationKey + "=" + asString);
		}
		RouteStore routeStore = new RouteStore(asFile);
		RouteStoreSingleton.setInstance(routeStore);
		getLog().info("Config: routeStorePath="+asString);
		getLog().info("Route store initialized, there are " + routeStore.getAvailableRoutes().size() + " different routes available.");
	}

	private static void initializeEventsList() {
		String configurationKey = "bnserver.events.dir";
		String asString = KeyValueStoreSingleton.getPath(configurationKey);
		File asFile = new File(asString);
		if ( ! asFile.isDirectory() ) {
			getLog().error("Invalid directory for the events: " + configurationKey + "=" + asString);
		}
		getLog().info("Config: eventStorePath="+asString);

		ListPersistor<Event> persistor = new ListPersistor<Event>(Event.class, asFile);

		EventList eventList = new EventList();
		eventList.setPersistor(persistor);
		try {
			eventList.read();
		} catch (Exception e) {
			getLog().error("Failed to read events: " + e.toString());
			System.exit(1);
		}
		EventsListSingleton.setInstance(eventList);
		getLog().info("Events list initialized with " + eventList.size() + " events.");
	}

	private static void initializeProcession() {
		Procession procession = new Procession();
		Event nextEvent = EventsListSingleton.getInstance().getActiveEvent();
		if ( nextEvent != null ) {
			Route route = RouteStoreSingleton.getInstance().getRoute(nextEvent.getRouteName());
			procession.setRoute(route);
		}
		else {
			getLog().warn("No upcoming event found");
		}
		ProcessionSingleton.setProcession(procession);

		double smoothingFactor = KeyValueStoreSingleton.getDouble("bnserver.procession.smoothing", 0.0);
		procession.setUpdateSmoothingFactor(smoothingFactor);

		getLog().info("Config: Procession smoothingFactor="+smoothingFactor);

		new Thread(new ComputeScheduler(procession, 1000)).start();

		initializeParticipantCollector(procession);

	}

	private static void initializeParticipantCollector(Procession procession) {
		long maxAbsoluteAge 			= KeyValueStoreSingleton.getLong("bnserver.procession.collector.absolute", 		30000	);
		double maxRelativeAgeFactor 	= KeyValueStoreSingleton.getDouble("bnserver.procession.collector.relative", 	5.0		);
		long period 					= KeyValueStoreSingleton.getLong("bnserver.procession.collector.period", 		1000	);

		getLog().info("Config: Procession collector maxAbsoluteAge="+maxAbsoluteAge);
		getLog().info("Config: Procession collector maxRelativeAgeFactor="+maxRelativeAgeFactor);
		getLog().info("Config: Procession collector period="+period);

		ParticipantCollector collector = new ParticipantCollector(procession);
		collector.setPeriod(period);
		collector.setMaxAbsoluteAge(maxAbsoluteAge);
		collector.setMaxRelativeAgeFactor(maxRelativeAgeFactor);

		new Thread(collector).start();
	}

	private static void initializeRelationshipStore() {
		RelationshipStore relationshipStore = new RelationshipStore();
		String configurationKey = "bnserver.relationships.path";
		String path = KeyValueStoreSingleton.getPath(configurationKey);
		if ( path == null || ! new File(path).isDirectory()) {
			getLog().error(configurationKey + " in the configuraiton file needs to point to a valid directory: " + path);
			System.exit(1);
		}
		getLog().info("Config: relationshipStorePath="+path);
		ListPersistor<Relationship> persistor = new ListPersistor<Relationship>(Relationship.class, new File(path));
		relationshipStore.setPersistor(persistor);
		try {
			relationshipStore.read();
		} catch (Exception e) {
			getLog().error("Failed to read relationships from " + path);
			System.exit(1);
		}

		long maxAge = KeyValueStoreSingleton.getLong("bnserver.relationships.collector.maxage", 		3600*1000	);
		long period = KeyValueStoreSingleton.getLong("bnserver.relationships.collector.period", 		60*1000	);
		
		getLog().info("Config: Relationship collector: maxAge="+maxAge);
		getLog().info("Config: Relationship collector: period="+period);

		RelationshipCollector collector = new RelationshipCollector(relationshipStore, period, maxAge);
		new Thread(collector).start();
		RelationshipStoreSingleton.setInstance(relationshipStore);
	}

	private static void initializePasswordSafe() {
		PasswordSafe passwordSafe = new PasswordSafe();
		String configurationKey = "bnserver.admin.password";
		String password = KeyValueStoreSingleton.getString(configurationKey);
		if ( password == null ) {
			getLog().warn(configurationKey + " is not set in the configuraiton file, defaulting to a random but safe value");
			password = UUID.randomUUID().toString() + UUID.randomUUID().toString(); 
		}
		passwordSafe.setAdminPassword(password);
		PasswordSafeSingleton.setInstance(passwordSafe);
	}

	private static void initializeProtocol(WampServer server)  {
		try {
			initializeProtocolWithException(server);
		}
		catch (IOException e) {
			getLog().error("Failed to open protocol file: "+e);
			System.exit(1);
		}
	}
	private static void initializeProtocolWithException(WampServer server) throws IOException {
		String path = KeyValueStoreSingleton.getPath("bnserver.protocol.path");
		if ( path == null )
			return;
		File file = new File(path);
		FileUtils.forceMkdir(file.getParentFile());
		final Protocol protocol = new Protocol(file);
		TextFrameEavesdropper incomingEavesdropper = new TextFrameEavesdropper() {
			@Override
			public void handler(String session, String frame) {
				protocol.write("WAMPIN", session, frame);
			}
		};
		server.addIncomingFramesEavesdropper(incomingEavesdropper);
	}

	private static Log log;

}
