package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.AdminMessage;
import de.greencity.bladenightapp.security.PasswordSafe;
import de.greencity.bladenightapp.security.PasswordSafeSingleton;
import de.greencity.bladenightapp.testutils.LogHelper;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class VerifyAdminPasswordTest {

	@Before
	public void init() throws IOException {
		LogHelper.disableLogs();

		passwordSafe = new PasswordSafe();
		passwordSafe.setAdminPassword(password);
		PasswordSafeSingleton.setInstance(passwordSafe);
		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	@Test
	public void verifyPassword() throws IOException, BadArgumentException {
		assertTrue(verifyAgainstServer(password));
		assertTrue(! verifyAgainstServer("invalid password"));
	}

	private boolean verifyAgainstServer(String password) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.VERIFY_ADMIN_PASSWORD.getText());
		AdminMessage adminMessage = new AdminMessage();
		adminMessage.authenticate(password);
		msg.setPayload(adminMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message returnedMessage = MessageMapper.fromJson(channel.lastMessage());
		return MessageType.CALLRESULT.equals(returnedMessage.getType()); 
	}

	private PasswordSafe passwordSafe;
	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
	final static String password = UUID.randomUUID().toString();
}
