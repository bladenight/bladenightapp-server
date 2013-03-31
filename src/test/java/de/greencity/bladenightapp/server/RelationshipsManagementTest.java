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

		RelationshipOutputMessage output = sendAndParse(deviceId1, null, 0);
		assertTrue(output.getRequestId() > 0);
		assertEquals(1, output.getFriendId());

		output = sendAndParse(null, deviceId2, output.getRequestId());
		assertTrue(output.getRequestId() > 0);
		assertEquals(1, output.getFriendId());

		output = sendAndParse(deviceId1, null, 0);
		assertTrue(output.getRequestId() > 0);
		assertEquals(2, output.getFriendId());

		output = sendAndParse(deviceId2, null, 0);
		assertTrue(output.getRequestId() > 0);
		assertEquals(2, output.getFriendId());
	}

	@Test
	public void openMultipleRequests() throws IOException, BadArgumentException {
		String deviceId1 = UUID.randomUUID().toString();

		RelationshipOutputMessage message1 = sendAndParse(deviceId1, null, 0);
		assertTrue(message1.getRequestId() > 0);
		assertEquals(1, message1.getFriendId());

		RelationshipOutputMessage message2 = sendAndParse(deviceId1, null, 0);
		assertTrue(message2.getRequestId() > 0);
		assertTrue(message1.getRequestId() != message2.getRequestId());
		assertEquals(2, message2.getFriendId());
	}


	@Test
	public void invalidRequest1() throws IOException, BadArgumentException {
		Message message = send(null, null, 0);
		assertTrue(message.getType() == MessageType.CALLERROR);
	}

	@Test
	public void invalidRequest2() throws IOException, BadArgumentException {
		RelationshipOutputMessage output = sendAndParse(UUID.randomUUID().toString(), null, 0);
		assertTrue(output.getRequestId() > 0);
		Message message = send(null, null, output.getRequestId());
		assertTrue(message.getType() == MessageType.CALLERROR);
	}

	@Test
	public void selfRelationship() throws IOException, BadArgumentException {
		String deviceId = UUID.randomUUID().toString();
		RelationshipOutputMessage output = sendAndParse(deviceId, null, 0);
		assertTrue(output.getRequestId() > 0);
		Message message = send(null, deviceId, output.getRequestId());
		assertTrue(message.getType() == MessageType.CALLERROR);
	}


	public long createRelationShip(String deviceId1, String deviceId2) throws IOException, BadArgumentException {
		RelationshipOutputMessage output;
		output = sendAndParse(deviceId1, null, 0);
		long friendId = output.getFriendId();
		output = sendAndParse(null, deviceId2, output.getRequestId());
		return friendId;
	}
	
	public Message send(String deviceId1, String deviceId2, long relationshipId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.CREATE_RELATIONSHIP.getText());
		RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId1, deviceId2, relationshipId);
		msg.setPayload(partnershipMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public RelationshipOutputMessage sendAndParse(String deviceId1, String deviceId2, long relationshipId) throws IOException, BadArgumentException {
		Message message = send(deviceId1, deviceId2, relationshipId);
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(RelationshipOutputMessage.class);
	}

	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
}
