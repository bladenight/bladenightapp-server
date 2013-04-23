package de.greencity.bladenightapp.server.rpchandlers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.SetActiveRouteMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.security.PasswordSafe;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerSetActiveRoute extends RpcHandler {

	private PasswordSafe passwordSafe;

	public RpcHandlerSetActiveRoute(EventList eventList, Procession procession, RouteStore routeStore, PasswordSafe passwordSafe) {
		this.procession = procession;
		this.routeStore = routeStore;
		this.eventList = eventList;
		this.passwordSafe = passwordSafe;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		SetActiveRouteMessage msg = rpcCall.getInput(SetActiveRouteMessage.class);
		
		if ( msg == null ) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
			return;
		}
		if ( ! msg.verify(passwordSafe.getAdminPassword(), 3600*1000)) {
			rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Invalid password");
			return;
		}
		String newRouteName = msg.getRouteName();
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
			eventList.write();
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
