package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.server.Session;

public class BladenightWampServerTest {
	@Test
	public void test() throws IOException, BadArgumentException {
		Procession procession = new Procession();
		ProcessionSingleton.setProcession(procession);
		
		BladenightWampServer server = new BladenightWampServer();
		ProtocollingChannel channel = new ProtocollingChannel();
		Session session = server.openSession(channel);
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.PARTICIPANT_UPDATE.getText());
		msg.setPayload(new GpsInfo("test", 0, 0), GpsInfo.class);
		server.handleIncomingMessage(session, msg);
		assertEquals(2, channel.handledMessages.size());
	}
}
