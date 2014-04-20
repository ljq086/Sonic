//package de.mebibyte.Sonic;
//
//import android.util.Log;
//import utill.common.array.ByteArray;
//
///**
// * Description missing
// * Author: Till Hoeppner
// */
//public class BinaryConverter {
//
//    private static final String TAG = BinaryConverter.class.getName();
//
//    private ByteArray data = new ByteArray();
//    private byte buffer;
//    private int bufferIndex;
//
//    public void onFftAvailable(Receiver.FftResult result) {
//        int onBinIndex = (int) (Config.ON_FREQ / result.binLength);
//        int offBinIndex = (int) (Config.OFF_FREQ / result.binLength);
//
//        float onMag = result.magnitude[onBinIndex];
//        float offMag = result.magnitude[offBinIndex];
//
//        boolean on = onMag > Config.TRESHOLD;
//        boolean off = offMag > Config.TRESHOLD;
//
//        if (on && off) Log.e(TAG, "on && off");
//
//        if (on) buffer |= 1 << bufferIndex;
//        if (on || off) {
//            bufferIndex++;
//            if (bufferIndex == 8) {
//                bufferIndex = 0;
//                data.add(buffer);
//                Log.e(TAG, "received: " + buffer);
//            }
//        }
//    }
//}
