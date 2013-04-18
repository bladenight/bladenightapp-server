package de.greencity.bladenightapp.server.rpchandlers;

import java.util.List;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.FriendMessage;
import de.greencity.bladenightapp.network.messages.FriendsMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.relationships.RelationshipMember;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetFriends extends RpcHandler {
	public RpcHandlerGetFriends(RelationshipStore relationshipStore, Procession procession) {
		this.relationshipStore = relationshipStore;
		this.procession = procession;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		String deviceId = rpcCall.getInput(String.class);
		if ( deviceId == null || deviceId.length() == 0 ) {
			rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid deviceId: " + deviceId);
			return;
		}
		List<RelationshipMember> relationshipMembers = relationshipStore.getAllRelationships(deviceId);
		FriendsMessage friends = new FriendsMessage();
		for ( RelationshipMember member : relationshipMembers) {
			FriendMessage friend = new FriendMessage();
			friend.setFriendId(member.getFriendId());
			friend.setRequestId(member.getRequestId());
			friends.put(friend.getFriendId(),friend);
			if ( member.getDeviceId() != null)
				friend.isOnline(procession.getParticipant(member.getDeviceId()) != null);
		}
		rpcCall.setOutput(friends, FriendsMessage.class);
	}

	private RelationshipStore relationshipStore;
	private Procession procession;
	
}
