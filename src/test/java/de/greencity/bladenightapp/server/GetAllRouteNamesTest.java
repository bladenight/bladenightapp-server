package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.messages.RouteNamesMessage;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageType;

public class GetAllRouteNamesTest {
	final Set<String> expectedNames = new HashSet<String>();
	final String newRouteName = "Ost - lang";
	final String routesDir = "/routes/";

	@Before
	public void init() {
		LogHelper.disableLogs();

		RouteStore routeStore = new RouteStore(FileUtils.toFile(GetAllRouteNamesTest.class.getResource(routesDir)));

		expectedNames.add("Nord - kurz");
		expectedNames.add("Ost - lang");
		
		BladenightWampServer server = new BladenightWampServer.ServerBuilder()
		.setRouteStore(routeStore)
		.build();

		client = new Client(server);
	}
	
	@Test
	public void test() throws IOException, BadArgumentException {
		Message returnMessage = client.getAllRouteNames();
		assertTrue(returnMessage.getType() == MessageType.CALLRESULT);
		RouteNamesMessage routeNamesMessage = ((CallResultMessage)returnMessage).getPayload(RouteNamesMessage.class);
		assertEquals(2, routeNamesMessage.rna.length);
		assertEquals("Nord - kurz", routeNamesMessage.rna[0]);
		assertEquals("Ost - lang", routeNamesMessage.rna[1]);
	}


	private Client client;
}
