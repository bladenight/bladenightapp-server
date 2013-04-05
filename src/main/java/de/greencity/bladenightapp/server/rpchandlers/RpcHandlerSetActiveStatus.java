package de.greencity.bladenightapp.server.rpchandlers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerSetActiveStatus extends RpcHandler {

	public RpcHandlerSetActiveStatus(EventList eventList) {
		this.eventList = eventList;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		EventStatus newStatus = rpcCall.getInput(EventStatus.class);
		eventList.setActiveStatus(EventMessage.convertStatus(newStatus));
		try {
			eventList.write();
		}
		catch(IOException e) {
			getLog().error("Failed to write to dir: " + e);
		}
	}
	
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerSetActiveStatus.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerSetActiveStatus.class));
		return log;
	}

	private EventList eventList;
}
