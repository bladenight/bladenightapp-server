package de.greencity.bladenightapp.server.rpchandlers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import de.greencity.bladenightapp.network.messages.SetActiveStatusMessage;
import de.greencity.bladenightapp.security.PasswordSafe;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerSetActiveStatus extends RpcHandler {

	public RpcHandlerSetActiveStatus(EventList eventList, PasswordSafe passwordSafe) {
		this.eventList = eventList;
		this.passwordSafe = passwordSafe;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		SetActiveStatusMessage msg = rpcCall.getInput(SetActiveStatusMessage.class);
		if ( msg == null ) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
			return;
		}
		if ( ! msg.verify(passwordSafe.getAdminPassword(), 10000)) {
			rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Invalid password");
			return;
		}
		EventStatus newStatus = msg.getStatus();
		if (newStatus == null) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid status");
			return;
		}
		
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
	private PasswordSafe passwordSafe;

}
