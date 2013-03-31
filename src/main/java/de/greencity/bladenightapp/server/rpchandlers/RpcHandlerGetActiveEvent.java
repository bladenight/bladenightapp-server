package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventMessage;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetActiveEvent extends RpcHandler {

	public RpcHandlerGetActiveEvent(EventList manager) {
		this.eventManager = manager;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		Event nextEvent = eventManager.getActiveEvent();
		rpcCall.setOutput(new EventMessage(nextEvent), EventMessage.class);
	}

	private EventList eventManager;
}
