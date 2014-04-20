package de.mebibyte.Sonic;

import android.content.Loader;
import android.media.AsyncPlayer;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Warning: Not threadsafe at all...
 * Author: Till Hoeppner
 */
public class AudioOutput {

    public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC,
                            CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO,
                            ENCODING = AudioFormat.ENCODING_PCM_16BIT,
                            MODE = AudioTrack.MODE_STREAM;

    private AudioTrack track;

    public AudioOutput(int samplerate, int buffersize_bytes) {
        track = new AudioTrack(STREAM_TYPE, samplerate, CHANNEL_CONFIG, ENCODING, buffersize_bytes, MODE);

        track.setStereoVolume(1F, 1F);
        track.play();
    }

    public void write(short[] data) {
        track.write(data, 0, data.length);
    }

    public void writeSine(float freq, float length, int dummy) {
        int samplerate = track.getSampleRate();
        float period = samplerate / freq;
        final short[] data = new short[(int) (samplerate * length)];
        for (int phase = 0; phase < data.length; phase++) {
            double angle = 2 * Math.PI * phase / period;
            data[phase] = (short) (Math.sin(angle) * Short.MAX_VALUE);
        }

        track.write(data, 0, data.length);
    }

}
