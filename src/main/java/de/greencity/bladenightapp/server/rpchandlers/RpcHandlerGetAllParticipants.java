package de.greencity.bladenightapp.server.rpchandlers;

import java.util.List;

import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.MovingPoint;
import de.greencity.bladenightapp.procession.Participant;
import de.greencity.bladenightapp.procession.Procession;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetAllParticipants extends RpcHandler {

	public RpcHandlerGetAllParticipants(Procession procession) {
		this.procession = procession;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		List<Participant> participants = procession.getParticipants();
		LatLong[] coordinates = new LatLong[participants.size()];
		int i = 0;
		for ( Participant p : participants) {
			coordinates[i++] = new LatLong(p.getLatitude(), p.getLongitude()); 
		}
		rpcCall.setOutput(coordinates, LatLong[].class);
	}

	private Procession procession;
}
