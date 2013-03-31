package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;
import de.greencity.bladenightapp.testutils.LogHelper;
import de.greencity.bladenightapp.testutils.ProtocollingChannel;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.MessageType;
import fr.ocroquette.wampoc.server.Session;

public class SetActiveRouteTest {
	final String initialRouteName = "Nord - kurz";
	final String newRouteName = "Ost - lang";
	final String routesDir = "/routes/";

	@Before
	public void init() {
		LogHelper.disableLogs();

		RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveRouteTest.class.getResource(routesDir)));
		RouteStoreSingleton.setInstance(routeStore);
		route = routeStore.getRoute(initialRouteName);
		assertEquals(initialRouteName, route.getName());

		procession = new Procession();
		procession.setRoute(route);
		procession.setMaxComputeAge(0);
		ProcessionSingleton.setProcession(procession);

		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	@Test
	public void setActiveRouteToValidRoute() throws IOException, BadArgumentException {
		Message returnMessage = setActiveRouteTo(newRouteName);
		assertTrue(returnMessage.getType() == MessageType.CALLRESULT);
		Route newRoute = procession.getRoute();
		assertEquals(newRouteName, newRoute.getName());
		assertEquals(16727, newRoute.getLength(), 1);
	}

	@Test
	public void setActiveRouteToUnavailableRoute() throws IOException, BadArgumentException {
		Message returnMessage = setActiveRouteTo(newRouteName+"-invalid");
		assertTrue(returnMessage.getType() == MessageType.CALLERROR);
		Route newRoute = procession.getRoute();
		assertEquals(initialRouteName, newRoute.getName());
		assertEquals(12605, newRoute.getLength(), 1);
	}

	@Test
	public void setActiveRouteToNullRoute() throws IOException, BadArgumentException {
		Message returnMessage = setActiveRouteTo(null);
		assertTrue(returnMessage.getType() == MessageType.CALLERROR);
		Route newRoute = procession.getRoute();
		assertEquals(initialRouteName, newRoute.getName());
		assertEquals(12605, newRoute.getLength(), 1);
	}


	Message setActiveRouteTo(String newRoute) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.SET_ACTIVE_ROUTE.getText());
		msg.setPayload(newRoute);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		return MessageMapper.fromJson(channel.lastMessage());
	}

	private Route route;
	private Procession procession;
	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
}
