package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.network.messages.RouteNamesMessage;
import de.greencity.bladenightapp.routes.RouteStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetAllRouteNames extends RpcHandler {

	public RpcHandlerGetAllRouteNames(RouteStore routeStore) {
		this.routeStore = routeStore;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		rpcCall.setOutput(RouteNamesMessage.newFromRouteNameList(routeStore.getAvailableRoutes()), RouteNamesMessage.class);
	}

	private RouteStore routeStore;
}
