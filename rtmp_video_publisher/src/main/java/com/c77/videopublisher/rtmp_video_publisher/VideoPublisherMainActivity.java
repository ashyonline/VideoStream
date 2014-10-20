package com.c77.videopublisher.rtmp_video_publisher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.c77.videopublisher.rtmp_video_publisher.manager.ClientManager;

public class VideoPublisherMainActivity extends Activity implements View.OnClickListener {

    public static final int STOPPED = 0;
    public static final int RECORDING = 1;

    ClientManager clientManager = new ClientManager();
    Button startButton = null;
    Button stopButton = null;
    Button exitButon = null;
    //TextView textView = null;
    int status = STOPPED;

    public void onClick(View v) {
        if (v == startButton) {
            this.setTitle("Started!");
            if(status == STOPPED){
                clientManager.setRecording(true);
                status = RECORDING;
            }
        }
        if (v == stopButton) {
            this.setTitle("stoped");
            if(status == RECORDING){
                clientManager.setRecording(false);
                status = STOPPED;
            }
        }
        if (v == exitButon) {
            clientManager.setRecording(false);
            clientManager.setRunning(false);
            System.exit(0);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        startButton = new Button(this);
        stopButton = new Button(this);
        exitButon = new Button(this);
        //textView = new TextView(this);

        startButton.setText("Start");
        stopButton.setText("Stop");
        exitButon.setText("Exit");
        /*textView.setText("Android Recorder ChangeLog£º" +
                "\n(1)Get PCM data." +
                "\n(2)Encode with speex." +
                "\n(3)Package in flv format" +
                "\n(4)Publish audio to server." +
                "\n(5)Record both local and server side.");*/

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        exitButon.setOnClickListener(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        //layout.addView(textView);
        layout.addView(startButton);
        layout.addView(stopButton);
        layout.addView(exitButon);
        this.setContentView(layout);

        clientManager.setMode(ClientManager.NETANDFILE);
        clientManager.setRunning(true);
        Thread cmThread = new Thread(clientManager);
        cmThread.start();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}