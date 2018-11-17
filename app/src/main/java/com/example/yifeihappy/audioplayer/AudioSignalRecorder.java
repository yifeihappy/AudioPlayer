package com.example.yifeihappy.audioplayer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by yifeihappy on 2018/11/14.
 */

public class AudioSignalRecorder {
    private AudioRecord audioRecord = null;
    private int recBufSize; //每次接收到的数据的长度
    String baseFilePath = null;
    short recBuf[] = null; //每次接收到的数据buffer
    short bigRecBuf[] = null; //存储数据
    int l = 0; // bigRecBuf 数据长度
    private volatile boolean blnRecorder = false;

    public AudioSignalRecorder(){
        recBufSize = AudioRecord.getMinBufferSize(FrequencyPlayer.sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recBuf = new short[recBufSize];
        bigRecBuf = new short[61*FrequencyPlayer.sampleRate*16];

        baseFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        Log.d("folder", baseFilePath);

    }

    public void startRecorder() {
        l = 0;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FrequencyPlayer.sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);
        blnRecorder = true;
        new AudioRecorderThread().start(); //开始记录数据
    }

    public void stopRecorder() {
        blnRecorder = false;
        audioRecord.stop();
        audioRecord.release();
        saveToFile("rawData.txt", bigRecBuf, l);
    }

    private class AudioRecorderThread extends Thread{

        @Override
        public void run() {
            super.run();
            audioRecord.startRecording();

            while (blnRecorder) {
                int line = audioRecord.read(recBuf, 0, recBufSize);
                for(int i=0; i<line; i++){
                    bigRecBuf[l++] = recBuf[i];
                }
            }
        }
    }

    private void saveToFile(String fileName, short[] content, int length) {
        File file = new File(baseFilePath, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "gb2312");
            for(int i=0; i<length; i++) {
                writer.write(String.valueOf(content[i]));
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
