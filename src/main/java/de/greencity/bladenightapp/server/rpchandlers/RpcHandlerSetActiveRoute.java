package de.greencity.bladenightapp.server.rpchandlers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerSetActiveRoute extends RpcHandler {

	public RpcHandlerSetActiveRoute(EventList eventList, Procession procession, RouteStore routeStore) {
		this.procession = procession;
		this.routeStore = routeStore;
		this.eventList = eventList;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		String newRouteName = rpcCall.getInput(String.class);
		if (newRouteName == null) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Input route name = " + newRouteName);
			return;
		}
		Route newRoute = routeStore.getRoute(newRouteName);
		if (newRoute == null) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Unknown route: " + newRouteName);
			return;
		}

		procession.setRoute(newRoute);
		eventList.setActiveRoute(newRouteName);
		try {
			eventList.writeToDir();
		} catch (IOException e) {
			getLog().error("Failed to save events: " + e);
		}
	}
	
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerSetActiveRoute.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerSetActiveRoute.class));
		return log;
	}
	
	private Procession procession;
	private RouteStore routeStore;
	private EventList eventList;
}
