package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
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
		channel = new ProtocollingChannel();
		server = new BladenightWampServer();
		session = server.openSession(channel);
	}

	@Test
	public void simpleRelationship() throws IOException, BadArgumentException {
		String deviceId1 = UUID.randomUUID().toString();
		String deviceId2 = UUID.randomUUID().toString();

		RelationshipOutputMessage output = sendAndParse(deviceId1, ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		assertEquals(friendIdCounter, output.getFriendId());

		output = sendAndParse(deviceId2, ++friendIdCounter, output.getRequestId());
		assertTrue(output.getRequestId() > 0);
		assertEquals(friendIdCounter, output.getFriendId());

		output = sendAndParse(deviceId1, ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		assertEquals(friendIdCounter, output.getFriendId());

		output = sendAndParse(deviceId2, ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		assertEquals(friendIdCounter, output.getFriendId());
	}

	@Test
	public void openMultipleRequests() throws IOException, BadArgumentException {
		String deviceId1 = UUID.randomUUID().toString();

		RelationshipOutputMessage message1 = sendAndParse(deviceId1, ++friendIdCounter, 0);
		assertTrue(message1.getRequestId() > 0);
		assertEquals(friendIdCounter, message1.getFriendId());

		RelationshipOutputMessage message2 = sendAndParse(deviceId1, ++friendIdCounter, 0);
		assertTrue(message2.getRequestId() > 0);
		assertTrue(message1.getRequestId() != message2.getRequestId());
		assertEquals(friendIdCounter, message2.getFriendId());
	}


	@Test
	public void invalidRequest1() throws IOException, BadArgumentException {
		Message message = send(null, ++friendIdCounter, 0);
		assertTrue(message.getType() == MessageType.CALLERROR);
	}

	@Test
	public void invalidRequest2() throws IOException, BadArgumentException {
		RelationshipOutputMessage output = sendAndParse(UUID.randomUUID().toString(), ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		Message message = send(null, ++friendIdCounter, output.getRequestId());
		assertTrue(message.getType() == MessageType.CALLERROR);
	}

	@Test
	public void selfRelationship() throws IOException, BadArgumentException {
		String deviceId = UUID.randomUUID().toString();
		RelationshipOutputMessage output = sendAndParse(deviceId, ++friendIdCounter, 0);
		assertTrue(output.getRequestId() > 0);
		Message message = send(deviceId, ++friendIdCounter, output.getRequestId());
		assertTrue(message.getType() == MessageType.CALLERROR);
	}


	public long createRelationShip(String deviceId1, String deviceId2) throws IOException, BadArgumentException {
		RelationshipOutputMessage output;
		output = sendAndParse(deviceId1, ++friendIdCounter, 0);
		long friendId = friendIdCounter;
		output = sendAndParse(deviceId2, ++friendIdCounter, output.getRequestId());
		return friendId;
	}
	
	public Message send(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.CREATE_RELATIONSHIP.getText());
		RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, requestId);
		msg.setPayload(partnershipMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public RelationshipOutputMessage sendAndParse(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
		Message message = send(deviceId, friendId, requestId);
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(RelationshipOutputMessage.class);
	}

	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
	static int friendIdCounter = 1;
}
