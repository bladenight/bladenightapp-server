package de.greencity.bladenightapp.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import de.greencity.bladenightapp.network.messages.AdminMessage;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.HandshakeClientMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.network.messages.SetActiveRouteMessage;
import de.greencity.bladenightapp.network.messages.SetActiveStatusMessage;
import de.greencity.bladenightapp.server.BladenightWampServer;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class Client {
	public Client(BladenightWampServer server) {
		this.channel = new ProtocollingChannel();
		this.server = server;
		this.session = server.openSession(channel);
	}

	public RealTimeUpdateData getRealtimeUpdate(GpsInfo gpsInfo) throws IOException, BadArgumentException {
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

	public EventMessage getActiveEvent() throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_ACTIVE_EVENT.getText());
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message message = MessageMapper.fromJson(channel.lastMessage());
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(EventMessage.class);
	}

	public RouteMessage getActiveRoute() throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_ACTIVE_ROUTE.getText());
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message message = MessageMapper.fromJson(channel.lastMessage());
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(RouteMessage.class);
	}

	public EventsListMessage getAllEvents() throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_ALL_EVENTS.getText());
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message message = MessageMapper.fromJson(channel.lastMessage());
		assertTrue(message.getType() == MessageType.CALLRESULT);
		CallResultMessage callResult = (CallResultMessage) message;
		return callResult.getPayload(EventsListMessage.class);
	}

	public Message getAllRouteNames() throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_ALL_ROUTE_NAMES.getText());
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}


	public Message sendRelationshipRequest(String deviceId, int friendId, long requestId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.CREATE_RELATIONSHIP.getText());
		RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, requestId);
		msg.setPayload(partnershipMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
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

	public Message deleteReleationship(String deviceId, int friendId) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.DELETE_RELATIONSHIP.getText());
		RelationshipInputMessage partnershipMessage = new RelationshipInputMessage(deviceId, friendId, 0);
		msg.setPayload(partnershipMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public Message setActiveRouteTo(String newRoute, String password) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.SET_ACTIVE_ROUTE.getText());
		SetActiveRouteMessage payload = new SetActiveRouteMessage(newRoute, password);
		assertTrue(payload.verify(password, 10000));
		payload.setRouteName(newRoute);
		msg.setPayload(payload);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public Message setActiveStatusTo(EventStatus newStatus, String password) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.SET_ACTIVE_STATUS.getText());

		SetActiveStatusMessage payload = new SetActiveStatusMessage(newStatus, password);
		assertTrue(payload.verify(password, 10000));
		
		msg.setPayload(payload);
		
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	public boolean verifyPasswordAgainstServer(String password) throws IOException, BadArgumentException {
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


	public Message shakeHands(String deviceId, int buildNumber, String phoneManufacturer, String phoneModel, String androidRelease) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.SHAKE_HANDS.getText());
		HandshakeClientMessage handshakeClientMessage = new HandshakeClientMessage(deviceId, buildNumber, phoneManufacturer, phoneModel, androidRelease);
		msg.setPayload(handshakeClientMessage);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}


	private ProtocollingChannel channel;
	private Session session;
	private BladenightWampServer server;
}
