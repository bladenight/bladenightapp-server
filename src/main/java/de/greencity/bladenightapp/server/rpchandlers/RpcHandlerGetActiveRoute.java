package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.routes.Route;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetActiveRoute extends RpcHandler {

    public RpcHandlerGetActiveRoute(Procession procession) {
        this.procession = procession;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        Route route = procession.getRoute();
        if ( route != null )
            rpcCall.setOutput(new RouteMessage(route), RouteMessage.class);
        else
            rpcCall.setError(BladenightUrl.BASE+"noSuchRoute", "No active route available");
    }

    public boolean validateInput(RpcCall rpcCall, String input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }

    private Procession procession;
}
