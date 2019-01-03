package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetAllEvents extends RpcHandler {

    public RpcHandlerGetAllEvents(EventList eventsList) {
        this.eventsList = eventsList;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        rpcCall.setOutput(EventListMessage.newFromEventsList(eventsList), EventListMessage.class);
    }

    private EventList eventsList;
}
