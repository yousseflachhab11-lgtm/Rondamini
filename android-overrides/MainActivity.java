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

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
            getBridge().getWebView().setLayerType(
                android.view.View.LAYER_TYPE_HARDWARE, null
            );

            getBridge().getWebView().addJavascriptInterface(
                new OverlayBridge(), "AndroidOverlay"
            );
        }
    }

    public class OverlayBridge {

        @JavascriptInterface
        public void startOverlay() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        Intent intent = new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName())
                        );
                        startActivity(intent);
                        return;
                    }
                }
                Intent serviceIntent = new Intent(MainActivity.this, OverlayService.class);
                startService(serviceIntent);
            } catch (Exception e) {
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

        @JavascriptInterface
        public void setTouchable(final boolean touchable) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OverlayService.updateTouchable(touchable);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}