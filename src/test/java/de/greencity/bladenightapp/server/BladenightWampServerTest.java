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
	final String routeName = "Nord - kurz";
	final String path = "/routes/" + routeName + ".kml";

	@Before
	public void init() {
		Route.setLog(new NoOpLog());
		File file = FileUtils.toFile(EventsList.class.getResource(path));
		assertTrue(file != null);
		route = new Route();
		assertTrue(route.load(file));
		assertEquals(routeName, route.getName());

		procession = new Procession();
		procession.setRoute(route);
		ProcessionSingleton.setProcession(procession);
		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	@Test
	public void clientOnlyObserving() throws IOException, BadArgumentException {
		RealTimeUpdateData data = getRealTimeData();
		assertTrue(data != null);
		assertEquals(12605, data.getRouteLength(), 1.0);
		assertEquals(routeName, data.getRouteName());
	}

	@Test
	public void routeNameAndLength() throws IOException, BadArgumentException {
		RealTimeUpdateData data = sendParticipantUpdate("userOutOfCorridor", 0, 0);
		assertTrue(data != null);
		assertEquals(12605, data.getRouteLength(), 1.0);
		assertEquals(routeName, data.getRouteName());
	}
	
	@Test
	public void userOutOfCorridor() throws IOException, BadArgumentException {
		RealTimeUpdateData data = sendParticipantUpdate("userOutOfCorridor", 0, 0);
		assertTrue(data != null);
		assertEquals(false, data.isUserOnRoute());
	}

	@Test
	public void userOnRoute() throws IOException, BadArgumentException {
		RealTimeUpdateData data = sendParticipantUpdate("userInCorridor", 48.139341, 11.547129);
		assertTrue(data != null);
		assertEquals(1241, data.getUserPosition().getPosition(), 1.0);
		assertEquals(true, data.isUserOnRoute());
	}

	@Test
	public void userSpeed() throws IOException, BadArgumentException {
		RealTimeUpdateData data1 = sendParticipantUpdate("movingUser", 48.139341, 11.547129);
		assertTrue(data1 != null);
		assertEquals(0.0, data1.getUserPosition().getSpeed(), 1.0);
		RealTimeUpdateData data2 = sendParticipantUpdate("movingUser", 48.143655, 11.548839);
		assertTrue(data2 != null);
		assertTrue(data2.getUserPosition().getSpeed() > 0.0);
	}

	@Test
	public void userCounts() throws IOException, BadArgumentException {
		RealTimeUpdateData data1 = sendParticipantUpdate("client-1", 48.139341, 11.547129);
		assertEquals(1, data1.getUserOnRoute());
		assertEquals(1, data1.getUserTotal());
		RealTimeUpdateData data2 = sendParticipantUpdate("client-2", 0, 0);
		assertEquals(1, data2.getUserOnRoute());
		assertEquals(2, data2.getUserTotal());
		RealTimeUpdateData data3 = getRealTimeData();
		assertEquals(1, data3.getUserOnRoute());
		assertEquals(2, data3.getUserTotal());
	}
	
	RealTimeUpdateData getRealTimeData() throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_REAL_TIME_UPDATE_DATA.getText());
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message message = MessageMapper.fromJson(channel.lastMessage());
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(RealTimeUpdateData.class);
	}

	RealTimeUpdateData sendParticipantUpdate(String clientId, double lat, double lon) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.PARTICIPANT_UPDATE.getText());
		msg.setPayload(new GpsInfo(clientId, lat, lon), GpsInfo.class);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message message = MessageMapper.fromJson(channel.lastMessage());
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
