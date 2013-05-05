package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.Event.EventStatus;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;

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
		.setParticipants(200)
		.setStatus(EventStatus.CONFIRMED)
		.build());
		EventsListSingleton.setInstance(eventsList);

		client = new Client(new BladenightWampServer());
	}

	@Test
	public void test() throws IOException, BadArgumentException {
		EventsListMessage data = client.getAllEvents();
		assertTrue(data != null);
		assertTrue(data.evt != null);
		assertEquals(eventsList, data.convertToEventsList());
	}

	private Client client;
	private EventList eventsList;
}
