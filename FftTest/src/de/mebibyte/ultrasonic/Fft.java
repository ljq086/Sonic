package de.mebibyte.Sonic;

import utill.common.Numbers;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class Fft {

    public static final float FORWARD = (float) (-2F * Math.PI),
                              INVERSE = (float) (2F * Math.PI);

    private static final float LN2 = (float) Math.log(2.0);

    public static void complex(float[] re, float[] im, float mode) {
        int n = re.length;
        int ld = (int) (Math.log(n) / LN2);

        if (!Numbers.isPowerOfTwo(n)) {
            throw new IllegalArgumentException("Length of real input (" + n + ") is not a power of two.");
        }

        int nu = ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        float tre, tim;
        double p, arg, c, s;

        // First phase - calculation
        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++, k++) {
                    p = bitrevRef(k >> nu1, nu);
                    // direct FFT or inverse FFT
                    arg = mode * p / n;
                    c = Math.cos(arg);
                    s = Math.sin(arg);
                    tre = (float) (re[k + n2] * c + im[k + n2] * s);
                    tim = (float) (im[k + n2] * c - re[k + n2] * s);
                    re[k + n2] = re[k] - tre;
                    im[k + n2] = im[k] - tim;
                    re[k] += tre;
                    im[k] += tim;
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 /= 2;
        }

        // Second phase - recombination
        k = 0;
        int r;
        while (k < n) {
            r = bitrevRef(k, nu);
            if (r > k) {
                tre = re[k];
                tim = im[k];
                re[k] = re[r];
                im[k] = im[r];
                re[r] = tre;
                im[r] = tim;
            }
            k++;
        }
    }

    private static int bitrevRef(int j, int nu) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }

}
