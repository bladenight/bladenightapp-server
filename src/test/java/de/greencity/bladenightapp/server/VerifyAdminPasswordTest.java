package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.security.PasswordSafe;
import de.greencity.bladenightapp.security.PasswordSafeSingleton;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;

public class VerifyAdminPasswordTest {

	@Before
	public void init() throws IOException {
		LogHelper.disableLogs();

		passwordSafe = new PasswordSafe();
		passwordSafe.setAdminPassword(password);
		PasswordSafeSingleton.setInstance(passwordSafe);
		
		client = new Client(new BladenightWampServer());

	}
	
	@Test
	public void verifyPassword() throws IOException, BadArgumentException {
		assertTrue(client.verifyPasswordAgainstServer(password));
		assertTrue(! client.verifyPasswordAgainstServer("invalid password"));
	}

	private Client client;
	private PasswordSafe passwordSafe;
	final static String password = UUID.randomUUID().toString();
}
