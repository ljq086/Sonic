package de.mebibyte.Sonic;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import utill.common.Numbers;
import utill.common.stream.OStream;
import utill.io.FileHandle;

import java.io.IOException;

import static de.mebibyte.Sonic.Config.TRESHOLD;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class FftTest {

    public static class FftResult {
        public final float[] magnitude, phase, binStart;
        public final int sampleRate, windowSize;
        public final float binLength;

        public FftResult(float[] magnitude, float[] phase, float[] binStart,
                         int sampleRate, int windowSize,
                         float binLength) {
            this.magnitude = magnitude;
            this.phase = phase;
            this.binStart = binStart;
            this.sampleRate = sampleRate;
            this.windowSize = windowSize;
            this.binLength = binLength;
        }

        public boolean hasPeak(int freq) {
            int binIndex = (int) (freq / binLength);
            return magnitude[binIndex] > TRESHOLD;
        }

        public float magnitude(int freq) {
            int binIndex = (int) (freq / binLength);
            return magnitude[binIndex];
        }
    }

    private static void shortsToFloats(short[] in, float[] out) {
        for (int i = 0; i < in.length; i++) out[i] = in[i];
    }

    private static void shortsToDoubles(short[] in, double[] out) {
        for (int i = 0; i < in.length; i++) out[i] = in[i];
    }

    private static void calculateMagPhase(double[] fftResult, double[] outMagnitude, double[] outPhase) {
        // http://stackoverflow.com/a/10305676/1768238
        for (int i = 0; i * 2 < fftResult.length; ++i) {
            double re = fftResult[i * 2], im = fftResult[i * 2 + 1];
            outMagnitude[i] = (float) Math.sqrt(re * re + im * im);
            outPhase[i] = (float) Math.atan2(im, re);
        }
    }

    public static void main(String[] args) throws IOException {
        AudioCompositor compositor = new AudioCompositor(44100, 1);
        compositor.sine(19000, Short.MAX_VALUE / 2, 0, 1);
        short[] data = compositor.getData();

        double[] re = new double[Numbers.nextPot(data.length)];
        shortsToDoubles(data, re);

        //float[] re = new float[Numbers.nextPot(data.length)], im = new float[Numbers.nextPot(data.length)];
        //shortsToFloats(data, re);

        //Fft.complex(re, im, Fft.FORWARD);

        //FloatFFT_1D fft = new FloatFFT_1D((int) (44100 * 1/*s*/));
        //fft.complexForward(re);

        DFFT.fft(DFFT.realToComplex(re));

        double[] mag = new double[data.length], phase = new double[data.length];

        calculateMagPhase(re, mag, phase);

        int window = (int) (44100 * 1/*s*/);
        float[] binstart = new float[window];
        for (int i = 1; i < window; i++) {
            binstart[i] = i * 1 / 1/*s*/;
        }

        // FftResult result = new FftResult(mag, phase, binstart, 44100, 44100 * 1/*s*/, 1 / 1/*s*/);

        // System.out.println(result.hasPeak(21000));

        FileHandle out = FileHandle.homeHandle("tmp/out");
        OStream o = new OStream(out.asBufferedOutputStream());
        o.writeDoubleArray(mag);
        o.close();
    }

}
