package com.ronda.mini;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private WebView webView;
    private WindowManager.LayoutParams params;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ⚡ نحولو dp لـ px
    private int dpToPx(int dp) {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // ⚡ الحجم بالـ dp
        int widthPx = dpToPx(300);
        int heightPx = dpToPx(370);

        // ⚡ نحسبو عرض وطول الشاشة
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // ⚡ الإعدادات — هادي المهمة!
        // FLAG_NOT_FOCUSABLE: ما كياخدش focus (اللعبة تبقى شغالة)
        // FLAG_NOT_TOUCH_MODAL: اللمس برا الـ WebView كيدوز
        // FLAG_WATCH_OUTSIDE_TOUCH: كيراقب اللمس برا
        // FLAG_LAYOUT_NO_LIMITS: كيسمح بالحركة بحرية
        params = new WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        // ⚡ النافذة على اليمين
        params.x = screenWidth - widthPx - dpToPx(10);
        params.y = dpToPx(50);

        // الـ WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // ⚡ نخليو الـ WebView يتعامل مع اللمس فقط داخل الحدود
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // إلا كان اللمس فالحدود ديال الـ WebView — نتقبلو
                // إلا كان برا — نمرروه للـ Android
                float x = event.getX();
                float y = event.getY();
                
                // إلا كان اللمس داخل النافذة
                if (x >= 0 && x <= v.getWidth() && y >= 0 && y <= v.getHeight()) {
                    return false;  // WebView كيتعامل معاه
                }
                
                // إلا كان برا — نمرروه للـ Android
                return false;
            }
        });

        webView.loadUrl("file:///android_asset/public/index.html");

        windowManager.addView(webView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null && windowManager != null) {
            try {
                windowManager.removeView(webView);
            } catch (Exception e) {
                // تجاهل
            }
        }
    }
}