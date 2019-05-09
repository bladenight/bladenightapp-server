package de.greencity.bladenightapp.server;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.server.rpchandlers.*;
import fr.ocroquette.wampoc.server.WampServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;

public class BladenightWampServerAux extends WampServer {

    static public class Builder {

        Builder() {
            this.bladenightMainWampServer = new BladenightWampServerAux();
        }

        public Builder setProcession(Procession procession) {
            bladenightMainWampServer.procession = procession;
            return this;
        }

        public Builder setEventList(EventList eventList) {
            bladenightMainWampServer.eventList = eventList;
            return this;
        }

        public Builder setRouteStore(RouteStore routeStore) {
            bladenightMainWampServer.routeStore = routeStore;
            return this;
        }

        public BladenightWampServerAux build() {
            bladenightMainWampServer.register();
            return bladenightMainWampServer;
        }

        private BladenightWampServerAux bladenightMainWampServer;
    }

    private BladenightWampServerAux() {
    }


    void register() {
        getLog().debug("Registering RPC handlers...");
        registerRpcHandler(BladenightUrl.GET_ACTIVE_EVENT.getText(),            new RpcHandlerGetActiveEvent(eventList));
        registerRpcHandler(BladenightUrl.GET_ALL_EVENTS.getText(),              new RpcHandlerGetAllEvents(eventList));
        registerRpcHandler(BladenightUrl.GET_ACTIVE_ROUTE.getText(),            new RpcHandlerGetActiveRoute(procession));
        registerRpcHandler(BladenightUrl.GET_ROUTE.getText(),                   new RpcHandlerGetRoute(routeStore));
        registerRpcHandler(BladenightUrl.GET_REALTIME_UPDATE.getText(),         new RpcHandlerGetRealtimeUpdate(procession, Optional.empty(), false));
        registerRpcHandler(BladenightUrl.GET_ALL_ROUTE_NAMES.getText(),         new RpcHandlerGetAllRouteNames(routeStore));
    }

    private Procession procession;
    private EventList eventList;
    private RouteStore routeStore;


    private static Log log;

    public static void setLog(Log log) {
        BladenightWampServerAux.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(BladenightWampServerAux.class));
        return log;
    }
}
