package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageType;

public class GetRouteTest {
    final String routesDir = "/routes/";

    @Before
    public void init() {
        LogHelper.disableLogs();

        RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveRouteTest.class.getResource(routesDir)));

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setRouteStore(routeStore)
        .build();

        client = new Client(server);
    }

    @Test
    public void test() throws IOException, BadArgumentException {
        final String routeName = "Nord - kurz";
        Message message = client.getRoute(routeName);
        assertTrue(message.getType().equals(MessageType.CALLRESULT));
        RouteMessage routeMessage = ((CallResultMessage)message).getPayload(RouteMessage.class);
        assertTrue(routeMessage != null);
        assertEquals(routeName, routeMessage.getRouteName());
        assertEquals(12605, routeMessage.getRouteLength());
        List<LatLong> nodes = routeMessage.getNodes();
        assertTrue(nodes != null);
        assertEquals(nodes.size(), 76);
        assertEquals(nodes.get(0), new LatLong(48.13246449995051, 11.54349921573263));
        assertEquals(nodes.get(75), new LatLong(48.1325299743437, 11.54351506700966));
    }


    private Client client;

}
