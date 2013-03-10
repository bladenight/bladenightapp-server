package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.events.EventsList;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetAllEvents extends RpcHandler {

	public RpcHandlerGetAllEvents(EventsList eventsList) {
		this.eventsList = eventsList;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		rpcCall.setOutput(EventsListMessage.newFromEventsList(eventsList), EventsListMessage.class);
	}

	private EventsList eventsList;
}
