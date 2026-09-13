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
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private WebView webView;
    private WindowManager.LayoutParams params;

    // ⚡ ⚡ ⚡ static reference — باش نتحكمو من MainActivity
    private static OverlayService instance;

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

    // ⚡ ⚡ ⚡ دالة static للتحكم — يستدعيها MainActivity
    public static void updateTouchable(boolean touchable) {
        if (instance == null) return;
        if (instance.params == null) return;
        if (instance.windowManager == null) return;
        if (instance.webView == null) return;

        if (touchable) {
            instance.params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            instance.params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }

        try {
            instance.windowManager.updateViewLayout(instance.webView, instance.params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int widthPx = dpToPx(300);
        int heightPx = dpToPx(370);

        params = new WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dpToPx(10);
        params.y = dpToPx(50);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        webView.loadUrl("file:///android_asset/public/index.html");

        windowManager.addView(webView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        if (webView != null && windowManager != null) {
            try {
                windowManager.removeView(webView);
            } catch (Exception e) {
                // تجاهل
            }
        }
    }
}