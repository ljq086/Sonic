package de.mebibyte.Sonic;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.provider.MediaStore;
import utill.common.array.Array;
import utill.common.stream.OStream;
import utill.common.throwable.CantHappenException;
import utill.io.FileHandle;
import utill.threads.TaskExecutor;

import java.io.*;

import static de.mebibyte.Sonic.Config.*;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class Sender {

    private AudioOutput output;
    private AudioCompositor compositor;
    private TaskExecutor taskExecutor;

    public Sender(int sampleRate) {
        int buffersize_bytes = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        buffersize_bytes = nextPot(buffersize_bytes) * 2;

        output = new AudioOutput(sampleRate, buffersize_bytes);
        compositor = new AudioCompositor(sampleRate, 5);

        taskExecutor = new TaskExecutor();
        Thread thread = new Thread(taskExecutor);
        thread.setDaemon(true);
        thread.start();
    }

    public void send(final String s) {
        taskExecutor.submit(new Runnable() {
            @Override
            public void run() {
                byte[] data = {42};
                /*try {
                    data = s.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new CantHappenException();
                } */

                float secLength = data.length * Byte.SIZE * (SIGNAL_TIME / 1000);
                AudioCompositor compositor = new AudioCompositor(SAMPLE_RATE, secLength);

                //compositor.sine(ON_FREQ, Short.MAX_VALUE, 0, 10);

                //compositor.lerpAmplitude(0, 1, 0, 0.25F);
                //compositor.lerpAmplitude(1, 0, 9.9F, 0.25F);

                float lerpTime = 0.0075F;
                float time = 0F;
                for (byte b : data) {
                    for (int i = 0; i < 8; i++) {
                        //compositor.sine(MARKER_FREQ, Short.MAX_VALUE, time, MARKER_TIME);
                        //compositor.lerpAmplitude(0, 1, time, lerpTime);
                        //compositor.lerpAmplitude(1, 0, time + MARKER_TIME - lerpTime, lerpTime);
                        //time += MARKER_TIME;
                        //time += 0.01F;
                        if ((b & (1 << i)) != 0) {
                            compositor.sine(ON_FREQ, Short.MAX_VALUE, time, SIGNAL_TIME);
                            //compositor.lerpAmplitude(0, 1, time, lerpTime);
                            //compositor.lerpAmplitude(1, 0, time + SIGNAL_TIME - lerpTime, lerpTime);
                            time += SIGNAL_TIME;
                            //time += 0.01F;
                        } else {
                            compositor.sine(OFF_FREQ, Short.MAX_VALUE, time, SIGNAL_TIME);
                            //compositor.lerpAmplitude(0, 1, time, lerpTime);
                            //compositor.lerpAmplitude(1, 0, time + SIGNAL_TIME - lerpTime, lerpTime);
                            time += SIGNAL_TIME;
                            //time += 0.01F;
                        }

                        //compositor.sine((ON_FREQ + OFF_FREQ) / 2, 0, time, PAUSE_TIME); // TODO: waste less cycles ;)
                        //time += PAUSE_TIME;
                    }
                }

                //compositor.lerpAmplitude(0, 1, 0, 0.25F);
                //compositor.lerpAmplitude(1, 0, secLength - 0.25F, 0.25F);

                FileHandle out = new FileHandle("/sdcard/pcmout");
                OStream ostream = null;
                try {
                    ostream = new OStream(out.asBufferedOutputStream());
                    ostream.writeShortArray(compositor.getData());
                    ostream.close();
                } catch (FileNotFoundException e) {
                    throw new CantHappenException();
                } catch (IOException e) {
                    throw new CantHappenException();
                }

                try {
                    analyze(compositor.getData());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                output.write(compositor.getData());
            }
        });
    }

    private int nextPot(int i) {
        int pot = Integer.highestOneBit(i);
        if (pot != i) {
            pot *= 2;
        }
        return pot;
    }

    private static void shortsToDoubles(short[] in, double[] out) {
        for (int i = 0; i < in.length; i++) out[i] = in[i];
    }

    public void analyze(short[] data) throws IOException {
        FileHandle f = new FileHandle("/sdcard/fftout");
        DataOutputStream dos = new DataOutputStream(f.asBufferedOutputStream());

        double[] buffer = new double[(int) (data.length)];
        double[] complexbuffer = new double[buffer.length * 2];
        shortsToDoubles(data, buffer);

        for (int i = 0; i < buffer.length; ++i) {
            complexbuffer[2 * i] = buffer[i];
            complexbuffer[2 * i + 1] = 0;
        }

        DFFT.fft(complexbuffer);

        for (int i = 0; i * 2 < complexbuffer.length; ++i) {
            double re = complexbuffer[i * 2], im = complexbuffer[i * 2 + 1];
            double mag = Math.sqrt(re * re + im * im);
            double phase = Math.atan2(im, re);
            dos.writeDouble(mag);
        }

        dos.close();
    }

}
