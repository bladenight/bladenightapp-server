package de.greencity.bladenightapp.server.rpchandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.HandshakeClientMessage;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerHandshake extends RpcHandler {

	public RpcHandlerHandshake(int minClientBuild) {
		this.minClientBuild = minClientBuild;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		HandshakeClientMessage input = rpcCall.getInput(HandshakeClientMessage.class);

		if ( ! validateInput(rpcCall, input) )
			return;

		getLog().info("minClientBuild="+minClientBuild);
		getLog().info("clientbuild="+input.getClientBuildNumber());
		if ( minClientBuild > 0 && input.getClientBuildNumber() > 0  ) {
			if ( input.getClientBuildNumber() < minClientBuild ) {
				rpcCall.setError(BladenightError.OUTDATED_CLIENT.getText(), "Please update your client to version " + minClientBuild + " or greater");
				return;
			}
				
		}
	}

	public boolean validateInput(RpcCall rpcCall, HandshakeClientMessage input) {
		if ( input == null ) {
			rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
			return false;
		}
		return true;
	}

	private int minClientBuild;
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerHandshake.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerHandshake.class));
		return log;
	}
}
