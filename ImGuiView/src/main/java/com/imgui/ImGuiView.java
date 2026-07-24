package com.imgui;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.os.Process;
import android.widget.FrameLayout;

import static android.view.WindowManager.LayoutParams;
import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.graphics.PixelFormat.TRANSPARENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

public class ImGuiView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private final static String TAG = "ImGuiView";
//    private final static String TMP_PATH = "/data/local/tmp";
//    private final static String LIB_NAME = "libimgui.so";
//

    private final Activity ctx;
    private ViewGroup rootView;

    public ImGuiView(Activity ctx) {
        super(ctx);
        this.ctx = ctx;
        Log.d(TAG, "MyGLSurfaceView");
        if (!supportsOpenGLES3(ctx)) {
            Log.e(TAG, "OpenGL ES 3.0 not supported on this device");

            new AlertDialog.Builder(ctx)
                    .setTitle("Error")
                    .setMessage("This device does not support OpenGL ES 3.0")
                    .setPositiveButton("Exit", (dialog, which) -> {
                        ((Activity) ctx).finish();
                        Process.killProcess(Process.myPid());
                    }).setOnDismissListener(dialog -> {
                        ((Activity) ctx).finish();
                        Process.killProcess(Process.myPid());
                    })
                    .show();

            return;
        }

        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(TRANSPARENT);
        setZOrderOnTop(true);
        setRenderer(this);
        startMenu(ctx);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        nativeOnSurfaceCreated(getHolder().getSurface());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        nativeOnSurfaceChanged(getHolder().getSurface(), width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //Log.d(TAG, "onDrawFrame");

        nativeOnDrawFrame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        if (handleTouch(event.getX(), event.getY(), event.getAction()))
            return true;
        MotionEvent eventCopy = MotionEvent.obtain(event);
        for (int i = 0; i < this.rootView.getChildCount(); i++) {
            View child = this.rootView.getChildAt(i);
            Log.i(TAG,"ImGuiView onTouchEvent：" + child);
            if (child != this && child.dispatchTouchEvent(eventCopy))
                return true;
        }
        return true;
    }
//    @Override
//    public void surfaceDestroyed( SurfaceHolder holder) {
//        Log.i(TAG,"ImGuiView surfaceDestroyed");
//        super.surfaceDestroyed(holder);
//        //nativeOnDestroyed();
//
//    }


    private void startMenu(Activity ctx) {
        Log.d(TAG, "startMenu");

        // check if the view is already added
        if (getParent() != null) {
            Log.d(TAG, "View already added");
            return;
        }
        LayoutParams params = new LayoutParams(
                MATCH_PARENT,
                MATCH_PARENT
        );
        this.rootView = ctx.findViewById(android.R.id.content);
        this.rootView.addView(this, params);
    }

    public boolean supportsOpenGLES3(Context ctx) {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = am.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x30000);
    }

    private static native void nativeOnDrawFrame();

    private static native void nativeOnSurfaceChanged(Surface surface, int width, int height);

    private static native void nativeOnSurfaceCreated(Surface surface);

    private static native boolean handleTouch(float x, float y, int action);
    private static native void nativeOnDestroyed();
}