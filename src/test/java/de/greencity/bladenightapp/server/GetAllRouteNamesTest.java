package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.impl.NoOpLog;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.RouteNamesMessage;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class GetAllRouteNamesTest {
	final Set<String> expectedNames = new HashSet<String>();
	final String newRouteName = "Ost - lang";
	final String routesDir = "/routes/";

	@Before
	public void init() {
		Route.setLog(new NoOpLog());

		RouteStore routeStore = new RouteStore(FileUtils.toFile(GetAllRouteNamesTest.class.getResource(routesDir)));
		RouteStoreSingleton.setInstance(routeStore);

		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
		
		expectedNames.add("Nord - kurz");
		expectedNames.add("Ost - lang");
	}
	
	@Test
	public void test() throws IOException, BadArgumentException {
		Message returnMessage = getAllRouteNames();
		assertTrue(returnMessage.getType() == MessageType.CALLRESULT);
		RouteNamesMessage routeNamesMessage = ((CallResultMessage)returnMessage).getPayload(RouteNamesMessage.class);
		assertEquals(2, routeNamesMessage.rna.length);
		assertEquals("Nord - kurz", routeNamesMessage.rna[0]);
		assertEquals("Ost - lang", routeNamesMessage.rna[1]);
	}


	Message getAllRouteNames() throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.GET_ALL_ROUTE_NAMES.getText());
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
}
