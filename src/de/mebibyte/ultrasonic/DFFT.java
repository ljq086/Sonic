package de.mebibyte.Sonic;

/**
 * 
 * Quellen aus Internet, Original von Jon Squire
 * 
 * Schnelle, diskrete Fourier-Transformation.
 */

public class DFFT {

	/**
	 * @param ts
	 *            Zeitreihe, abwechselnd Real- und Imagin�rteil, Funktionswerte
	 *            einer reellen Zeitreihe also bei Index 0, 1, 3, 5 etc.
	 *            eintragen
	 * @return Frequenzspektrum, abwechselnd Real- und Imagin�rteil
	 */
	public static void fft(double ts[]) {
		fft(ts, true);
	}

	public static void ifft(double ts[]) {
		fft(ts, false);
	}

	public static double[] realToComplex(double ts[]) {
		double[] freq = new double[2*ts.length];
		for (int i=0;i<ts.length;++i) freq[2*i]=ts[i];
		return freq;
	}

	private static void fft(double A[], boolean analyse)
	// real,imag real,imag ... trans=1.0 for FFT
	{
		final double trans = (analyse) ? 1.0 : -1.0;
		double tmpr, tmpi;
		double wxr, wxi;
		double wr, wi;
		int i, js, ix, m, isp, mmax;
		double ph1;
		int n = A.length / 2;

		js = 1;
		for (ix = 1; ix < n; ix++) // reorder data
		{
			if (js > ix) {
				tmpr = A[2 * (js - 1)];
				tmpi = A[2 * (js - 1) + 1];
				A[2 * (js - 1)] = A[2 * (ix - 1)];
				A[2 * (js - 1) + 1] = A[2 * (ix - 1) + 1];
				A[2 * (ix - 1)] = tmpr;
				A[2 * (ix - 1) + 1] = tmpi;
			}
			m = n / 2;
			while (m < js && m > 0) {
				js = js - m;
				m = m / 2;
			}
			js = js + m;
		}
		mmax = 1;
		while (mmax < n) // compute transform
		{
			isp = mmax + mmax;
			ph1 = Math.PI * trans / (double) mmax;
			wxr = Math.cos(ph1);
			wxi = Math.sin(ph1);
			wr = 1.0;
			wi = 0.0;
			for (m = 0; m < mmax; m++) {
				ix = m;
				while (ix + mmax < n) {
					js = ix + mmax;
					tmpr = wr * A[2 * js] - wi * A[2 * js + 1];
					tmpi = wr * A[2 * js + 1] + wi * A[2 * js];
					A[2 * js] = A[2 * ix] - tmpr; // BASIC BUTTERFLY
					A[2 * js + 1] = A[2 * ix + 1] - tmpi;
					A[2 * ix] = A[2 * ix] + tmpr;
					A[2 * ix + 1] = A[2 * ix + 1] + tmpi;
					ix = ix + isp;
				}
				tmpr = wr * wxr - wi * wxi;
				tmpi = wr * wxi + wi * wxr;
				wr = tmpr;
				wi = tmpi;
			}
			mmax = isp;
		}
		if (!analyse) // only divide by n on inverse transform
		{
			for (i = 0; i < n; i++) {
				final int ii = 2 * i;
				A[ii] = A[ii] / (double) n;
				A[ii + 1] = A[ii + 1] / (double) n;
			}
		}
	} // end fft

	/**
	 * Faltung zweier (gleich langer) reeller Zeitreihen
	 * 
	 * @param a
	 *            erste Zeitreihe
	 * @param b
	 *            zweite Zeitreihe
	 * @return Faltung (doppelt so lang wie einzelne Zeitreihen)
	 */
	public static double[] fftconv(double[] a, double[] b) {
		int n = a.length;
		int m = b.length;
		if (n != m) {
			System.out.println("fftconv n!=m");
			System.exit(1);
		}
		double c[] = new double[2 * n];
		double AA[] = new double[4 * n];
		double BB[] = new double[4 * n];
		double CC[] = new double[4 * n];
		for (int i = 0; i < n; i++) {
			AA[2 * i] = a[i];
			BB[2 * i] = b[i];
		}

		// Transformation
		fft(AA);
		fft(BB);

		// Produkt im Frequenzspektrum (komplexe Zahlen)
		for (int i = 0; i < 2 * n; i++) {
			final int ii = 2 * i;
			CC[ii] = AA[ii] * BB[ii] - AA[ii + 1] * BB[ii + 1];
			CC[ii + 1] = AA[ii] * BB[ii + 1] + AA[ii + 1] * BB[ii];
		}

		// R�cktransformation
		ifft(CC);

		// Nur reelle Anteile liefern
		for (int i = 0; i < 2 * n; i++) {
			c[i] = CC[2 * i];
		}

		return c;
	} // end fftconv

	public static int fftcrosscorr(double[] a, double[] b) {
		int n = a.length;
		int m = b.length;
		if (n != m) {
			System.out.println("fftconv n!=m");
			System.exit(1);
		}
		double AA[] = new double[4 * n];
		double BB[] = new double[4 * n];
		double CC[] = new double[4 * n];
		for (int i = 0; i < n; i++) {
			AA[2 * i] = a[i];
			BB[2*n - 2 * i] = b[i]; // in der Zeit umdrehen
		}

		// Transformation
		fft(AA);
		fft(BB);

		// Produkt im Frequenzspektrum (komplexe Zahlen)
		for (int i = 0; i < 2 * n; i++) {
			final int ii = 2 * i;
			CC[ii] = AA[ii] * BB[ii] - AA[ii + 1] * BB[ii + 1];
			CC[ii + 1] = AA[ii] * BB[ii + 1] + AA[ii + 1] * BB[ii];
		}

		// R�cktransformation
		ifft(CC);

		// Maximalen reellen Anteile suchen => dort gr�sste (Pearson) Korrelation
		int offset = 0;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < 2*n; i++) 
			if (CC[2*i] > max) { max = CC[2*i]; offset=i; }

		return offset;
	} 

} // end class Cxfft
