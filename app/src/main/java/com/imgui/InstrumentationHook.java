package com.imgui;

import android.app.Activity;
import android.app.Instrumentation;
import android.util.Log;

class HookInstrumentation extends Instrumentation {
    private Instrumentation original;

    public HookInstrumentation(Instrumentation original) {
        this.original = original;
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        Log.d("Hook", "Before onResume: " + activity.getClass().getSimpleName());
        super.callActivityOnResume(activity); // 调用原始逻辑
        Log.d("Hook", "After onResume: " + activity.getClass().getSimpleName());
    }
}

// 替换 Application 的 mInstrumentation
