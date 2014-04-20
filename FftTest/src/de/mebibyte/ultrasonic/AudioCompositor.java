package de.mebibyte.Sonic;

import android.util.Log;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class AudioCompositor {

    private int sampleRate;
    private float length;
    private short[] data;

    public AudioCompositor(int sampleRate, float length) {
        this.sampleRate = sampleRate;
        this.length = length;
        data = new short[(int) (sampleRate * length)];
    }

    public void reset(float start, float length) {
        int startIndex = (int) (start * sampleRate);
        int endIndex = (int) ((start + length) * sampleRate);
        for (int i = startIndex; i < endIndex; i++) {
            data[i] = 0;
        }
    }

    public void reset() {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    public void sine(int freq, int amp, float start, float length) {
        float periodLength = (float) sampleRate / freq;
        int startIndex = (int) (start * sampleRate);
        int endIndex = (int) ((start + length) * sampleRate);
        if (endIndex > data.length) endIndex = data.length;
        for (int phase = startIndex; phase < endIndex; phase++) {
            double angle = 2 * Math.PI * phase / periodLength;
            if (Math.sin(angle) > 0F &&
                    data[phase] + (short) (Math.sin(angle) * amp) < data[phase])
                Log.e("AudioCompositor", "Overflow!");
            data[phase] += (short) (Math.sin(angle) * amp);
        }
    }

    public void lerpAmplitude(float startFactor, int endFactor, float start, float length) {
        int startIndex = (int) (start * sampleRate);
        int endIndex = (int) ((start + length) * sampleRate);
        if (endIndex > data.length) endIndex = data.length;

        float diff = endIndex - startIndex;
        for (int i = startIndex; i < endIndex; i++) {
            float alpha = (i - startIndex) / diff;
            float factor = startFactor + (endFactor - startFactor) * alpha;
            data[i] *= factor;
        }
    }

    public short[] getData() {
        return data;
    }

    public int getIndex(float time) {
        return (int) (time * sampleRate);
    }

}
