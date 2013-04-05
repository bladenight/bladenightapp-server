package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import de.greencity.bladenightapp.persistence.ListPersistor;
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

public class SetActiveStatusTest {
	final String initialRouteName = "Nord - kurz";
	final String newRouteName = "Ost - lang";
	final String routesDir = "/routes/";

	@Before
	public void init() throws IOException {
		LogHelper.disableLogs();

		RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveStatusTest.class.getResource(routesDir)));
		RouteStoreSingleton.setInstance(routeStore);
		route = routeStore.getRoute(initialRouteName);
		assertEquals(initialRouteName, route.getName());

		File tmpFolder = createTemporaryFolder();
		persistenceFolder = new File(tmpFolder, "copy");
		File srcDir = FileUtils.toFile(EventList.class.getResource("/events/"));
		FileUtils.copyDirectory(srcDir, persistenceFolder);

		ListPersistor<Event> persistor = new ListPersistor<Event>(Event.class, persistenceFolder);

		eventList = new EventList();
		eventList.setPersistor(persistor);
		eventList.read();

		EventsListSingleton.setInstance(eventList);

		procession = new Procession();
		procession.setRoute(route);
		procession.setMaxComputeAge(0);
		ProcessionSingleton.setProcession(procession);

		channel = new ProtocollingChannel();

		server = new BladenightWampServer();
		session = server.openSession(channel);
	}
	
	@Test
	public void setActiveStatus() throws IOException, BadArgumentException {
		setActiveStatusTo(EventStatus.CAN);
		verifyPersistency(de.greencity.bladenightapp.events.Event.EventStatus.CANCELLED);
	}

	private void verifyPersistency(de.greencity.bladenightapp.events.Event.EventStatus status) throws JsonSyntaxException, IOException {
		File file = new File(persistenceFolder, "2020-03-03.per");
		Event event = EventGsonHelper.getGson().fromJson(FileUtils.readFileToString(file), Event.class);
		assertEquals(status, event.getStatus());
	}
	
	private void setActiveStatusTo(EventStatus newStatus) throws IOException, BadArgumentException {
		int messageCount = channel.handledMessages.size();
		String callId = UUID.randomUUID().toString();
		CallMessage msg = new CallMessage(callId,BladenightUrl.SET_ACTIVE_STATUS.getText());
		msg.setPayload(newStatus);
		server.handleIncomingMessage(session, msg);
		assertEquals(messageCount+1, channel.handledMessages.size());
		Message returnedMessage = MessageMapper.fromJson(channel.lastMessage());
		assertEquals(MessageType.CALLRESULT, returnedMessage.getType());
	}

	public File createTemporaryFolder() throws IOException  {
		File file = File.createTempFile("tmpfolder", ".d");
		file.delete();
		file.mkdir();
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
		return file;
	}

	private Route route;
	private Procession procession;
	private ProtocollingChannel channel;
	private BladenightWampServer server;
	private Session session;
	private File persistenceFolder;
	private EventList eventList;
}