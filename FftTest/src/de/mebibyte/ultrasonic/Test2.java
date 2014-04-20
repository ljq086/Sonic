package de.mebibyte.Sonic;

import utill.common.stream.OStream;
import utill.io.FileHandle;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class Test2 {

    public static void main(String[] args) throws IOException {
        int n = 4096;
        double A[] = new double[n];
        double th;

        // A ==  sin 2 th
        for (int i = 0; i < n / 2; i++) {
            th = (2.0 * Math.PI) * (double) i / (double) n;
            A[2 * i] = 4 * Math.sin(620 * th);
            A[2 * i + 1] = 0.0;
        }
        // A transformieren
        DFFT.fft(A);
        for (int i = 0; i < n / 4; i++)
            System.out.printf("cycles = %.6f  amplitude = %.6f  phase = %.6f\n", (2.0 * i),
                    Math.sqrt(A[2 * i] * A[2 * i] + A[2 * i + 1] * A[2 * i + 1]) / n,
                    Math.atan2(A[2 * i + 1], A[2 * i])
            );
        System.out.println(" ");

        FileHandle out = FileHandle.homeHandle("tmp/out");
        OStream o = new OStream(out.asBufferedOutputStream());
        o.writeDoubleArray(A);
        o.close();
    }

}
