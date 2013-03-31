package de.greencity.bladenightapp.server.rpchandlers;

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
	}
	
	private Procession procession;
	private RouteStore routeStore;
	private EventList eventList;
}
