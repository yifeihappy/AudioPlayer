package com.example.yifeihappy.audioplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;

public class MainActivity extends AppCompatActivity {


    private Button btnSTART = null;
    private Button btnSTOP = null;
    private TextView txtHz = null;
    private TextView txtSampleRate = null;
    private TextView txtTime = null;
    private FrequencyPlayer freqPlayer = null;
    private AudioSignalRecorder audioSignalRecorder = null;
    private long startTime = 0L;
    private Handler handler = null;
    private int PER_REQ_CODE = 0x0001;
    private String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private volatile boolean blnTime = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            //判断有没有权限
            for(int i = 0; i < PERMISSIONS.length; i++) {
                int flag = ActivityCompat.checkSelfPermission(MainActivity.this, PERMISSIONS[i]);
                if( flag != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{PERMISSIONS[i]},PER_REQ_CODE);
                }
            }
        }

        freqPlayer = new FrequencyPlayer();
        audioSignalRecorder = new AudioSignalRecorder();

        btnSTART = (Button)findViewById(R.id.btnSTART);
        btnSTOP = (Button)findViewById(R.id.btnSTOP);
        txtHz = (TextView)findViewById(R.id.txtHz);
        txtSampleRate = (TextView)findViewById(R.id.txtSampleRate);
        txtSampleRate.setText(String.valueOf(freqPlayer.sampleRate));
        txtHz.setText(String.valueOf(freqPlayer.freqOfTone[0]));
        txtTime = (TextView)findViewById(R.id.txtTime);

        btnSTOP.setEnabled(false);
        handler = new MainHandler();

        btnSTART.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSTOP.setEnabled(true);
                btnSTART.setEnabled(false);
                freqPlayer.play();

                txtTime.setText("0");
                startTime = System.currentTimeMillis();
               audioSignalRecorder.startRecorder(); //开始记录数据
                blnTime = true;
                new TimeThread().start();
            }
        });

        btnSTOP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSTOP.setEnabled(false);
                btnSTART.setEnabled(true);
                freqPlayer.stop();
                blnTime = false  ;
                long lastTime = System.currentTimeMillis();
                txtTime.setText("" + (lastTime - startTime));
                audioSignalRecorder.stopRecorder();
            }
        });
    }
    private class MainHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String t = msg.obj.toString();
            txtTime.setText(t);
            if( (long)msg.obj > 60000) {
                btnSTOP.performClick();
            }
        }
    }
    private class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while(blnTime){
                    Thread.sleep(100);
                    Long curTime = System.currentTimeMillis();//获取当前时间
                    Message msg = new Message();
                    msg.obj = curTime - startTime;
                    handler.sendMessage(msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
