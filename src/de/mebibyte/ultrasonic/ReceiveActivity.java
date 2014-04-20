package de.mebibyte.Sonic;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import utill.common.ArrayUtil;
import utill.common.array.Array;
import utill.common.array.ByteArray;
import utill.common.throwable.CantHappenException;
import utill.io.FileHandle;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static de.mebibyte.Sonic.Config.*;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class ReceiveActivity extends Activity {

    private Button recordButton;
    private TextView resultText;
    private CheckBox saveFft;

    private AudioInput input;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.receive);

        recordButton = (Button) findViewById(R.id.button_record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input == null) {
                    input = new AudioInput(SAMPLE_RATE, READ_WINDOW_TIME);
                    input.asThread().start();
                } else {
                    input.stop();
                    try {
                        Array<short[]> data = input.popData();
                        write(data);
                        analyze(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    input = null;
                }
            }
        });

        resultText = (TextView) findViewById(R.id.textview_result);
        saveFft = (CheckBox) findViewById(R.id.checkbox_save_fft);
    }

    public void write(Array<short[]> data) throws IOException {
        FileHandle out = new FileHandle("/sdcard/pcmin");
        DataOutputStream os = new DataOutputStream(out.asBufferedOutputStream());
        for (short[] sa : data.trimmedArray()) {
            for (short s : sa) {
                os.writeShort(s);
            }
        }
        os.close();
    }

    private static void shortsToDoubles(short[] in, double[] out, int start, int count) {
        for (int i = 0, j = start; i < count; i++, j++) out[i] = in[j];
    }

    private static void calculateMagPhase(double[] fftResult, double[] outMagnitude, double[] outPhase) {
        // http://stackoverflow.com/a/10305676/1768238
        for (int i = 0; i * 2 < fftResult.length; ++i) {
            double re = fftResult[i * 2], im = fftResult[i * 2 + 1];
            outMagnitude[i] = (float) Math.sqrt(re * re + im * im);
            outPhase[i] = (float) Math.atan2(im, re);
        }
    }

    // TODO: Relativer Schwellwert, Biterkennung
    public void analyze(Array<short[]> data) throws IOException {
        boolean save = saveFft.isChecked();
        FileHandle f = null;
        PrintStream dos = null;
        if (save) {
            f = new FileHandle("/sdcard/fftin" + System.currentTimeMillis());
            dos = new PrintStream(f.asBufferedOutputStream());
        }

        ByteArray output = new ByteArray();
        byte outputBuffer = 0;
        int outputBufferIndex = 0;

        short[] flat = ArrayUtil.join(data.trimmedArray());
        Log.e("Flat length", Integer.toString(flat.length));

        float totalTime = flat.length / SAMPLE_RATE;

        float binlength = 1F / WINDOW_TIME;
        int binIndexOn = (int) (ON_FREQ / binlength);
        int binIndexOff = (int) (OFF_FREQ / binlength);
        Log.e("binIndex", "On: " + binIndexOn + ", Off:" + binIndexOff);

        StringBuilder onString = new StringBuilder(), offString = new StringBuilder();

        int onCount = 0, offCount = 0;
        int width = 2;

        double[] buffer = new double[(int) (SAMPLE_RATE * WINDOW_TIME)];
        double[] complexbuffer = new double[buffer.length * 2];

        int windows = (int) (totalTime / WINDOW_SHIFT) + 1;
        double[] onMag = new double[windows], offMag = new double[windows];

        int firstReq = -1;

        int window = 0;
        for (float time = 0F; time < totalTime; time += WINDOW_SHIFT, window++) {
            shortsToDoubles(flat, buffer, (int) (time * SAMPLE_RATE), buffer.length);

            for (int i = 0; i < buffer.length; ++i) {
                complexbuffer[2 * i] = buffer[i];
                complexbuffer[2 * i + 1] = 0;
            }

            DFFT.fft(complexbuffer);

            double currOnMag = 0;
            for (int i = -width; i <= width; i++) {
                double onRe = complexbuffer[(binIndexOn + i) * 2], onIm = complexbuffer[(binIndexOn + i) * 2 + 1];
                currOnMag += Math.sqrt(onRe * onRe + onIm * onIm);
            }
            onMag[window] = currOnMag;

            double currOffMag = 0;
            for (int i = -width; i <= width; i++) {
                double offRe = complexbuffer[(binIndexOff + i) * 2], offIm = complexbuffer[(binIndexOff + i) * 2 + 1];
                currOffMag += Math.sqrt(offRe * offRe + offIm * offIm);
            }
            offMag[window] = currOffMag;

            if (save) {
                for (int i = 0; i * 2 < complexbuffer.length; ++i) {
                    double re = complexbuffer[i * 2], im = complexbuffer[i * 2 + 1];
                    double mag = Math.sqrt(re * re + im * im);

                    dos.print((int) (time * 1000));
                    dos.print(" ");
                    dos.print(i);
                    dos.print(" ");
                    dos.print(mag);
                    dos.println();
                }
                dos.println();
            }

        }

        double maxOnMag = ArrayUtil.max(onMag), maxOffMag = ArrayUtil.max(offMag);
        double treshold = Math.max(maxOnMag, maxOffMag) * 0.45;

        for (window = 0; window < windows; window++) {
            boolean on = onMag[window] > treshold;
            boolean off = offMag[window] > treshold;

            if (on) onCount++;
            if (off) offCount++;

            if ((on || off) && (firstReq == -1)) {
                firstReq = window - 1;
                Log.e("Debug", "firstReq=" + firstReq);
            }

            int windowsPerSignal = (int) (SIGNAL_TIME / WINDOW_SHIFT);
            if (firstReq > -1 && (window - firstReq) % windowsPerSignal == 0) {
                if (outputBufferIndex == 8) {
                    output.add(outputBuffer);
                    outputBufferIndex = outputBuffer = 0;
                }
                if ((onCount > offCount) && (onCount + offCount > windowsPerSignal / 2)) {
                    outputBuffer |= 1 << outputBufferIndex;
                }
                Log.e("Debug", "@" + window + "  on=" + onCount + "  " + "off=" + offCount + "  " + Integer.toBinaryString(outputBuffer));
                outputBufferIndex++;
                onCount = offCount = 0;
            }
        }

        output.add(outputBuffer);

        Log.e("ON Log ", "…" + onString.toString());
        Log.e("OFF Log", offString.toString());

        resultText.setText(Arrays.toString(output.trimmedArray()) + " => "
                + new String(output.trimmedArray(), "UTF-8"));

        if (save) dos.close();
    }


//for (int i: new int[]{binIndexOn, binIndexOff}/*int i = 0; i * 2 < complexbuffer.length; ++i*/) {
            /*    double re = complexbuffer[i * 2], im = complexbuffer[i * 2 + 1];
                double mag = Math.sqrt(re * re + im * im);
                double phase = Math.atan2(im, re);

                if (i == binIndexOn && mag > TRESHOLD) {
                    bit = Bit.ON; Log.e("Req", "OnMag: " + mag);
                }
                if (i == binIndexOff && mag > TRESHOLD) {
                    bit = Bit.OFF; Log.e("Req", "OffMag: " + mag);
                }

                //dos.printf("%d %d %f%n", (int) (time * 1000), i, mag);
            } */
//dos.println();

}