package de.greencity.bladenightapp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.testutils.Client;
import de.greencity.bladenightapp.testutils.LogHelper;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.messages.CallErrorMessage;
import fr.ocroquette.wampoc.messages.Message;
import fr.ocroquette.wampoc.messages.MessageType;

public class HandshakeTest {

    @Before
    public void init() throws ParseException {
        LogHelper.disableLogs();
        BladenightWampServer server = new BladenightWampServer.ServerBuilder()
                .setMinimumClientBuildNumber(minClientBuildNumber)
                .build();
        client = new Client(server);
    }

    @Test
    public void validBuildNumber() throws IOException, BadArgumentException {
        Message returnedMessage = client.shakeHands("deviceid", minClientBuildNumber, "manufacturer", "model", "4.0.0");
        assertTrue(MessageType.CALLRESULT.equals(returnedMessage.getType()));
    }

    @Test
    public void outdatedBuildNumber() throws IOException, BadArgumentException {
        Message returnedMessage = client.shakeHands("deviceid",minClientBuildNumber-1,"manufacturer", "model", "4.0.0");
        assertTrue(MessageType.CALLERROR.equals(returnedMessage.getType()));
        CallErrorMessage callErrorMessage = (CallErrorMessage) returnedMessage;
        assertEquals(callErrorMessage.getErrorUri(), BladenightError.OUTDATED_CLIENT.getText());
    }

    private Client client;
    private final int minClientBuildNumber = 10;
}
