package de.greencity.bladenightapp.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.security.PasswordSafe;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerCreateRelationship;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerDeleteRelationship;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetActiveEvent;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetActiveRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllEvents;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllParticipants;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetAllRouteNames;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetFriends;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetRealtimeUpdate;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerGetRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerHandshake;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerKillServer;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerSetActiveRoute;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerSetActiveStatus;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerSetMinimumLinearPosition;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerVerifyAdminPassword;
import fr.ocroquette.wampoc.server.WampServer;

public class BladenightWampServer extends WampServer {

    public RelationshipStore relationshipStore;

    static public class ServerBuilder {

        ServerBuilder() {
            this.bladenightWampServer = new BladenightWampServer();
        }

        public ServerBuilder setMinimumClientBuildNumber(int minClientBuildNumber) {
            bladenightWampServer.minClientBuildNumber = minClientBuildNumber;
            return this;
        }

        public ServerBuilder setProcession(Procession procession) {
            bladenightWampServer.procession = procession;
            return this;
        }

        public ServerBuilder setEventList(EventList eventList) {
            bladenightWampServer.eventList = eventList;
            return this;
        }

        public ServerBuilder setRouteStore(RouteStore routeStore) {
            bladenightWampServer.routeStore = routeStore;
            return this;
        }

        public ServerBuilder setPasswordSafe(PasswordSafe passwordSafe) {
            bladenightWampServer.passwordSafe = passwordSafe;
            return this;
        }

        public ServerBuilder setRelationshipStore(RelationshipStore relationshipStore) {
            bladenightWampServer.relationshipStore = relationshipStore;
            return this;
        }

        public BladenightWampServer build() {
            bladenightWampServer.register();
            return bladenightWampServer;
        }


        private BladenightWampServer bladenightWampServer;
    }

    private BladenightWampServer() {
    }


    public void setMinimumClientBuildNumber(int minClientBuildNumber) {
        this.minClientBuildNumber = minClientBuildNumber;
    }

    void register() {
        getLog().debug("Registering RPC handlers...");
        registerRpcHandler(BladenightUrl.GET_ACTIVE_EVENT.getText(),            new RpcHandlerGetActiveEvent(eventList));
        registerRpcHandler(BladenightUrl.GET_ALL_EVENTS.getText(),              new RpcHandlerGetAllEvents(eventList));
        registerRpcHandler(BladenightUrl.GET_ACTIVE_ROUTE.getText(),            new RpcHandlerGetActiveRoute(procession));
        registerRpcHandler(BladenightUrl.GET_ROUTE.getText(),                   new RpcHandlerGetRoute(routeStore));
        registerRpcHandler(BladenightUrl.GET_ALL_PARTICIPANTS.getText(),        new RpcHandlerGetAllParticipants(procession));
        registerRpcHandler(BladenightUrl.GET_REALTIME_UPDATE.getText(),         new RpcHandlerGetRealtimeUpdate(procession, relationshipStore));
        registerRpcHandler(BladenightUrl.CREATE_RELATIONSHIP.getText(),         new RpcHandlerCreateRelationship(relationshipStore));
        registerRpcHandler(BladenightUrl.SET_ACTIVE_ROUTE.getText(),            new RpcHandlerSetActiveRoute(eventList, procession, routeStore, passwordSafe));
        registerRpcHandler(BladenightUrl.SET_ACTIVE_STATUS.getText(),           new RpcHandlerSetActiveStatus(eventList, passwordSafe));
        registerRpcHandler(BladenightUrl.GET_ALL_ROUTE_NAMES.getText(),         new RpcHandlerGetAllRouteNames(routeStore));
        registerRpcHandler(BladenightUrl.VERIFY_ADMIN_PASSWORD.getText(),       new RpcHandlerVerifyAdminPassword(passwordSafe));
        registerRpcHandler(BladenightUrl.GET_FRIENDS.getText(),                 new RpcHandlerGetFriends(relationshipStore, procession));
        registerRpcHandler(BladenightUrl.DELETE_RELATIONSHIP.getText(),         new RpcHandlerDeleteRelationship(relationshipStore));
        registerRpcHandler(BladenightUrl.SET_MIN_POSITION.getText(),            new RpcHandlerSetMinimumLinearPosition(passwordSafe));
        registerRpcHandler(BladenightUrl.KILL_SERVER.getText(),                 new RpcHandlerKillServer(passwordSafe));
        registerRpcHandler(BladenightUrl.SHAKE_HANDS.getText(),                 new RpcHandlerHandshake(minClientBuildNumber));
    }

    private int minClientBuildNumber;
    private Procession procession;
    private EventList eventList;
    private RouteStore routeStore;
    private PasswordSafe passwordSafe;


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
