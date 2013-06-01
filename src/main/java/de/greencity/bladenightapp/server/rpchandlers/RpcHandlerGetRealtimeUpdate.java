package de.greencity.bladenightapp.server.rpchandlers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.FriendMessage;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.procession.Participant;
import de.greencity.bladenightapp.procession.ParticipantInput;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.relationships.RelationshipMember;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerGetRealtimeUpdate extends RpcHandler {

	public RpcHandlerGetRealtimeUpdate(Procession procession, RelationshipStore relationshipStore) {
		this.procession = procession;
		this.relationshipStore = relationshipStore;
	}

	@Override
	public void execute(RpcCall rpcCall) {
		GpsInfo gpsInput = rpcCall.getInput(GpsInfo.class);

		if ( ! validateInput(rpcCall, gpsInput) )
			return;

		if ( procession == null ) {
			rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Internal error: Procession is null");
			return;
		}

		RealTimeUpdateData data = new RealTimeUpdateData();

		if ( gpsInput != null ) {
			ParticipantInput participantInput = new ParticipantInput(gpsInput.getDeviceId(), gpsInput.isParticipating(), gpsInput.getLatitude(), gpsInput.getLongitude(), gpsInput.getAccuracy());
			Participant participant = procession.updateParticipant(participantInput);
			data.isUserOnRoute(participant.isOnRoute());
			data.setUserPosition((long)participant.getLinearPosition(), (long)participant.getLinearSpeed());
		}

		double routeLength = procession.getRoute().getLength();
		
		data.setHead(procession.getHead());
		data.getHead().setEstimatedTimeToArrival((long)(procession.evaluateTravelTimeBetween(data.getHead().getPosition(), routeLength)));
		
		data.setTail(procession.getTail());
		data.getTail().setEstimatedTimeToArrival((long)(procession.evaluateTravelTimeBetween(data.getTail().getPosition(), routeLength)));
		
		data.setRouteLength((int)procession.getRoute().getLength());
		data.setRouteName(procession.getRoute().getName());
		data.setUserTotal(procession.getParticipantCount());
		data.setUserOnRoute(procession.getParticipantsOnRoute());
		data.getUser().setEstimatedTimeToArrival((long)(procession.evaluateTravelTimeBetween(data.getUser().getPosition(), routeLength)));

		if ( gpsInput != null ) {
			List<RelationshipMember> relationships = relationshipStore.getFinalizedRelationships(gpsInput.getDeviceId());
			for (RelationshipMember relationshipMember : relationships) {
				Participant participant = procession.getParticipant(relationshipMember.getDeviceId());
				FriendMessage friendMessage;
				if ( participant != null) {
					friendMessage = new FriendMessage();
					friendMessage.copyFrom(participant.getLastKnownPoint());
					friendMessage.setEstimatedTimeToArrival((long)(procession.evaluateTravelTimeBetween(participant.getLinearPosition(), routeLength)));
					data.addFriend(relationshipMember.getFriendId(), friendMessage);
				}
			}
		}

		rpcCall.setOutput(data, RealTimeUpdateData.class);
	}

	public boolean validateInput(RpcCall rpcCall, GpsInfo input) {
		if ( input == null )
			return true;

		if ( input.getDeviceId() == null || input.getDeviceId().length() == 0 ) {
			rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
			return false;
		}
		return true;
	}


	private Procession procession;
	private RelationshipStore relationshipStore;

	private static Log log;

	public static void setLog(Log log) {
		RpcHandlerGetRealtimeUpdate.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(RpcHandlerGetRealtimeUpdate.class));
		return log;
	}
}
