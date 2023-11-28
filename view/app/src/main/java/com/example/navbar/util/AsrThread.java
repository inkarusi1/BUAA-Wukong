package com.example.navbar.util;

import static android.media.AudioRecord.getMinBufferSize;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import com.ubtrobot.speech.SpeechApiExtra;
import com.ubtrobot.speech.asr.AsrRequest;

import java.util.Arrays;


public class AsrThread extends Thread {
    private static final String TAG = "InAsrThread";
    private final Context context;
    private boolean listening;
    private final Speaker speaker;
    private final SpeechApiExtra speechApiExtra;
    private final int minBufferSize = getMinBufferSize(16000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord audioRecord;
    private volatile byte[] buffer;
    private volatile byte[] finalBuffer;
    private static String result;
    private boolean running;
    private EditText inputText;  // 来自ChatFragment

    public AsrThread(Context context1, EditText editText) {
        this.context = context1;
        this.listening = true;
        this.speaker = new Speaker();
        speechApiExtra = SpeechApiExtra.get();
        this.finalBuffer = new byte[0];
        this.running = false;
        this.inputText = editText;
    }

    /*
    * 使用方法：
    * asrThread.start(); asrThread.endThread(); myString = AsrThread.getResult(); */
    public void run() {
        Log.i(TAG, "xxxxxx result: [" + result + "]");
        getSay();
        Log.i(TAG, "xxxxxx result: [" + result + "][" + finalBuffer.length + "]");
        speaker.speak("让本大爷来说：" + result);
//        inputText.setText(result);
        Log.i(TAG, "asrThread end!");
//        while (listening) {
//            running = !running;
//        }
//        result += result;
    }

    public void endThread() {
        listening = false;
        running = false;
    }

    public static String getResult() {
        return result;
    }

    private AudioRecord createRecorder() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "audio record permission denied!!!???");
            return null;
        }
        return new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
    }

    private void getSay() {
        audioRecord = createRecorder();
        assert audioRecord != null;
        audioRecord.startRecording();
        buffer = new byte[minBufferSize];
        int readResult;

        while (listening && audioRecord != null) {
            if ((readResult = audioRecord.read(buffer, 0, minBufferSize)) < AudioRecord.SUCCESS) {
                Log.e("AudioHandler", "read data error!" + readResult + ", " + buffer.length);
                audioRecord.release();
                audioRecord = null;
            } else {
                finalBuffer = concatenateByteArrays(finalBuffer, buffer);
                Log.i(TAG, "listening........................");
            }
        }
        assert audioRecord != null;
        audioRecord.stop();
        audioRecord.release();
        speechApiExtra.beginAsrSession(AsrRequest.SampleRate.Rate16K, 2);
        speechApiExtra.asr(finalBuffer);
        result = speechApiExtra.endAsrSession();
    }

    private static byte[] concatenateByteArrays(byte[] array1, byte[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;

        // 创建一个新的数组，长度为两个数组的总长度
        byte[] result = Arrays.copyOf(array1, length1 + length2);

        // 将第二个数组复制到新数组中
        System.arraycopy(array2, 0, result, length1, length2);

        return result;
    }

}
