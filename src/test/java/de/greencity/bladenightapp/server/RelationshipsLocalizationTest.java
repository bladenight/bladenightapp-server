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

import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.NetMovingPoint;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.relationships.RelationshipStoreSingleton;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.testutils.LogHelper;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import de.greencity.bladenightapp.time.Sleep;
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
		LogHelper.disableLogs();
	}

	@Before
	public void before() {
		Route.setLog(new NoOpLog());
		File file = FileUtils.toFile(EventList.class.getResource(path));
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
	public void test() throws IOException, BadArgumentException, InterruptedException {
		String deviceId1 = "user-1";
		String deviceId2 = "user-2";
		String deviceId3 = "user-3";
		String deviceId4 = "user-4";

		int friendIdFor2 = createRelationShip(deviceId1, deviceId2);
		int friendIdFor3 = createRelationShip(deviceId1, deviceId3);
		int friendIdFor4 = createRelationShip(deviceId1, deviceId4);

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

		NetMovingPoint friend1 = data1.getFriendsMap().get(friendIdFor2); 
		assertTrue(friend1 != null);
		assertEquals(data2.getUserPosition(), friend1.getPosition(), 1.0);

		NetMovingPoint friend2 = data1.getFriendsMap().get(friendIdFor3); 
		assertTrue(friend2 != null);
		assertEquals(data3.getUserPosition(), friend2.getPosition(), 1.0);

		NetMovingPoint friend3 = data1.getFriendsMap().get(friendIdFor4); 
		assertTrue(friend3 != null);
		assertEquals(false, friend3.isInProcession());
		assertEquals(false, friend3.isOnRoute());
		
		Sleep.sleep(10);
		
		data3 = getRealtimeUpdateFromParticipant(deviceId3, 48.160027,  11.561509);
		assertEquals(4729.0, data3.getUserPosition(), 1.0);
		assertEquals(true, data3.isUserOnRoute());

		data1 = getRealtimeUpdateFromParticipant(deviceId1, 48.139341, 11.547129);
		friend3 = data1.getFriendsMap().get(friendIdFor3); 
		assertTrue(friend3 != null);
		assertEquals(true, friend3.isOnRoute());
		assertEquals(friend3.getPosition(), data3.getUserPosition(), 0.0);
		assertTrue(friend3.getEstimatedTimeToArrival() > 0 );
		assertTrue(data3.getUser().getEstimatedTimeToArrival() <= friend3.getEstimatedTimeToArrival() );
	}

	public int createRelationShip(String deviceId1, String deviceId2) throws IOException, BadArgumentException {
		RelationshipOutputMessage output;
		output = sendAndParse(deviceId1, ++friendIdCounter, (long)0);
		int friendId = output.getFriendId();
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

	public RelationshipOutputMessage sendAndParse(String deviceId, int friendId, long relationshipId) throws IOException, BadArgumentException {
		Message message = send(deviceId, friendId, relationshipId);
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
		msg.setPayload(gpsInfo);
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
	static int friendIdCounter = 1;

}
