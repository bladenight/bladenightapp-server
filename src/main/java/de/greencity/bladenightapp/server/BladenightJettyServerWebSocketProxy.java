package de.greencity.bladenightapp.server;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

import fr.ocroquette.wampoc.adapters.jetty.ChannelToConnectionAdapter;
import fr.ocroquette.wampoc.exceptions.BadArgumentException;
import fr.ocroquette.wampoc.server.SessionId;
import fr.ocroquette.wampoc.server.WampServer;

/***
 * Proxy between a specific WebSocket session on the server side and the internal WampServer.
 */
public class BladenightJettyServerWebSocketProxy implements OnTextMessage {
	public BladenightJettyServerWebSocketProxy(WampServer wampServer) {
		this.wampServer = wampServer; 
	}

	@Override
	public void onOpen(Connection connection) {
		sessionId = wampServer.connectClient(new ChannelToConnectionAdapter(connection));
		getLog().info("WAMP session opened: " + sessionId);
	}

	@Override
	public void onClose(int closeCode, String message) {
		wampServer.discardClient(sessionId);
		getLog().info("WAMP session closed: " + sessionId);
	}

	@Override
	public void onMessage(String data) {
		try {
			wampServer.handleIncomingMessage(sessionId, data);
		} catch (IOException e) {
			getLog().warn("Got exception in onMessage", e);
		} catch (BadArgumentException e) {
			getLog().warn("Got exception in onMessage", e);
		}
	}

	private static Log log;

	public static void setLog(Log log) {
		BladenightJettyServerWebSocketProxy.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(BladenightJettyServerWebSocketProxy.class));
		return log;
	}
	
	private WampServer wampServer;
	private SessionId sessionId;
}	
