package com.mynd.neil.myndalphaprototype;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity
        implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks{

    //private TextView mTextView;
    private String TAG = "myndwear";
    private GoogleApiClient mGoogleApiClient;
    //private Button offButton;
    private RelativeLayout wearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Is needed for communication between the wearable and the device.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //mTextView = (TextView) stub.findViewById(R.id.text);
                //offButton = (Button) stub.findViewById(R.id.offButton);
                //offButton.setOnClickListener(new View.OnClickListener() {
                //    @Override
                //    public void onClick(View v) {
                //        //send message to phone to turn off sound
                //        Log.d(TAG,"turn off sound");
                //        sendToPhone("/PHONEOFF");
                //    }
               // });
                wearLayout = (RelativeLayout) stub.findViewById(R.id.watchLayout);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "connected to Google Play Services on Wear!");
        Wearable.MessageApi.addListener(mGoogleApiClient, this).setResultCallback(resultCallback);
    }

    /**
     * Not needed, but here to show capabilities. This callback occurs after the MessageApi
     * listener is added to the Google API Client.
     */
    private ResultCallback<Status> resultCallback =  new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            Log.v(TAG, "Status: " + status.getStatus().isSuccess());
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    sendToPhone("/START");
                    return null;
                }
            }.execute();
        }
    };

    /**
     * receives message from watch
     * @param str
     */
    public void sendToPhone(String str){
        NodeApi.GetConnectedNodesResult rawNodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (final Node node : rawNodes.getNodes()) {
            Log.v(TAG, "Node: " + node.getId());
            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                    mGoogleApiClient,
                    node.getId(),
                    str,
                    null
            );

            result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    Log.v(TAG, "Our callback is done.");
                    changeBackgroundColor(Color.BLACK);
                }
            });
        }
    }



    @Override
    public void onConnectionSuspended(int i) {//do nothing here

    }

    /**
     *     receives message from phone
     */
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
/*
        This method apparently runs in a background thread.
         */

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().endsWith("/START")){
                    //mTextView.setText("Is connected to phone");
                    Log.d(TAG, "connected");
                }
                else if(messageEvent.getPath().endsWith("/ALARMON")){
                    changeBackgroundColor(Color.RED);
                    Log.d(TAG,"alarm ON");
                }
                else if(messageEvent.getPath().endsWith("/ALARMOFF")){
                    changeBackgroundColor(Color.BLACK);
                    Log.d(TAG,"alarm OFF");
                }
                else if(messageEvent.getPath().endsWith("/OTHERALARM")){
                    changeBackgroundColor(Color.YELLOW);
                    Log.d(TAG, "other alarm");
                }
                else {
                    Log.d(TAG,"no proper suffix in msg");
                }
            }
        });

        Log.v(TAG, "Message received on wear: " + messageEvent.getPath());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
    }

    public void changeBackgroundColor(int color){
        wearLayout.setBackgroundColor(color);
    }
}
