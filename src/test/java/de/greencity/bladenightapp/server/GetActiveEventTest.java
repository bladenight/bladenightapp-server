package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.Event.EventStatus;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;

public class GetActiveEventTest {

	@Before
	public void init() throws ParseException {
		LogHelper.disableLogs();

		eventList = new EventList();
		eventList.addEvent(new Event.Builder()
		.setStartDate("2020-06-01T21:00")
		.setRouteName("route 1")
		.setDurationInMinutes(60)
		.setStatus(EventStatus.CANCELLED)
		.build());
		eventList.addEvent(new Event.Builder()
		.setStartDate("2020-06-08T21:00")
		.setRouteName("route 2")
		.setDurationInMinutes(120)
		.setStatus(EventStatus.CONFIRMED)
		.build());
		
		BladenightWampServer server = new BladenightWampServer.ServerBuilder()
			.setEventList(eventList)
			.build();

		client = new Client(server);

	}

	@Test
	public void test() throws IOException, BadArgumentException {
		EventMessage data = client.getActiveEvent();
		assertEquals(data.toEvent(), eventList.get(0));
	}

	private EventList eventList;
	private Client client;
}
