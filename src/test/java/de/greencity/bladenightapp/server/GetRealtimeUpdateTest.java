package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;

public class GetRealtimeUpdateTest {
    final String routeName = "Nord - kurz";
    final String path = "/routes/" + routeName + ".kml";

    @Before
    public void init() {
        LogHelper.disableLogs();

        File file = FileUtils.toFile(GetRealtimeUpdateTest.class.getResource(path));
        assertTrue(file != null);
        route = new Route();
        assertTrue(route.load(file));
        assertEquals(routeName, route.getName());

        procession = new Procession();
        procession.setRoute(route);
        procession.setMaxComputeAge(0);

        BladenightWampServerMain server = new BladenightWampServerMain.Builder()
        .setProcession(procession)
        .setRelationshipStore(new RelationshipStore())
        .build();

        client = new Client(server);
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
    public void estimatedTimes() throws IOException, BadArgumentException {
        RealTimeUpdateData data1 = getRealtimeUpdateFromParticipant("movingUser", 48.139341, 11.547129);
        assertTrue(data1 != null);
        assertTrue(data1.getTail().getEstimatedTimeToArrival() == 0);
        assertTrue(data1.getHead().getEstimatedTimeToArrival() == 0);
        RealTimeUpdateData data2 = getRealtimeUpdateFromParticipant("movingUser", 48.143655, 11.548839);
        assertTrue(data2 != null);
        assertTrue(data2.getTail().getEstimatedTimeToArrival() > 0);
        assertTrue(data2.getHead().getEstimatedTimeToArrival() > 0);
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
        return client.getRealtimeUpdate(new GpsInfo(clientId, true, lat, lon));
    }

    RealTimeUpdateData getRealtimeUpdateFromLocalizedObserver(String clientId, double lat, double lon) throws IOException, BadArgumentException {
        return client.getRealtimeUpdate(new GpsInfo(clientId, false, lat, lon));
    }

    RealTimeUpdateData getRealtimeUpdateFromUnlocalizedObserver() throws IOException, BadArgumentException {
        return client.getRealtimeUpdate(null);
    }

    private Route route;
    private Procession procession;
    private Client client;
}
