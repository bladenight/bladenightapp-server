package de.greencity.bladenightapp.server;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import fr.ocroquette.wampoc.server.WampServer;

public class BladenightJettyServerHandler extends WebSocketHandler {
	
	public BladenightJettyServerHandler() {
		this.wampocServer = new WampServer();
	}

	public BladenightJettyServerHandler(WampServer wampocServer) {
		this.wampocServer = wampocServer;
	}
	
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request,
			String protocol) {
		return new BladenightJettyServerWebSocketProxy(wampocServer);
	}

	protected WampServer wampocServer;
}
