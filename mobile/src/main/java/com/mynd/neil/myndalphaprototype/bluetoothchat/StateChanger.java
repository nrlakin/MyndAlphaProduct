package com.mynd.neil.myndalphaprototype.bluetoothchat;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.FileDescriptor;
import java.io.IOException;


/**
 * Created by Yukti on 4/27/15.
 */
public class StateChanger extends Fragment{
    private LinearLayout phoneLayout;
    private BroadcastReceiver receiver;
    //TODO: change to whatever classname you have
    private MainActivity phoneContext;

    //TODO:make sure you put music file in raw folder in mobile resources
    final MediaPlayer mp = new MediaPlayer();


    String TAG = "State";

    //fragment needs no argument constructor
    public StateChanger(){
        //watch sends message to phone to turn off
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(PhoneListenerService.PLS_MESSAGE);
                allClear();
            }
        };
    }


    //TODO: change the parameter of this context to whatever the main phone activity is
    /**
     * NEED to set before doing anything else.
     * @param _context
     */
    public void setContext(MainActivity _context){
        phoneContext = _context;
        // TODO: change to whatever phoneLayout name you have
        phoneLayout = (LinearLayout) phoneContext.findViewById(R.id.sample_main_layout);

    }


    /**
     * iAmMissing
     * red screen
     * music on
     * @return
     */
    public void iAmMissing(){
        PhoneListenerService.sendToWatch("/ALARMON");
        //changeBackgroundColor(Color.RED);
        //music on
        try {
            AssetFileDescriptor afd = phoneContext.getAssets().openFd("alarm.mp3");//directly file name
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            mp.reset();
            mp.setDataSource(fileDescriptor,afd.getStartOffset(),afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * someoneIsMissing
     * yellow screen
     * music off
     * @return
     */
    public void someoneIsMissing(){
        PhoneListenerService.sendToWatch("/OTHERALARM");
        //changeBackgroundColor(Color.YELLOW);
        mp.stop();
    }

    /**
     * allClear
     * green screen
     * music off
     * @return
     */
    public void allClear(){
        PhoneListenerService.sendToWatch("/ALARMOFF");
        //changeBackgroundColor(Color.GREEN);
        mp.stop();
    }

    /**
     * change the background of phone
     //* @param color
     */
    //private void changeBackgroundColor(final int color){
    //    phoneContext.runOnUiThread(new Runnable() {
    //        @Override
    //        public void run() {
    //            phoneLayout.setBackgroundColor(color);
    //        }
    //    });
   // }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(phoneContext).registerReceiver((receiver),
                new IntentFilter(PhoneListenerService.PLS_RESULT)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(phoneContext).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
