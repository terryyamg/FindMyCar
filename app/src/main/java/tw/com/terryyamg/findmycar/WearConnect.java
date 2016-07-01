package tw.com.terryyamg.findmycar;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class WearConnect extends Thread {
    String path;
    String message;
    GoogleApiClient mGoogleClient;
    // Constructor to send a message to the data layer
    WearConnect(String p, String msg,GoogleApiClient googleClient) {
        path = p;
        message = msg;
        mGoogleClient = googleClient;
    }

    public void run() {

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v("MessageActivity", "Message: {" + message + "} sent to: " + node.getDisplayName());
            } else {
                // Log an error
                Log.v("MessageActivity", "ERROR: failed to send Message");
            }
        }
    }
}

