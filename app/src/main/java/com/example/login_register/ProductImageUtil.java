package com.example.login_register;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import java.util.HashMap;

public final class ProductImageUtil {
    private static final int TARGET_LONG_EDGE = 128;
    private static final int MIN_OPAQUE_ALPHA = 200;
    private static final HashMap<Integer, ColorMatrixColorFilter> FILTER_CACHE =
            new HashMap<>();

    private ProductImageUtil() {
    }

    public static void apply(ImageView view, int resId) {
        view.setImageResource(resId);
        view.clearColorFilter();
        if (resId == R.drawable.ic_shoe_placeholder) {
            return;
        }

        ColorMatrixColorFilter filter = getFilter(view.getResources(), resId,
                ContextCompat.getColor(view.getContext(), R.color.product_tile));
        if (filter != null) {
            view.setColorFilter(filter);
        }
    }

    private static ColorMatrixColorFilter getFilter(Resources resources, int resId,
                                                     int tileColor) {
        synchronized (FILTER_CACHE) {
            if (FILTER_CACHE.containsKey(resId)) {
                return FILTER_CACHE.get(resId);
            }
            ColorMatrixColorFilter filter = createFilter(resources, resId, tileColor);
            FILTER_CACHE.put(resId, filter);
            return filter;
        }
    }

    private static ColorMatrixColorFilter createFilter(Resources resources, int resId,
                                                        int tileColor) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        bounds.inScaled = false;
        BitmapFactory.decodeResource(resources, resId, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight);
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId, options);
        if (bitmap == null) {
            return null;
        }

        try {
            float[] background = averageCornerColor(bitmap);
            if (background == null) {
                return null;
            }

            // Điều chỉnh sắc độ nền theo màu ô ảnh để ảnh liền mạch với giao diện.
            float redScale = channelScale(Color.red(tileColor), background[0]);
            float greenScale = channelScale(Color.green(tileColor), background[1]);
            float blueScale = channelScale(Color.blue(tileColor), background[2]);
            ColorMatrix matrix = new ColorMatrix(new float[]{
                    redScale, 0, 0, 0, 0,
                    0, greenScale, 0, 0, 0,
                    0, 0, blueScale, 0, 0,
                    0, 0, 0, 1, 0
            });
            return new ColorMatrixColorFilter(matrix);
        } finally {
            bitmap.recycle();
        }
    }

    private static int sampleSize(int width, int height) {
        int sampleSize = 1;
        int longEdge = Math.max(width, height);
        while (longEdge / sampleSize > TARGET_LONG_EDGE) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    private static float[] averageCornerColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int rightStart = Math.max(0, width - 3);
        int bottomStart = Math.max(0, height - 3);
        int[] cornerX = {0, rightStart, 0, rightStart};
        int[] cornerY = {0, 0, bottomStart, bottomStart};
        long redTotal = 0;
        long greenTotal = 0;
        long blueTotal = 0;
        int sampleCount = 0;

        for (int corner = 0; corner < cornerX.length; corner++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    int pixel = bitmap.getPixel(
                            Math.min(width - 1, cornerX[corner] + x),
                            Math.min(height - 1, cornerY[corner] + y));
                    if (Color.alpha(pixel) < MIN_OPAQUE_ALPHA) {
                        continue;
                    }
                    redTotal += Color.red(pixel);
                    greenTotal += Color.green(pixel);
                    blueTotal += Color.blue(pixel);
                    sampleCount++;
                }
            }
        }

        if (sampleCount == 0) {
            return null;
        }
        return new float[]{
                (float) redTotal / sampleCount,
                (float) greenTotal / sampleCount,
                (float) blueTotal / sampleCount
        };
    }

    private static float channelScale(int tileChannel, float backgroundChannel) {
        float scale = tileChannel / Math.max(1f, backgroundChannel);
        return Math.max(0.85f, Math.min(1.25f, scale));
    }
}
