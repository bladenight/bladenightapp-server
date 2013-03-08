package de.greencity.bladenightapp.server.rpchandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
import de.greencity.bladenightapp.relationships.HandshakeInfo;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerRelationship extends RpcHandler {

	public RpcHandlerRelationship(RelationshipStore relationshipStore) {
		this.relationshipStore = relationshipStore;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		RelationshipInputMessage input = rpcCall.getInput(RelationshipInputMessage.class);

		if ( ! validateInput(rpcCall, input) )
			return;
		
		if ( relationshipStore == null ) {
			rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Internal error: relationshipStore is null");
			return;
		}
		
		if ( handleNewRequest(rpcCall, input) )
			return;

		if ( handleRequestFinalization(rpcCall, input) )
			return;
		
		rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Protocol error", "Neither new nor existing request");
	}
	
	public boolean handleNewRequest(RpcCall rpcCall, RelationshipInputMessage input) {
		if ( input.getDeviceId1() == null || input.getDeviceId1().length() == 0 )
			return false;

		if ( input.getDeviceId2() != null || input.getRequestId() > 0 )
			return false;

		HandshakeInfo handshakeInfo = relationshipStore.newRequest(input.getDeviceId1());
		
		rpcCall.setOutput(new RelationshipOutputMessage(handshakeInfo.getRequestId(), handshakeInfo.getFriendId()), RelationshipOutputMessage.class);
		
		return true;
	}

	private boolean handleRequestFinalization(RpcCall rpcCall, RelationshipInputMessage input) {
		if ( input.getDeviceId2() == null || input.getDeviceId2().length() == 0 )
			return false;

		if ( input.getRequestId() <= 0 )
			return false;

		if ( input.getDeviceId1() != null )
			return false;

		HandshakeInfo handshakeInfo = new HandshakeInfo();
		try {
			handshakeInfo = relationshipStore.finalize(input.getRequestId(), input.getDeviceId2());
		} catch (Exception e) {
			getLog().error("Failed to finalize relationship: ", e);
			return false;
		}

		rpcCall.setOutput(new RelationshipOutputMessage(input.getRequestId(), handshakeInfo.getFriendId()), RelationshipOutputMessage.class);

		return true;
	}


	public boolean validateInput(RpcCall rpcCall, RelationshipInputMessage input) {
		if ( input == null ) {
			rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
			return false;
		}
		return true;
	}


	private RelationshipStore relationshipStore;
	
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerRelationship.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerRelationship.class));
		return log;
	}
}
