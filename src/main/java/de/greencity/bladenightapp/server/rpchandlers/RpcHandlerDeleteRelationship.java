package de.greencity.bladenightapp.server.rpchandlers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.relationships.RelationshipStore;
import fr.ocroquette.wampoc.server.RpcCall;
import fr.ocroquette.wampoc.server.RpcHandler;

public class RpcHandlerDeleteRelationship extends RpcHandler {

    public RpcHandlerDeleteRelationship(RelationshipStore relationshipStore) {
        this.relationshipStore = relationshipStore;
    }

    @Override
    public void execute(RpcCall rpcCall) {
        RelationshipInputMessage input = rpcCall.getInput(RelationshipInputMessage.class);

        if ( ! validateInput(rpcCall, input) )
            return;

        if ( relationshipStore == null ) {
            rpcCall.setError(BladenightError.INTERNAL_ERROR.getText(), "Internal error: relationshipStore is null");
            return;
        }

        if ( input.getDeviceId() == null || input.getDeviceId().length() == 0 ) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid device id:" + input.getDeviceId() );
            return;
        }

        if ( input.getFriendId() <= 0 ) {
            rpcCall.setError(BladenightError.INVALID_ARGUMENT.getText(), "Invalid device id:" + input.getDeviceId() );
            return;
        }

        getLog().info("Trying to delete relationship for deviceId=" + input.getDeviceId() +  " friendId=" + input.getFriendId() );

        int hits = relationshipStore.deleteRelationship(input.getDeviceId(), input.getFriendId());

        getLog().info("Deleted " + hits + " relationship(s)" );

        try {
            relationshipStore.write();
        }
        catch(IOException e){
            getLog().error("Failed to write relationships: "  + e);
        }
    }

    public boolean validateInput(RpcCall rpcCall, RelationshipInputMessage input) {
        if ( input == null ) {
            rpcCall.setError(BladenightUrl.BASE+"invalidInput", "Invalid input: "+ input);
            return false;
        }
        return true;
    }


    private RelationshipStore relationshipStore;

    private static Log log;

    public static void setLog(Log log) {
        RpcHandlerDeleteRelationship.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(RpcHandlerDeleteRelationship.class));
        return log;
    }
}
