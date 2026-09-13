package com.ronda.mini;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // شفافية النافذة
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // شفافية WebView + JavaScript Bridge
        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
            getBridge().getWebView().setLayerType(
                android.view.View.LAYER_TYPE_HARDWARE, null
            );

            // ⚡ نضيفو JavaScript Interface
            getBridge().getWebView().addJavascriptInterface(
                new OverlayBridge(), "AndroidOverlay"
            );
        }
    }

    // ⚡ هاد الكلاس كيسمح للـ HTML يتواصل مع Java
    public class OverlayBridge {

        @JavascriptInterface
        public void startOverlay() {
            try {
                // نتحققو من الإذن
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        // نطلب الإذن
                        Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                        );
                        startActivity(intent);
                        return;
                    }
                }

                // نشغل الـ Service (بلا foreground باش ما يطلبش notification)
                Intent serviceIntent = new Intent(MainActivity.this, OverlayService.class);
                startService(serviceIntent);

            } catch (Exception e) {
                // نتجاهلو الخطأ باش ما يطيحش التطبيق
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void stopOverlay() {
            try {
                Intent serviceIntent = new Intent(MainActivity.this, OverlayService.class);
                stopService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public boolean hasOverlayPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(MainActivity.this);
            }
            return true;
        }
    }
}