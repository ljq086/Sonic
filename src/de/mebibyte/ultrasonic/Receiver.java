//package de.mebibyte.Sonic;
//
//import android.os.Environment;
//import android.util.Log;
//import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
//import utill.common.Numbers;
//import utill.common.stream.OStream;
//import utill.common.throwable.CantHappenException;
//import utill.io.FileHandle;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static de.mebibyte.Sonic.Config.*;
//
///**
// * Description missing
// * Author: Till Hoeppner
// */
//public class Receiver {
//
//    private static void shortsToFloats(short[] in, float[] out) {
//        for(int i = 0; i < in.length; i++) out[i] = in[i];
//    }
//
//    private static void calculateMagPhase(float[] fftResult, float[] outMagnitude, float[] outPhase) {
//        // http://stackoverflow.com/a/10305676/1768238
//        for (int i = 0; i * 2 < fftResult.length; ++i) {
//            float re = fftResult[i * 2], im = fftResult[i * 2 + 1];
//            outMagnitude[i] = (float) Math.sqrt(re * re + im * im);
//            outPhase[i] = (float) Math.atan2(im, re);
//        }
//    }
//
//    public class Bg implements Runnable {
//
//        // being GC friendly
//        // private FloatFFT_1D markerFft = new FloatFFT_1D((int) (SAMPLE_RATE * MARKER_TIME));
//        private short[] markerWindow = new short[Numbers.nextPot((int) (SAMPLE_RATE * MARKER_TIME))];
//        private float[] markerWindowFloat = new float[markerWindow.length],
//                        markerWindowEmpty = new float[markerWindow.length],
//                        markerMagnitude = new float[markerWindowFloat.length / 2],
//                        markerPhase = new float[markerWindowFloat.length / 2],
//                        markerBinStart = new float[(int) (SAMPLE_RATE * MARKER_TIME)];
//        private float   markerBinLength = 1F / MARKER_TIME;
//
//        // private FloatFFT_1D signalFft = new FloatFFT_1D((int) (SAMPLE_RATE * SIGNAL_TIME));
//        private short[] signalWindow = new short[Numbers.nextPot((int) (SAMPLE_RATE * SIGNAL_TIME))];
//        private float[] signalWindowFloat = new float[signalWindow.length],
//                        signalWindowEmpty = new float[signalWindow.length],
//                        signalMagnitude = new float[signalWindowFloat.length / 2],
//                        signalPhase = new float[signalWindowFloat.length / 2],
//                        signalBinStart = new float[(int) (SAMPLE_RATE * SIGNAL_TIME)];
//        private float   signalBinLength = 1F / SIGNAL_TIME;
//
//
//        public Bg() {
//            int markerWindow = (int) (SAMPLE_RATE * MARKER_TIME);
//            for (int i = 1; i < markerWindow; i++) {
//                markerBinStart[i] = i * markerBinLength;
//            }
//
//            int signalWindow = (int) (SAMPLE_RATE * SIGNAL_TIME);
//            for (int i = 1; i < signalWindow; i++) {
//                signalBinStart[i] = i * signalBinLength;
//            }
//        }
//
//        @Override
//        public void run() {
//            // minimize access calls
//            AudioInput localInput = Receiver.this.input;
//            while (true) {
//                localInput.copyLast(markerWindow);
//                shortsToFloats(markerWindow, markerWindowFloat);
//
//                Arrays.fill(markerWindowEmpty, 0);
//                //markerFft.realForward(markerWindowFloat);
//                Fft.complex(markerWindowFloat, markerWindowEmpty, Fft.FORWARD);
//                calculateMagPhase(markerWindowFloat, markerMagnitude, markerPhase);
//
//                /* FileHandle out = new FileHandle(Environment.getExternalStorageDirectory(), "/in_pcm");
//                OStream ostream = null;
//                try {
//                    ostream = new OStream(out.asBufferedOutputStream());
//                    ostream.writeFloatArray(markerWindowFloat);
//                    ostream.close();
//                } catch (FileNotFoundException e) {
//                    throw new CantHappenException();
//                } catch (IOException e) {
//                    throw new CantHappenException();
//                }
//
//                FileHandle out2 = new FileHandle(Environment.getExternalStorageDirectory(), "/in_mag");
//                OStream ostream2 = null;
//                try {
//                    ostream2 = new OStream(out2.asBufferedOutputStream());
//                    ostream2.writeFloatArray(markerMagnitude);
//                    ostream2.close();
//                } catch (FileNotFoundException e) {
//                    throw new CantHappenException();
//                } catch (IOException e) {
//                    throw new CantHappenException();
//                } */
//
//                FftResult markerResult = new FftResult(markerMagnitude,
//                                                        markerPhase,
//                                                        markerBinStart,
//                                                        SAMPLE_RATE,
//                                                        (int) (SAMPLE_RATE * MARKER_TIME),
//                                                        markerBinLength);
//
//                for (Interest<FftResult> interest: interests) {
//                    interest.satisfy(markerResult);
//                }
//
//                if (markerResult.hasPeak(MARKER_FREQ)) {
//                    try {
//                        Thread.sleep(AFTER_MARKER_WAIT_MS);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    Log.i("Receiver", "Yay, marker peak!");
//
//                    localInput.copyLast(signalWindow);
//                    shortsToFloats(signalWindow, signalWindowFloat);
//
//                    Arrays.fill(signalWindowEmpty, 0);
//                    //signalFft.realForward(signalWindowFloat);
//                    Fft.complex(signalWindowFloat, signalWindowEmpty, Fft.FORWARD);
//                    calculateMagPhase(signalWindowFloat, signalMagnitude, signalPhase);
//
//                    FftResult signalResult = new FftResult(signalMagnitude,
//                                                            signalPhase,
//                                                            signalBinStart,
//                                                            SAMPLE_RATE,
//                                                            (int) (SAMPLE_RATE * SIGNAL_TIME),
//                                                            signalBinLength);
//
//                    // TODO: query signalResult for ON_FREQ and OFF_FREQ and get result
//
//                    boolean on = signalResult.hasPeak(ON_FREQ);
//                    boolean off = signalResult.hasPeak(OFF_FREQ);
//
//                    if (on && off) Log.e("Receiver", "on && off");
//
//                    if (on) {
//                        Log.e("Receiver", "on");
//                    } else {
//                        Log.e("Receiver", "off");
//                    }
//                }
//            }
//        }
//
//        public Thread asThread() {
//            return new Thread(this);
//        }
//
//    }
//
//    public static class FftResult {
//        public final float[] magnitude, phase, binStart;
//        public final int sampleRate, windowSize;
//        public final float binLength;
//        public FftResult(float[] magnitude, float[] phase, float[] binStart,
//                         int sampleRate, int windowSize,
//                         float binLength) {
//            this.magnitude = magnitude;
//            this.phase = phase;
//            this.binStart = binStart;
//            this.sampleRate = sampleRate;
//            this.windowSize = windowSize;
//            this.binLength = binLength;
//        }
//
//        public boolean hasPeak(int freq) {
//            int binIndex = (int) (freq / binLength);
//            return magnitude[binIndex] > TRESHOLD;
//        }
//
//        public float magnitude(int freq) {
//            int binIndex = (int) (freq / binLength);
//            return magnitude[binIndex];
//        }
//    }
//
//    private AudioInput input;
//    private Bg bg;
//
//    private List<Interest<FftResult>> interests = new ArrayList<Interest<FftResult>>();
//
//    public Receiver(final int samplerate) {
//        input = new AudioInput(samplerate, READ_WINDOW_TIME);
//
//        bg = new Bg();
//        bg.asThread().start();
//
//        input.asThread().start();
//    }
//
//    public void register(Interest<FftResult> interest) {
//        interests.add(interest);
//    }
//
//}
