package de.mebibyte.Sonic;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class Config {

    public final static int SAMPLE_RATE = 44100,
                            ON_FREQ = 17000,
                            OFF_FREQ = 19000,
                            MARKER_FREQ = 16000,
                            AFTER_MARKER_WAIT_MS = 40,
                            TRESHOLD = 500000;

    public final static float SIGNAL_TIME = 0.05F,
                              PAUSE_TIME = 0.03F,
                              READ_WINDOW_TIME = 1,
                              WINDOW_TIME = 10, // 10 * 44100 * 2 = 861 Kb is okay
                              MARKER_TIME = 0.02F;

}
