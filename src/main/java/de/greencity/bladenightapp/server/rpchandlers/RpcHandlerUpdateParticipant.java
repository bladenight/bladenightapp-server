package de.greencity.bladenightapp.server.rpchandlers;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.MovingPoint;
import de.greencity.bladenightapp.procession.ParticipantInput;
import de.greencity.bladenightapp.procession.Procession;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerUpdateParticipant extends RpcHandler {

	public RpcHandlerUpdateParticipant(Procession procession) {
		this.procession = procession;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		GpsInfo input = rpcCall.getInput(GpsInfo.class);
		
		if ( ! validateInput(rpcCall, input) )
			return;
		
		if ( procession == null ) {
			rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Internal error: Procession is null");
			return;
		}
		
		ParticipantInput participantInput = new ParticipantInput(input.getDeviceId(), input.getLatitude(), input.getLongitude());
		procession.updateParticipant(participantInput);
		
		RealTimeUpdateData data = new RealTimeUpdateData();
		MovingPoint head = procession.getHead();
		MovingPoint tail = procession.getTail();
		data.setHead(head.getLinearPosition(), head.getLinearSpeed());
		data.setTail(tail.getLinearPosition(), tail.getLinearSpeed());
		rpcCall.setOutput(data, RealTimeUpdateData.class);
	}

	public boolean validateInput(RpcCall rpcCall, GpsInfo input) {
		if ( input == null || input.getDeviceId() == null ) {
			rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
			return false;
		}
		return true;
	}


	private Procession procession;
}
