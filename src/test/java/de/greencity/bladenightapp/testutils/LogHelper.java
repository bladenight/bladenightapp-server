package de.greencity.bladenightapp.testutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.procession.HeadAndTailComputer;
import de.greencity.bladenightapp.procession.ParticipantUpdater;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.TravelTimeComputer;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.server.BladenightWampServer;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerCreateRelationship;

public class LogHelper {

    public static void disableLogs() {
        Log log = new NoOpLog();
        BladenightWampServer.setLog(log);
        RelationshipStore.setLog(log);
        RpcHandlerCreateRelationship.setLog(log);
        Route.setLog(log);
        RouteStore.setLog(log);
        Procession.setLog(log);
        HeadAndTailComputer.setLog(log);
        ParticipantUpdater.setLog(log);
        TravelTimeComputer.setLog(log);
        EventList.setLog(log);
    }

}
