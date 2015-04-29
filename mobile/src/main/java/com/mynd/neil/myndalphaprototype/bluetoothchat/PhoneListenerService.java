package com.mynd.neil.myndalphaprototype.bluetoothchat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yukti on 4/27/15.
 */
public class PhoneListenerService extends WearableListenerService {

    private static String TAG = "MYND";
    private static GoogleApiClient mGoogleApiClient;
    private static Node peerNode;
    private LocalBroadcastManager broadcastManager;
    static final public String PLS_RESULT = "com.mynd.androiddemo.backend.PhoneListenerService.REQUEST_PROCESSED";
    static final public String PLS_MESSAGE = "com.mynd.androiddemo.backend.PhoneListenerService.MSG";
    private StateChanger myState;

    @Override
    public void onCreate(){
        super.onCreate();

        broadcastManager = LocalBroadcastManager.getInstance(this);

        //  Needed for communication between watch and device.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        tellWatchState("START");
                        //  "onConnected: null" is normal.
                        //  There's nothing in our bundle.
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        //TODO: CHANGE THIS to the right name!!!
        myState = MainActivity.getMyState();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.v(TAG, "msg rcvd");
        Log.v(TAG, messageEvent.getPath());

        if(messageEvent.getPath().endsWith("/PHONEOFF")){
            myState.allClear();
            sendResult("/PHONEOFF");
        }
        else{
            Log.v(TAG,"msg received is not OFF");
        }

    }

    public void sendResult(String message) {
        Intent intent = new Intent(PLS_RESULT);
        if(message != null)
            intent.putExtra(PLS_MESSAGE, message);
        broadcastManager.sendBroadcast(intent);
    }

    private void tellWatchState(final String state){

        new AsyncTask<Void, Void, List<Node>>() {

            @Override
            protected List<Node> doInBackground(Void... params) {
                return getNodes();
            }

            @Override
            protected void onPostExecute(List<Node> nodeList) {
                for (final Node node : nodeList) {
                    Log.v(TAG, "telling " + node.getId() + " i am " + state);

                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient,
                            node.getId(),
                            "/" + state,
                            null
                    );

                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v(TAG, "Phone: " + sendMessageResult.getStatus().getStatusMessage());
                            peerNode = node;    //  Save the node that worked so we don't have to loop again.
                        }
                    });
                }
            }
        }.execute();

    }

    private List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();
        NodeApi.GetConnectedNodesResult rawNodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : rawNodes.getNodes()) {
            nodes.add(node);
        }
        return nodes;
    }

    public static void sendToWatch(final String str){
        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient,
                peerNode.getId(),
                str,
                null
        );
        result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Log.d(TAG,"sent message " + str);
            }
        });
    }

}
