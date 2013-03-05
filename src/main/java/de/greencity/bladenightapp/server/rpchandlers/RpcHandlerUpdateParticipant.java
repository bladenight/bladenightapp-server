package de.greencity.bladenightapp.server.rpchandlers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.MovingPoint;
import de.greencity.bladenightapp.procession.Participant;
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

		RealTimeUpdateData data = new RealTimeUpdateData();

		if ( input != null ) {
			ParticipantInput participantInput = new ParticipantInput(input.getDeviceId(), input.isParticipating(), input.getLatitude(), input.getLongitude());
			Participant participant = procession.updateParticipant(participantInput);
			data.isUserOnRoute(procession.isParticipantOnRoute(input.getDeviceId()));
			data.setUserPosition((int)participant.getLinearPosition(), participant.getLinearSpeed());
			if ( participant.getDeviceId().equals("TODO-generate")) {
				double time = procession.evaluateTravelTimeBetween(procession.getTailPosition(), participant.getLinearPosition());
				SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
				getLog().info("time left="+(int)(time/1000) + "  eta="+sdf.format(new Date((long) (System.currentTimeMillis() + time))));
			}
		}

		MovingPoint head = procession.getHead();
		MovingPoint tail = procession.getTail();
		data.setHead((int)head.getLinearPosition(), head.getLinearSpeed());
		data.setTail((int)tail.getLinearPosition(), tail.getLinearSpeed());
		data.setRouteLength((int)procession.getRoute().getLength());
		data.setRouteName(procession.getRoute().getName());
		data.setUserTotal(procession.getParticipantCount());
		data.setUserOnRoute(procession.getParticipantOnRoute());

		
		rpcCall.setOutput(data, RealTimeUpdateData.class);
	}

	public boolean validateInput(RpcCall rpcCall, GpsInfo input) {
		if ( input != null && ( input.getDeviceId() == null || input.getDeviceId().length() == 0 ) ) {
			rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
			return false;
		}
		return true;
	}


	private Procession procession;
	
	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerUpdateParticipant.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerUpdateParticipant.class));
		return log;
	}
}
