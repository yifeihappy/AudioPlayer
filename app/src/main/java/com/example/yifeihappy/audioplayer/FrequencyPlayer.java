package com.example.yifeihappy.audioplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by yifeihappy on 2018/11/14.
 */

public class FrequencyPlayer {
    private AudioTrack audioTrack;

    public static final int sampleRate = 44100; //采样率
    private int numSamples = 120960; //总共的采样点数,乘以频率，正好是采样率的倍数
    private int numfreq = 1; //使用的超声波频率种类数量
    public double freqOfTone[] = {17500, 18200, 18900, 19600, 20300}; //超声波频率的种类
    private double samples[] = new double[numSamples]; //记录采样点的数值

    private byte generatedSound[] = new byte[2 * numSamples]; //

    public FrequencyPlayer(){
        originalSignal();
    }

    private void originalSignal() {
        //fill out the array
        for(int i = 0; i< numSamples; i++){
            samples[i] = 0;
            for(int j = 0; j < numfreq; j++){
                samples[i] += Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone[j]));
            }
            samples[i] = samples[i] / numfreq;
        }

        //convert to 16 bit pcm sound array
        //assumes the sample buffer is normalised
        int idx = 0;
        for( final double v : samples) {
            final short val = (short) ((v * 32767)); // Java short: -32768 至 32767
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[idx++] = (byte) (val & 0x00ff);
            generatedSound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    public void play(){
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSound.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSound, 0, generatedSound.length);
        audioTrack.setLoopPoints(0, generatedSound.length / 2, -1); //可能是因为PCM bit 16
        audioTrack.play();
    }

    public void stop(){
        audioTrack.stop();
        audioTrack.release();
    }




}
