package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;

public class GetActiveRouteTest {
    final String routeName = "Nord - kurz";
    final String path = "/routes/" + routeName + ".kml";

    @Before
    public void init() {
        LogHelper.disableLogs();

        File file = FileUtils.toFile(GetRouteTest.class.getResource(path));
        assertTrue(file != null);
        route = new Route();
        assertTrue(route.load(file));
        assertEquals(routeName, route.getName());

        procession = new Procession();
        procession.setRoute(route);
        procession.setMaxComputeAge(0);

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setProcession(procession)
        .build();

        client = new Client(server);
    }

    @Test
    public void test() throws IOException, BadArgumentException {
        RouteMessage routeMessage = client.getActiveRoute();
        assertTrue(routeMessage != null);
        assertEquals("Nord - kurz", routeMessage.getRouteName());
        assertEquals(12605, routeMessage.getRouteLength());
        List<LatLong> nodes = routeMessage.getNodes();
        assertTrue(nodes != null);
        assertEquals(nodes.size(), 76);
        assertEquals(nodes.get(0), new LatLong(48.13246449995051, 11.54349921573263));
        assertEquals(nodes.get(75), new LatLong(48.1325299743437, 11.54351506700966));
    }


    private Route route;
    private Procession procession;
    private Client client;

}
