package com.ashionline.videoclient;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.SyncFailedException;
import java.net.Socket;
import java.net.UnknownHostException;


public class VideoClientActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "VideoServerActivity";
    MediaPlayer mediaPlayer;
    private SurfaceView videoPreview;
    private SurfaceHolder holder;
    private TextView connectionStatusTextView;
    public static final int SERVERPORT = 6775;
    public static String SERVERIP = "192.168.0.25";
    Socket clientSocket;
    private Handler handler = new Handler();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoPreview = (SurfaceView) findViewById(R.id.surface);
        connectionStatusTextView = (TextView) findViewById(R.id.status);
        holder = videoPreview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        connectionStatusTextView.setText("Attempting to connect");

        mediaPlayer = new MediaPlayer();
        Thread thread = new Thread() {
            public void run() {
                try {
                    clientSocket = new Socket(SERVERIP, SERVERPORT);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectionStatusTextView.setText("Connected to server");
                        }
                    });
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.fromSocket(clientSocket);
                                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                                fileDescriptor.sync();

                                mediaPlayer.setDataSource(fileDescriptor);
                                parcelFileDescriptor.close();
                                mediaPlayer.setDisplay(holder);
                                mediaPlayer.prepareAsync();
                                mediaPlayer.start();
                            } catch (SyncFailedException e) {
                                Log.e(TAG, "Error trying to sync file descriptor: " + Log.getStackTraceString(e));
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                Log.e(TAG, "IO exception: " + Log.getStackTraceString(e));
                            } catch (Throwable e) {
                                Log.e(TAG, "Too general exception: " + Log.getStackTraceString(e));
                            }
                        }
                    });

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "UnknownHosteException: " + Log.getStackTraceString(e));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "Another IO exception: " + Log.getStackTraceString(e));
                }
            }
        };
        thread.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}