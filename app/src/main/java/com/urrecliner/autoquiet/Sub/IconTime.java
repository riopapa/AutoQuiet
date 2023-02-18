package com.urrecliner.autoquiet.Sub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.urrecliner.autoquiet.R;

public class IconTime {
    public Bitmap make(Context context, String string) {
        Paint paint;
        paint = new Paint();
        paint.setTextSize(32);
        paint.setStrokeWidth(8);
        paint.setTypeface(ResourcesCompat.getFont(context, R.font.radioland_regular));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(0xFFFFFFFF);
        int sz = 68;
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888);
        Log.w("aBitmap", bitmap.getWidth()+"x"+bitmap.getHeight());
        canvas.setBitmap(bitmap);
        String hh = string.substring(0,2);
        String mm = string.substring(3);
        canvas.drawText(hh, sz/2, sz/2-3, paint);
        canvas.drawText(mm, sz/2, sz-3, paint);

        return bitmap;
    }
}