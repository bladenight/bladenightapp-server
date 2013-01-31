package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetRoute extends RpcHandler {

	public RpcHandlerGetRoute(RouteStore routeStore) {
		this.routeStore = routeStore;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		String input = rpcCall.getInput(String.class);
		if ( ! validateInput(rpcCall, input) )
			return;
		Route route = routeStore.getRoute(input);
		if ( route != null )
			rpcCall.setOutput(new RouteMessage(route), RouteMessage.class);
		else
			rpcCall.setError(BladenightUrl.BASE+"noSuchRoute", "Could not load route named "+ input);
	}
	
	public boolean validateInput(RpcCall rpcCall, String input) {
		if ( input == null ) {
			rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
			return false;
		}
		return true;
	}

	private RouteStore routeStore;
}
