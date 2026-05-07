package com.imgui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ImGuiView extends GLSurfaceView implements SurfaceHolder.Callback{

    static ImGuiView view;
    @SuppressLint("PrivateApi")
    static public void CreateImGuiViewThread(){
        if (view != null){
            return;
        }
        Class<?> activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map<?, ?> activities = (Map<?, ?>) activitiesField.get(activityThread);
            // 遍历mActivities，获取栈顶Activity
            Object topActivity = null;
            for (Object activityRecord : activities.values()) {
                Class<?> activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!(boolean) pausedField.get(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    topActivity = activityField.get(activityRecord);
                    break;
                }
            }
            if(topActivity == null){
                Thread.sleep(2000);
                new Thread(ImGuiView::CreateImGuiViewThread).start();
            }else{
                Log.i("Imgui",topActivity.toString());
                Activity a = (Activity)topActivity;
                a.runOnUiThread(() -> {
                    // 直接在主线程更新UI
                    Log.i("Imgui","asdasd");
                    view = new ImGuiView(a);
                });
                //view = new ImGuiView((Activity)topActivity);

            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressLint("PrivateApi")
    static public void CreateImGuiView(Activity activity){
        Log.i("Imgui","asdasd");
        new Thread(ImGuiView::CreateImGuiViewThread).start();


        //view = new ImGuiView(activity);
    }


    public ImGuiView(Activity activity) {
        super(activity.getBaseContext());
        ViewGroup contentView = ((ViewGroup)activity.getWindow().getDecorView().getRootView());
        contentView.addView(this);
        setEGLContextClientVersion(3);
        setEGLConfigChooser(  8,  8,  8,  8,  16,  0);
        setZOrderOnTop(true);//置顶
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(this);
        Log.i("Imgui","ImGuiView");
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        //int[] location = new int[2];
        //getLocationOnScreen(location);
        Log.i("Imgui","ImGuiView surfaceCreated");
        jniSurfaceCreate(holder.getSurface(), this.getWidth(), this.getHeight(), 0, 0);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.i("Imgui","ImGuiView surfaceChanged");
        jniSurfaceChanged(width,height);
    }
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i("Imgui","ImGuiView surfaceDestroyed");
        jniSurfaceDestroyed();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if (handleTouch(event.getX(), event.getY(), event.getAction()))
            return true;

        // If ImGui doesn't handle the event, dispatch it to the underlying views
        View rootView = ((Activity) getContext()).getWindow().getDecorView().getRootView();
        return dispatchTouchEventToRoot(rootView, event);
    }
    private boolean dispatchTouchEventToRoot(View rootView, MotionEvent event) {
        if (rootView instanceof ViewGroup) {
            ViewGroup rootViewGroup = (ViewGroup) rootView;
            MotionEvent eventCopy = MotionEvent.obtain(event);

            for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
                View child = rootViewGroup.getChildAt(i);
                if (child != this && child.dispatchTouchEvent(eventCopy))
                    return true;
            }
        }
        return false;
    }
    public static native void jniSurfaceCreate(Surface surface, int width, int high, float x, float y);
    public static native void jniSurfaceChanged(int width, int high);
    public static native void jniSurfaceDestroyed();

    public static native void createImGui(Activity activity);
    private static native boolean handleTouch(float x, float y, int action);

}
