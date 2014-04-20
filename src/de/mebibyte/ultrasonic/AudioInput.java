package de.mebibyte.Sonic;

import android.media.*;
import utill.common.Numbers;
import utill.common.array.Array;

import java.util.Arrays;


/**
 * Description missing
 * Author: Till Hoeppner
 */
public class AudioInput implements Runnable {

    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT,
            CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO,
            AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private Array<short[]> chunks = new Array<short[]>(short[].class);

    private AudioRecord record;

    boolean running = true;

    // private int sampleRate;
    private short[] readBuffer;
    // private int dataBufferIndex = 0;

    /**
     * Creates a new AudioInput holding the specified buffer and listening from a new Thread.
     *
     * @param sampleRate Samplerate of the buffer in Hertz.
     * @param readLength Duration of the internal read buffer in seconds
     */
    public AudioInput(int sampleRate, float readLength) {
        // this.sampleRate = sampleRate;
        int readBufferLength = Numbers.nextPot((int) (sampleRate * readLength));
        readBuffer = new short[readBufferLength];
        record = new AudioRecord(AUDIO_SOURCE, sampleRate, CHANNEL_CONFIG, ENCODING, readBufferLength);
    }

    ///**
    // * Copy last `duration` seconds of data
    // */
    //public void copyLast(short[] out) {
    //    if (out.length > dataBufferIndex) {
    //        int write = out.length - dataBufferIndex;
    //        System.arraycopy(dataBuffer, 0, out, 0, write);
    //        int idx = dataBuffer.length - (out.length - write);
    //        System.arraycopy(dataBuffer, idx, out, write, out.length - write);
    //    } else {
    //        System.arraycopy(dataBuffer, dataBufferIndex, out, 0, out.length);
    //    }
    //}

    @Override
    public void run() {
        record.startRecording();
        try {
            // ringbuffer into dataBuffer
            while (running) {
                int read = 0; // record.read(readBuffer, 0, readBuffer.length);
                while (read < readBuffer.length) {
                    read += record.read(readBuffer, read, readBuffer.length - read);
                }
                chunks.add(Arrays.copyOf(readBuffer, readBuffer.length));


                /*if (read > dataBuffer.length - dataBufferIndex) {
                    int write = dataBuffer.length - dataBufferIndex;
                    System.arraycopy(readBuffer, 0, dataBuffer, dataBufferIndex, write);
                    dataBufferIndex = 0; // wrap around
                    write = read - write;
                    System.arraycopy(readBuffer, write, dataBuffer, dataBufferIndex, write);
                } else {
                    System.arraycopy(readBuffer, 0, dataBuffer, dataBufferIndex, read);
                } */
            }
        } finally {
            record.stop();
            record.release();
            record = null;
        }
    }

    public Thread asThread() {
        return new Thread(this);
    }

    public void stop() {
        running = false;
    }

    public Array<short[]> popData() {
        Array<short[]> d = chunks;
        chunks = new Array<short[]>(short[].class);
        return d;
    }

}
