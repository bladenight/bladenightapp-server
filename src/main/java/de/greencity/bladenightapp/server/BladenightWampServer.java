package de.greencity.bladenightapp.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetActiveEvent;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetActiveRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllParticipants;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetRoute;
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
		registerRpcHandler(BladenightUrl.GET_ACTIVE_ROUTE.getText(), 			new RpcHandlerGetActiveRoute(ProcessionSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ROUTE.getText(), 					new RpcHandlerGetRoute(RouteStoreSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_ALL_PARTICIPANTS.getText(), 		new RpcHandlerGetAllParticipants(ProcessionSingleton.getInstance()));
		registerRpcHandler(BladenightUrl.GET_REALTIME_UPDATE.getText(), 			new RpcHandlerUpdateParticipant(ProcessionSingleton.getInstance()));
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
