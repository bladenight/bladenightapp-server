package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.impl.NoOpLog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.greencity.bladenightapp.events.EventsList;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.NetMovingPoint;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
import de.greencity.bladenightapp.procession.HeadAndTailComputer;
import de.greencity.bladenightapp.procession.ParticipantUpdater;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.procession.TravelTimeComputer;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.relationships.RelationshipStoreSingleton;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.server.rpchandlers.RpcHandlerRelationship;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class RelationshipsLocalizationTest {
	final String routeName = "Nord - kurz";
	final String path = "/routes/" + routeName + ".kml";

	@BeforeClass
	public static void beforeClass() {
		BladenightWampServer.setLog(new NoOpLog());
		RelationshipStore.setLog(new NoOpLog());
		RpcHandlerRelationship.setLog(new NoOpLog());
		Route.setLog(new NoOpLog());
		Procession.setLog(new NoOpLog());
		HeadAndTailComputer.setLog(new NoOpLog());
		ParticipantUpdater.setLog(new NoOpLog());
		TravelTimeComputer.setLog(new NoOpLog());
	}

	@Before
	public void before() {
		Route.setLog(new NoOpLog());
		File file = FileUtils.toFile(EventsList.class.getResource(path));
		assertTrue(file != null);
		route = new Route();
		assertTrue(route.load(file));
		assertEquals(routeName, route.getName());

		procession = new Procession();
		procession.setRoute(route);
		procession.setMaxComputeAge(0);
		ProcessionSingleton.setProcession(procession);

		RelationshipStoreSingleton.setInstance(new RelationshipStore());
		channel = new ProtocollingChannel();
		server = new BladenightWampServer();
		session = server.openSession(channel);
	}


	@Test
	public void test() throws IOException, BadArgumentException {
		String deviceId1 = "user-1";
		String deviceId2 = "user-2";
		String deviceId3 = "user-3";

		createRelationShip(deviceId1, deviceId2);
		createRelationShip(deviceId1, deviceId3);

		RealTimeUpdateData data2 = getRealtimeUpdateFromParticipant(deviceId2, 48.143655, 11.548839);
		assertEquals(1756, data2.getUserPosition(), 1.0);
		assertEquals(true, data2.isUserOnRoute());

		RealTimeUpdateData data3 = getRealtimeUpdateFromParticipant(deviceId3, 48.143755, 11.548839);
		assertEquals(1766, data3.getUserPosition(), 1.0);
		assertEquals(true, data3.isUserOnRoute());

		RealTimeUpdateData data1 = getRealtimeUpdateFromParticipant(deviceId1, 48.139341, 11.547129);
		assertEquals(1241, data1.getUserPosition(), 1.0);
		assertEquals(true, data1.isUserOnRoute());
		assertTrue(data1.getFriendsMap() != null);

		NetMovingPoint friend1 = data1.getFriendsMap().get(new Long(1)); 
		assertTrue(friend1 != null);
		assertEquals(data2.getUserPosition(), friend1.getPosition(), 1.0);

		NetMovingPoint friend2 = data1.getFriendsMap().get(new Long(2)); 
		assertTrue(friend2 != null);
		assertEquals(data3.getUserPosition(), friend2.getPosition(), 1.0);
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
		msg.setPayload(partnershipMessage, RelationshipInputMessage.class);
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

	RealTimeUpdateData getRealtimeUpdateFromParticipant(String clientId, double lat, double lon) throws IOException, BadArgumentException {
		return getRealtimeUpdate(new GpsInfo(clientId, true, lat, lon));
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


	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
	private Route route;
	private Procession procession;

}
