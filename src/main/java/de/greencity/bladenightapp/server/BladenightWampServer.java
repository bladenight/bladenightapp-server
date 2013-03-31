package de.greencity.bladenightapp.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.relationships.RelationshipStoreSingleton;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetActiveEvent;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetActiveRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllEvents;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllParticipants;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllRouteNames;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerRelationship;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerSetActiveRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerUpdateParticipant;
import fr.ocroquette.wampoc.server.WampServer;

public class BladenightWampServer extends WampServer {

	public BladenightWampServer() {
		super();
		register();
	}
	
	protected void register() {
		getLog().debug("Registering RPC handlers...");
		registerRpcHandler(BladenightUrl.GET_ACTIVE_EVENT.getText(), 			new RpcHandlerGetActiveEvent(EventsListSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ALL_EVENTS.getText(), 				new RpcHandlerGetAllEvents(EventsListSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ACTIVE_ROUTE.getText(), 			new RpcHandlerGetActiveRoute(ProcessionSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ROUTE.getText(), 					new RpcHandlerGetRoute(RouteStoreSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ALL_PARTICIPANTS.getText(), 		new RpcHandlerGetAllParticipants(ProcessionSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_REALTIME_UPDATE.getText(), 		new RpcHandlerUpdateParticipant(ProcessionSingleton.getInstance(), RelationshipStoreSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.CREATE_RELATIONSHIP.getText(), 		new RpcHandlerRelationship(RelationshipStoreSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.SET_ACTIVE_ROUTE.getText(), 			new RpcHandlerSetActiveRoute(ProcessionSingleton.getInstance(), RouteStoreSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ALL_ROUTE_NAMES.getText(), 		new RpcHandlerGetAllRouteNames(RouteStoreSingleton.getInstance()));
	}
	
	private static Log log;

	public static void setLog(Log log) {
		BladenightWampServer.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(BladenightWampServer.class));
		return log;
	}
}
