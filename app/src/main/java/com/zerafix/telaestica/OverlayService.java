package com.zerafix.telaestica;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private View chip;

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        showChip();
    }

    private void showChip() {
        if (!android.provider.Settings.canDrawOverlays(this)) return;
        TextView t = new TextView(this);
        t.setText("  TELA ESTICA  •  ACTIVA  ");
        t.setTextColor(Color.WHITE);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(225, 20,22,27));
        bg.setStroke(2, Color.rgb(255,138,0));
        bg.setCornerRadius(40);
        t.setBackground(bg);
        t.setPadding(8,4,8,4);
        t.setOnClickListener(v -> stopSelf());

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            android.os.Build.VERSION.SDK_INT >= 26 ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        p.y = 18;
        try { wm.addView(t, p); chip = t; } catch (Exception ignored) {}
    }

    @Override public void onDestroy() {
        if (chip != null) try { wm.removeView(chip); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
