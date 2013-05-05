package de.greencity.bladenightapp.server.rpchandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.SetMinimumLinearPosition;
import de.greencity.bladenightapp.security.PasswordSafe;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerKillServer extends RpcHandler {

	private PasswordSafe passwordSafe;

	public RpcHandlerKillServer(PasswordSafe passwordSafe) {
		this.passwordSafe = passwordSafe;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		SetMinimumLinearPosition msg = rpcCall.getInput(SetMinimumLinearPosition.class);
		
		if ( msg == null ) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Could not parse the input");
			return;
		}
		if ( ! msg.verify(passwordSafe.getAdminPassword(), 12*3600*1000)) {
			rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + msg.toString());
			return;
		}
		getLog().warn("Killing server on client request");
		System.exit(0);
	}
	
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerKillServer.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerKillServer.class));
		return log;
	}
}
