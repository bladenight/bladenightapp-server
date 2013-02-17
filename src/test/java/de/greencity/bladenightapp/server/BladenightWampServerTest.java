package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.impl.NoOpLog;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.events.EventsList;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class BladenightWampServerTest {
	final String path = "/routes/Nord - kurz.kml";

	@Before
	public void init() {
		Route.setLog(new NoOpLog());
		File file = FileUtils.toFile(EventsList.class.getResource(path));
		assertTrue(file != null);
		route = new Route();
		assertTrue(route.load(file));

		procession = new Procession();
		procession.setRoute(route);
		ProcessionSingleton.setProcession(procession);
		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	
	@Test
	public void userOutOfCorridor() throws IOException, BadArgumentException {
		RealTimeUpdateData data = sendParticipantUpdate(0, 0);
		System.out.println("xxx="+channel.handledMessages.get(1));
		assertTrue(data != null);
		assertEquals(12605, data.getRouteLength(), 1.0);
	}

	@Test
	public void userInCorridor() throws IOException, BadArgumentException {
		RealTimeUpdateData data = sendParticipantUpdate(48.139341, 11.547129);
		System.out.println("xxx="+channel.handledMessages.get(1));
		assertTrue(data != null);
		assertEquals(1241, data.getUserPosition().getPosition(), 1.0);
	}

	RealTimeUpdateData sendParticipantUpdate(double lat, double lon) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.PARTICIPANT_UPDATE.getText());
		msg.setPayload(new GpsInfo("test", lat, lon), GpsInfo.class);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message message = MessageMapper.fromJson(channel.handledMessages.get(1));
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(RealTimeUpdateData.class);
	}
	
	private Route route;
	private Procession procession;
	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
}
