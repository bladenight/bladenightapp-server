package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.events.EventsListSingleton;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.persistence.InconsistencyException;
import de.greencity.bladenightapp.persistence.ListPersistor;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.ProcessionSingleton;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.RouteStore;
import de.greencity.bladenightapp.routes.RouteStoreSingleton;
import de.greencity.bladenightapp.security.PasswordSafe;
import de.greencity.bladenightapp.security.PasswordSafeSingleton;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallErrorMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageType;

public class SetActiveRouteTest {
	final String initialRouteName = "Nord - kurz";
	final String newRouteName = "Ost - lang";
	final String routesDir = "/routes/";
	final String adminPassword = "test1234";

	@Before
	public void init() throws IOException, InconsistencyException {
		LogHelper.disableLogs();

		RouteStore routeStore = new RouteStore(FileUtils.toFile(SetActiveRouteTest.class.getResource(routesDir)));
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
		
		passwordSafe = new PasswordSafe();
		passwordSafe.setAdminPassword(adminPassword);
		PasswordSafeSingleton.setInstance(passwordSafe);

		client = new Client(new BladenightWampServer());
	}
	
	@Test
	public void setActiveRouteToValidRoute() throws IOException, BadArgumentException {
		Message returnMessage = client.setActiveRouteTo(newRouteName, adminPassword);
		assertTrue(returnMessage.getType() == MessageType.CALLRESULT);
		Route newRoute = procession.getRoute();
		assertEquals(newRouteName, newRoute.getName());
		assertEquals(16727, newRoute.getLength(), 1);
		verifyPersistency(newRouteName);
	}

	@Test
	public void setActiveRouteToValidRouteWithInvalidPassword() throws IOException, BadArgumentException {
		Message returnMessage = client.setActiveRouteTo(newRouteName, adminPassword + "-invalid");
		assertTrue(returnMessage.getType() == MessageType.CALLERROR);
		CallErrorMessage errorMessage = (CallErrorMessage)returnMessage;
		assertEquals(BladenightError.INVALID_PASSWORD.getText(), errorMessage.getErrorUri());
		Route newRoute = procession.getRoute();
		assertEquals(initialRouteName, newRoute.getName());
		verifyPersistency(initialRouteName);
	}

	@Test
	public void setActiveRouteToUnavailableRoute() throws IOException, BadArgumentException {
		Message returnMessage = client.setActiveRouteTo(newRouteName+"-invalid", adminPassword);
		assertTrue(returnMessage.getType() == MessageType.CALLERROR);
		Route newRoute = procession.getRoute();
		assertEquals(initialRouteName, newRoute.getName());
		assertEquals(12605, newRoute.getLength(), 1);
	}

	@Test
	public void setActiveRouteToNullRoute() throws IOException, BadArgumentException {
		Message returnMessage = client.setActiveRouteTo(null, adminPassword);
		assertTrue(returnMessage.getType() == MessageType.CALLERROR);
		Route newRoute = procession.getRoute();
		assertEquals(initialRouteName, newRoute.getName());
		assertEquals(12605, newRoute.getLength(), 1);
	}


	public File createTemporaryFolder() throws IOException  {
		File file = File.createTempFile("tmpfolder", ".d");
		file.delete();
		file.mkdir();
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
		return file;
	}

	private void verifyPersistency(String routeName) throws JsonSyntaxException, IOException {
		File file = new File(persistenceFolder, "2020-03-03.per");
		Event event = EventGsonHelper.getGson().fromJson(FileUtils.readFileToString(file), Event.class);
		assertEquals(routeName, event.getRouteName());
	}
	



	private Route route;
	private Procession procession;
	private Client client;
	private File persistenceFolder;
	private EventList eventList;
	private PasswordSafe passwordSafe;
}
