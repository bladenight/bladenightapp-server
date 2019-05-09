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
import de.greencity.bladenightapp.network.messages.EventListMessage;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;

public class GetAllEventsTest {

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
        .setParticipants(200)
        .setStatus(EventStatus.CONFIRMED)
        .build());

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setEventList(eventList)
        .build();

        client = new Client(server);
    }

    @Test
    public void test() throws IOException, BadArgumentException {
        EventListMessage data = client.getAllEvents();
        assertTrue(data != null);
        assertTrue(data.evt != null);
        assertEquals(eventList, data.convertToEventsList());
    }

    private Client client;
    private EventList eventList;
}
