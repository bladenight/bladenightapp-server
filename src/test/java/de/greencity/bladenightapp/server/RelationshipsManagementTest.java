package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.FriendsMessage;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.relationships.RelationshipStoreSingleton;
import de.greencity.bladenightapp.testutils.LogHelper;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class RelationshipsManagementTest {

	@BeforeClass
	public static void beforeClass() {
		LogHelper.disableLogs();
	}

	@Before
	public void before() {
		RelationshipStoreSingleton.setInstance(new RelationshipStore());
		ProcessionSingleton.setProcession(new Procession());
		channel = new ProtocollingChannel();
		server = new BladenightWampServer();
		session = server.openSession(channel);
	}

	@Test
	public void simpleRelationship() throws IOException, BadArgumentException {
		String deviceId1 = UUID.randomUUID().toString();
		String deviceId2 = UUID.randomUUID().toString();

		// Create request from deviceId1
		RelationshipOutputMessage output = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
		final int friendId1 = friendIdCounter;
		assertTrue(output.getRequestId() > 0);
		assertEquals(friendIdCounter, output.getFriendId());

		// Make sure the relationship is pending, with the right data
		FriendsMessage friends = getAndParseFriends(deviceId1);
		assertTrue(friends.get(friendId1) != null);
		assertTrue(friends.get(friendId1).isRelationshipPending());
		assertEquals(output.getRequestId(), friends.get(friendId1).getRequestId());

		// Accept the request from deviceId2
		output = sendAndParseRequest(deviceId2, ++friendIdCounter, output.getRequestId());
		int friendId2 = friendIdCounter;
		assertTrue(output.getRequestId() > 0);
		assertEquals(friendIdCounter, output.getFriendId());

		// From deviceId1's perspective: Make sure the relationship is not pending anymore, and has the right data
		friends = getAndParseFriends(deviceId1);
		assertTrue(friends.get(friendId1) != null);
		assertTrue(! friends.get(friendId1).isRelationshipPending());
		// The request ID shall have been reset:
		assertEquals(0, friends.get(friendId1).getRequestId());

		// Same from from deviceId2's perspective
		friends = getAndParseFriends(deviceId1);
		assertTrue(friends.get(friendId1) != null);
		assertTrue(! friends.get(friendId1).isRelationshipPending());
		// The request ID shall have been reset:
		assertEquals(0, friends.get(friendId1).getRequestId());

		// Request deletion of the relation ship from deviceId1:
		sendDeletionAndCheckForSuccess(deviceId1, friendId1);

		// From deviceId1's perspective: make sure the relationship is deleted 
		friends = getAndParseFriends(deviceId1);
		assertTrue(friends.get(friendId1) == null);
		
		// From deviceId2's perspective: make sure the relationship is deleted 
		friends = getAndParseFriends(deviceId2);
		assertTrue(friends.get(friendId2) == null);
	}

	@Test
	public void openMultipleRequests() throws IOException, BadArgumentException {
		String deviceId1 = UUID.randomUUID().toString();

		RelationshipOutputMessage message1 = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
		assertTrue(message1.getRequestId() > 0);
		assertEquals(friendIdCounter, message1.getFriendId());

		RelationshipOutputMessage message2 = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
		assertTrue(message2.getRequestId() > 0);
		assertTrue(message1.getRequestId() != message2.getRequestId());
		assertEquals(friendIdCounter, message2.getFriendId());
	}


	@Test
	public void invalidRequest1() throws IOException, BadArgumentException {
		Message message = sendRequest(null, ++friendIdCounter, 0);
		assertTrue(message.getType() == MessageType.CALLERROR);
	}

	@Test
	public void invalidRequest2() throws IOException, BadArgumentException {
		RelationshipOutputMessage output = sendAndParseRequest(UUID.randomUUID().toString(), ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		Message message = sendRequest(null, ++friendIdCounter, output.getRequestId());
		assertTrue(message.getType() == MessageType.CALLERROR);
	}

	@Test
	public void selfRelationship() throws IOException, BadArgumentException {
		String deviceId = UUID.randomUUID().toString();
		RelationshipOutputMessage output = sendAndParseRequest(deviceId, ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		Message message = sendRequest(deviceId, ++friendIdCounter, output.getRequestId());
		assertTrue(message.getType() == MessageType.CALLERROR);
	}


	public long createRelationShip(String deviceId1, String deviceId2) throws IOException, BadArgumentException {
		RelationshipOutputMessage output;
		output = sendAndParseRequest(deviceId1, ++friendIdCounter, 0);
		long friendId = friendIdCounter;
		output = sendAndParseRequest(deviceId2, ++friendIdCounter, output.getRequestId());
		return friendId;
	}

	public Message sendRequest(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.CREATE_RELATIONSHIP.getText());
		RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, requestId);
		msg.setPayload(partnershipMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public Message sendDeletion(String deviceId, int friendId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.DELETE_RELATIONSHIP.getText());
		RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, 0);
		msg.setPayload(partnershipMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public void sendDeletionAndCheckForSuccess(String deviceId, int friendId) throws IOException, BadArgumentException  {
		Message message = sendDeletion(deviceId, friendId);
		assertTrue(message.getType() == MessageType.CALLRESULT);
	}

	public void sendDeletionAndCheckForFailure(String deviceId, int friendId) throws IOException, BadArgumentException  {
		Message message = sendDeletion(deviceId, friendId);
		assertTrue(message.getType() == MessageType.CALLERROR);
	}


	public RelationshipOutputMessage sendAndParseRequest(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
		Message message = sendRequest(deviceId, friendId, requestId);
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(RelationshipOutputMessage.class);
	}

	public FriendsMessage getAndParseFriends(String deviceId) throws IOException, BadArgumentException {
		Message message = getFriends(deviceId);
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(FriendsMessage.class);
	}


	public Message getFriends(String deviceId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_FRIENDS.getText());
		msg.setPayload(deviceId);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
	static int friendIdCounter = 1;
}
