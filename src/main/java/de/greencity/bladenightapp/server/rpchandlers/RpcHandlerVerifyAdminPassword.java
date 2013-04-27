package de.greencity.bladenightapp.server.rpchandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.AdminMessage;
import de.greencity.bladenightapp.security.PasswordSafe;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerVerifyAdminPassword extends RpcHandler {

	public RpcHandlerVerifyAdminPassword(PasswordSafe passwordSafe) {
		this.passwordSafe = passwordSafe;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		AdminMessage message = rpcCall.getInput(AdminMessage.class);
		// TODO put maxAge in the configuration file
		if ( ! message.verify(passwordSafe.getAdminPassword(), 12*3600*1000) ) {
			rpcCall.setError(BladenightError.INVALID_PASSWORD.getText(), "Verification for admin message failed: " + message.toString());
			return;
		}
		rpcCall.setOutput("OK", String.class);
	}
	
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerVerifyAdminPassword.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerVerifyAdminPassword.class));
		return log;
	}
	
	private PasswordSafe passwordSafe;

}
