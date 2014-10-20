package com.ashionline.video_service;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VideoServiceActivity extends Activity {
    private static final String TAG = "VideoServiceActivity";
    // User Interface Elements
    VideoView videoView;
    TextView connectionStatus;
    SurfaceHolder holder;
    // Video variable
    MediaRecorder mediaRecorder;
    // Networking variables
    public static String SERVERIP = "";
    public static final int SERVERPORT = 6775;
    private Handler handler = new Handler();
    private ServerSocket serverSocket;
    private String error = "";

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Define UI elements
        videoView = (VideoView) findViewById(R.id.video);
        connectionStatus = (TextView) findViewById(R.id.status);
        holder = videoView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        SERVERIP = "192.168.0.25";
        // Run new thread to handle socket communications
        Thread sendVideo = new Thread(new SendVideoThread());
        sendVideo.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public class SendVideoThread implements Runnable {
        public void run() {
            // From Server.java
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectionStatus.setText("Listening on IP: " + SERVERIP);
                            Log.d(TAG, "Listening on IP: " + SERVERIP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        //listen for incoming clients
                        Socket client = serverSocket.accept();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectionStatus.setText("Connected.");
                                Log.d(TAG, "Connected");
                            }
                        });
                        try {
                            // Begin video communication
                            final ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(client);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mediaRecorder = new MediaRecorder();
                                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                    mediaRecorder.setOutputFile(pfd.getFileDescriptor());
                                    mediaRecorder.setVideoFrameRate(20);
                                    mediaRecorder.setVideoSize(176,144);
                                    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
                                    mediaRecorder.setPreviewDisplay(holder.getSurface());
                                    try {
                                        mediaRecorder.prepare();
                                    } catch (IllegalStateException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    mediaRecorder.start();
                                }
                            });
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    error = "Oops.Connection interrupted. Please reconnect your phones.";
                                    connectionStatus.setText(error);
                                    Log.e(TAG, error);
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            error = "Couldn't detect internet connection.";
                            connectionStatus.setText(error);
                            Log.e(TAG, error);
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        connectionStatus.setText(error);
                    }
                });
                e.printStackTrace();
            }
            // End from server.java
        }
    }
}