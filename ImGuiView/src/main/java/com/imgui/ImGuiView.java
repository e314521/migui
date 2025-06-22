package com.imgui;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.ViewGroup;


import androidx.annotation.NonNull;

public class ImGuiView extends GLSurfaceView implements SurfaceHolder.Callback{
    static {
        //System.loadLibrary("imgui");
    }
    static ImGuiView view;
    static public void CreateImGuiView(Activity activity){
        view = new ImGuiView(activity);
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
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        //int[] location = new int[2];
        //getLocationOnScreen(location);
        jniSurfaceCreate(holder.getSurface(), this.getWidth(), this.getHeight(), 0, 0);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        jniSurfaceChanged(width,height);
    }
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        jniSurfaceDestroyed();
    }
    public static native void jniSurfaceCreate(Surface surface, int width, int high, float x, float y);
    public static native void jniSurfaceChanged(int width, int high);
    public static native void jniSurfaceDestroyed();

    public static native void createImGui(Activity activity);

}
