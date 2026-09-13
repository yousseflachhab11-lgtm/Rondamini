package com.ronda.mini;

import android.graphics.Color;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // خلي خلفية الـ Window شفافة
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // خلي خلفية الـ WebView شفافة
        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
            getBridge().getWebView().setLayerType(
                android.view.View.LAYER_TYPE_HARDWARE, null
            );
        }
    }
}