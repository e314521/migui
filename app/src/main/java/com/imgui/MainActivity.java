package com.imgui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import com.imgui.databinding.ActivityMainBinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;


@SuppressLint("DiscouragedPrivateApi")
public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("imgui");
    }

    // Used to load the 'imgui' library on application startup.
    private ActivityMainBinding binding;
    static class ProxyInstrumentation extends Instrumentation {
        private final Instrumentation original;

        public ProxyInstrumentation(Instrumentation original) {
            this.original = original;
        }

        @Override
        public void callActivityOnResume(Activity activity) {
            // 3. 拦截onResume逻辑
            Log.d("HOOK", "Before onResume: " + activity.getClass().getSimpleName());
            original.callActivityOnResume(activity);
            Log.d("HOOK", "After onResume: " + activity.getClass().getSimpleName());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new ImGuiView(this);

    }

    /**
     * A native method that is implemented by the 'imgui' native library,
     * which is packaged with this application.
     */

}