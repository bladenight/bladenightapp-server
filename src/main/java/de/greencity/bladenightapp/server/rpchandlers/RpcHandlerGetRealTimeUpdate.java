package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.MovingPoint;
import de.greencity.bladenightapp.procession.Procession;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetRealTimeUpdate extends RpcHandler {

	public RpcHandlerGetRealTimeUpdate(Procession procession) {
		this.procession = procession;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		RealTimeUpdateData data = new RealTimeUpdateData();
		MovingPoint head = procession.getHead();
		MovingPoint tail = procession.getTail();
		data.setRouteLength((int)procession.getRoute().getLength());
		data.setRouteName(procession.getRoute().getName());
		data.setHead(head.getLinearPosition(), head.getLinearSpeed());
		data.setTail(tail.getLinearPosition(), tail.getLinearSpeed());
		rpcCall.setOutput(data, RealTimeUpdateData.class);
	}

	private Procession procession;
}
