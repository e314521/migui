package com.imgui;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;

public class Replacement {
    void onResumeReplacement(Hooker.MethodCallback callback) {
        Log.i("12321321","onResumeReplacement");
        Activity activity = (Activity) callback.args[0];
        try {
            Class<?> targetClass = activity.getClassLoader().loadClass("com.imgui.ImGuiView");
            targetClass.getConstructor(Activity.class).newInstance(activity);
            callback.backup.invoke(activity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
