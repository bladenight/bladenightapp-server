package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.testutils.LogHelper;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.CallResultMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class GetActiveRouteTest {
	final String routeName = "Nord - kurz";
	final String path = "/routes/" + routeName + ".kml";

	@Before
	public void init() {
		LogHelper.disableLogs();

		File file = FileUtils.toFile(GetActiveRouteTest.class.getResource(path));
		assertTrue(file != null);
		route = new Route();
		assertTrue(route.load(file));
		assertEquals(routeName, route.getName());

		procession = new Procession();
		procession.setRoute(route);
		procession.setMaxComputeAge(0);
		ProcessionSingleton.setProcession(procession);

		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	@Test
	public void test() throws IOException, BadArgumentException {
		RouteMessage routeMessage = getActiveRoute();
		assertTrue(routeMessage != null);
		assertEquals("Nord - kurz", routeMessage.getRouteName());
		assertEquals(12605, routeMessage.getRouteLength());
		List<LatLong> nodes = routeMessage.getNodes();
		assertTrue(nodes != null);
		assertEquals(nodes.size(), 76);
		assertEquals(nodes.get(0), new LatLong(48.13246449995051, 11.54349921573263));
		assertEquals(nodes.get(75), new LatLong(48.1325299743437, 11.54351506700966));
	}


	RouteMessage getActiveRoute() throws IOException, BadArgumentException {
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

	private Route route;
	private Procession procession;
	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
}
