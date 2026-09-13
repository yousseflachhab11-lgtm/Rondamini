package com.ronda.mini;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
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
    
    // ⚡ الأبعاد
    private int screenWidth;
    private int screenHeight;
    private int overlayWidth;
    private int overlayHeight;
    private int overlayX;
    private int overlayY;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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

        // ⚡ نحسبو الشاشة
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        // ⚡ الحجم
        overlayWidth = dpToPx(300);
        overlayHeight = dpToPx(370);

        // ⚡ الموضع (اليمين)
        overlayX = screenWidth - overlayWidth - dpToPx(10);
        overlayY = dpToPx(50);

        // ⚡ الإعدادات — FLAG_NOT_TOUCH_MODAL مهمة
        params = new WindowManager.LayoutParams(
            overlayWidth,
            overlayHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = overlayX;
        params.y = overlayY;

        // ⚡ الـ WebView
        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // ⚡ ⚡ ⚡ اللمس — MotionEvent Interception
        // هاد الـ Listener كيتفعل قبل ما يوصل اللمس للـ WebView
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // ⚡ نجيبو إحداثيات اللمس
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                
                // ⚡ نتحققو واش اللمس داخل منطقة Rondacalcul
                boolean insideOverlay = 
                    rawX >= overlayX && 
                    rawX <= overlayX + overlayWidth &&
                    rawY >= overlayY && 
                    rawY <= overlayY + overlayHeight;
                
                if (insideOverlay) {
                    // ✅ داخل Rondacalcul — WebView كيتعامل معاه
                    return false;
                } else {
                    // ❌ برا Rondacalcul — نمرروه للـ Android
                    // نرجعو true باش ناكلو الحدث (باش ما يوصلش للـ WebView)
                    // الـ FLAG_NOT_TOUCH_MODAL كيخلي اللمس يدوز للتطبيق اللي تحت
                    return true;
                }
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