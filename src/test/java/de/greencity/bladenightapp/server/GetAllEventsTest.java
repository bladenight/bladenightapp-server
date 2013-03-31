package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.Event.EventStatus;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.testutils.LogHelper;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class GetAllEventsTest {

	@Before
	public void init() throws ParseException {
		LogHelper.disableLogs();

		eventsList = new EventList();
		eventsList.addEvent(new Event.Builder()
		.setStartDate("2020-06-01T21:00")
		.setRouteName("route 1")
		.setDurationInMinutes(60)
		.setStatus(EventStatus.CANCELLED)
		.build());
		eventsList.addEvent(new Event.Builder()
		.setStartDate("2020-06-08T21:00")
		.setRouteName("route 2")
		.setDurationInMinutes(120)
		.setStatus(EventStatus.CONFIRMED)
		.build());
		EventsListSingleton.setInstance(eventsList);

		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}

	@Test
	public void test() throws IOException, BadArgumentException {
		EventsListMessage data = getAllEvents();
		assertTrue(data != null);
		assertTrue(data.evt != null);
		assertEquals(eventsList, data.convertToEventsList());
	}

	EventsListMessage getAllEvents() throws IOException, BadArgumentException {
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

	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
	private EventList eventsList;
}
