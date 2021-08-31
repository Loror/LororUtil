package com.loror.lororUtil.storage;

import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

public class RecordUtil {
    private MediaRecorder recorder;// 录音
    private final Handler handler;
    private Thread thread;// 监听线程
    private long startTime;// 录音开始时间
    private boolean isRecording;// 正在录音
    private String path;// 录音存储路径

    /**
     * 音量监听接口
     */
    public interface OnVolumeListener {
        void onVolume(double amplitude);
    }

    /**
     * 初始化
     */
    public RecordUtil() {
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取录音存储路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取当前录音音量
     */
    public double getAmplitude() throws Exception {
        if (isRecording) {
            return (recorder.getMaxAmplitude() / 2700.0);
        } else {
            return 0;
        }
    }

    /**
     * 开始录音
     */
    public void start(String path, final OnVolumeListener listenser) {
        if (isRecording) {
            throw new IllegalStateException("you must call stop before you call another start");
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(196608);
        recorder.setOutputFile(this.path = path);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("unexpected path");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new IllegalStateException("may lost permission");
        }
        recorder.start();
        startTime = System.currentTimeMillis();
        isRecording = true;
        if (listenser != null) {
            thread = new Thread() {
                public void run() {
                    while (isRecording) {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    listenser.onVolume(getAmplitude());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }

                ;
            };
            thread.start();
        }
    }

    /**
     * 停止录音
     */
    public long stop() {
        if (!isRecording || recorder == null)
            return 0;
        isRecording = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            recorder.stop();
            recorder.reset();
            recorder.release();
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
