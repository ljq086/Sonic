//package de.mebibyte.Sonic;
//
//import android.content.Context;
//import android.graphics.*;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.View;
//import utill.common.ArrayUtil;
//import utill.common.Numbers;
//
///**
// * Description missing
// * Author: Till Hoeppner
// */
//public class FFTView extends View implements Interest<Receiver.FftResult> {
//
//    private Paint paint;
//    private Path path;
//    private Bitmap screen, offscreen;
//    private Canvas canvas, offcanvas;
//
//    private boolean initialized;
//
//    public FFTView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public FFTView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public FFTView(Context context) {
//        super(context);
//    }
//
//    private void init() {
//        if (initialized || getWidth() <= 0 || getHeight() <= 0) return;
//        initialized = true;
//
//        paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setStrokeWidth(20);
//        paint.setColor(CodeColors.HOLO_BLUE_LIGHT);
//
//        path = new Path();
//
//        screen = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
//        offscreen = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
//        canvas = new Canvas(screen);
//        offcanvas = new Canvas(offscreen);
//    }
//
//    @Override
//    public void draw(Canvas canvas) {
//        synchronized (this) {
//            if (initialized) {
//                canvas.drawBitmap(screen, 0, 0, null);
//            }
//        }
//    }
//
//    @Override
//    public void satisfy(Receiver.FftResult fftResult) {
//        float[] magnitude = fftResult.magnitude;
//        synchronized (this) {
//            init();
//            if (!initialized) return;
//            path.rewind();
//
//            int w = getWidth(), h = getHeight();
//
//            path.moveTo(0, 0);
//            int step = w / magnitude.length;
//            if (step < 1) step = 1;
//
//            float max = 1E5F;
//            float scaleY = h / max;
//            for (int x = 0; x < magnitude.length; x += step) {
//                path.lineTo(x, Numbers.clamp(magnitude[x] * scaleY, 0, h));
//            }
//            path.lineTo(w, 0);
//
//            offcanvas.drawColor(0, PorterDuff.Mode.CLEAR);
//            offcanvas.save();
//            offcanvas.translate(0, h);
//            offcanvas.scale(1, -1);
//            offcanvas.drawPath(path, paint);
//            offcanvas.restore();
//
//            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
//            canvas.drawBitmap(offscreen, 0, 0, null);
//
//            postInvalidate();
//        }
//    }
//
//}
