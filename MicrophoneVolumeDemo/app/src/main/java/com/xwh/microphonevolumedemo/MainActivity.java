package com.xwh.microphonevolumedemo;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;


public class MainActivity extends Activity {

    private Button recordBtn, stopBtn;
    private TextView volomeTv;

    private boolean isRecording;

    private int volomeForShow;

    private MediaRecorder mediaRecorder;

    private RefreshVolomeTvTh refreshVolomeTvTh;
    private VolomeHandler volomeHandler;
    private ProgressBar volumePb;

    private final int REFRESH_VOLOME = 0x01;

    private final String FILE = Environment.getExternalStorageDirectory() + File.separator + "volomeDemo.aac";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
    }

    // Init the UI elements
    private void findView() {
        recordBtn = (Button) findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(FILE);
                if (file.exists() || !file.isDirectory()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mediaRecorder == null) {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setOutputFile(FILE);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                }

                if (volomeHandler == null) {
                    volomeHandler = new VolomeHandler();
                }

                if (!isRecording) {
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                        isRecording = true;
                        refreshVolomeTvTh = new RefreshVolomeTvTh();
                        refreshVolomeTvTh.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    isRecording = false;
                }

            }
        });
        stopBtn = (Button) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaRecorder == null) {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setOutputFile(FILE);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                }
                if (isRecording) {
                    isRecording = false;
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder = null;
                }
            }
        });
        volomeTv = (TextView) findViewById(R.id.volome_tv);
        volumePb = (ProgressBar) findViewById(R.id.volume_pb);
        volumePb.setMax(32767);
    }

    private class VolomeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_VOLOME:
                    volomeTv.setText((8 * volomeForShow / 32768) + " ----- " + volomeForShow);
                    volumePb.setProgress(volomeForShow);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(FILE);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }
        if (isRecording) {
            isRecording = false;
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    //Refresh the volome textview
    private class RefreshVolomeTvTh extends Thread {

        @Override
        public void run() {
            super.run();
            while (isRecording) {
                try {
                    volomeForShow = mediaRecorder.getMaxAmplitude();
                    volomeHandler.sendEmptyMessage(REFRESH_VOLOME);
                    sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
