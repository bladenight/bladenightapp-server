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

		File file = FileUtils.toFile(BladenightWampServerTest.class.getResource(path));
		assertTrue(file != null);
		route = new Route();
		assertTrue(route.load(file));
		assertEquals(routeName, route.getName());

		procession = new Procession();
		procession.setRoute(route);
		procession.setMaxComputeAge(0);
		ProcessionSingleton.setProcession(procession);

		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	@Test
	public void basicUnlocalizedObserver() throws IOException, BadArgumentException {
		RealTimeUpdateData data = getRealtimeUpdateFromUnlocalizedObserver();
		assertTrue(data != null);
		assertEquals(12605, data.getRouteLength(), 1.0);
		assertEquals(routeName, data.getRouteName());
	}

	@Test
	public void basicLocalizedObserver() throws IOException, BadArgumentException {
		RealTimeUpdateData data = getRealtimeUpdateFromLocalizedObserver("userInCorridor", 48.139341, 11.547129);
		assertTrue(data != null);
		assertEquals(12605, data.getRouteLength(), 1.0);
		assertEquals(routeName, data.getRouteName());
		assertEquals(true, data.isUserOnRoute());
	}


	@Test
	public void routeNameAndLength() throws IOException, BadArgumentException {
		RealTimeUpdateData data = getRealtimeUpdateFromParticipant("userOutOfCorridor", 0, 0);
		assertTrue(data != null);
		assertEquals(12605, data.getRouteLength(), 1.0);
		assertEquals(routeName, data.getRouteName());
	}
	
	@Test
	public void userOutOfCorridor() throws IOException, BadArgumentException {
		RealTimeUpdateData data = getRealtimeUpdateFromParticipant("userOutOfCorridor", 0, 0);
		assertTrue(data != null);
		assertEquals(false, data.isUserOnRoute());
	}

	@Test
	public void userOnRoute() throws IOException, BadArgumentException {
		RealTimeUpdateData data = getRealtimeUpdateFromParticipant("userInCorridor", 48.139341, 11.547129);
		assertTrue(data != null);
		assertEquals(1241, data.getUserPosition(), 1.0);
		assertEquals(true, data.isUserOnRoute());
		// assertEquals(0.0, data.getHead().getPosition(), 0.0);
		assertTrue(data.getHead().getPosition() > 0.0);
		assertTrue(data.getTail().getPosition() > 0.0);
	}

	@Test
	public void userSpeed() throws IOException, BadArgumentException {
		RealTimeUpdateData data1 = getRealtimeUpdateFromParticipant("movingUser", 48.139341, 11.547129);
		assertTrue(data1 != null);
		assertEquals(0.0, data1.getUserSpeed(), 1.0);
		RealTimeUpdateData data2 = getRealtimeUpdateFromParticipant("movingUser", 48.143655, 11.548839);
		assertTrue(data2 != null);
		assertTrue(data2.getUserSpeed() > 0.0);
	}


	@Test
	public void userCounts() throws IOException, BadArgumentException {
		RealTimeUpdateData data1 = getRealtimeUpdateFromParticipant("client-1", 48.139341, 11.547129);
		assertEquals(1, data1.getUserOnRoute());
		assertEquals(1, data1.getUserTotal());
		RealTimeUpdateData data2 = getRealtimeUpdateFromParticipant("client-2", 0, 0);
		assertEquals(1, data2.getUserOnRoute());
		assertEquals(2, data2.getUserTotal());
		RealTimeUpdateData data3 = getRealtimeUpdateFromUnlocalizedObserver();
		assertEquals(1, data3.getUserOnRoute());
		assertEquals(2, data3.getUserTotal());
	}
	
	RealTimeUpdateData getRealtimeUpdateFromParticipant(String clientId, double lat, double lon) throws IOException, BadArgumentException {
		return getRealtimeUpdate(new GpsInfo(clientId, true, lat, lon));
	}

	RealTimeUpdateData getRealtimeUpdateFromLocalizedObserver(String clientId, double lat, double lon) throws IOException, BadArgumentException {
		return getRealtimeUpdate(new GpsInfo(clientId, false, lat, lon));
	}

	RealTimeUpdateData getRealtimeUpdateFromUnlocalizedObserver() throws IOException, BadArgumentException {
		return getRealtimeUpdate(null);
	}

	RealTimeUpdateData getRealtimeUpdate(GpsInfo gpsInfo) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_REALTIME_UPDATE.getText());
		msg.setPayload(gpsInfo, GpsInfo.class);
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
