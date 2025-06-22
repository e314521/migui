package com.imgui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.imgui.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("imgui");
    }

    // Used to load the 'imgui' library on application startup.
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            String libraryPath = getApplicationInfo().nativeLibraryDir;
            //Class<?> System = getClassLoader().loadClass("java.lang.System");
            //Method loadMethod = System.getMethod("load", String.class);
            //loadMethod.invoke(null, libraryPath + "/libimgui.so");
            //Method loadMethod = System.getMethod("loadLibrary", String.class);
            //loadMethod.invoke(null, "imgui");


            //System.load(libraryPath + "/libimgui.so");
            Class<?> targetClass = getClassLoader().loadClass("com.imgui.ImGuiView");
            targetClass.getConstructor(Activity.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //getClassLoader().loadClass("java.lang.System");


        // Example of a call to a native method
        /*TextView tv = binding.sampleText;
        //ImGuiLoad.load(this);
        String optimizedDir = getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();
        //ImGuiLoad.createImGuiViewClass(this);

        //this.getApplicationContext().getCacheDir()
        String dexPath = getCacheDir() + "/ImGuiView.dex";
        //String optimizedDir = getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();

        String libraryPath = getApplicationInfo().nativeLibraryDir;
        try {
            Class<?> targetClass = getClassLoader().loadClass("com.imgui.ImGuiView");
            targetClass.getConstructor(Activity.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/






        //ImGuiView.createImGui(this);
        //new ImGuiView(this);


        /*try {
            Method findLib =  ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
            findLib.setAccessible(true);
            String path = (String) findLib.invoke(getClassLoader(), "imgui");
            libraryPath = new File(path).getParent();
            Log.i("MainActivity",path);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/




       /*BaseDexClassLoader a = (BaseDexClassLoader)getClassLoader();
        try {
            Method addDexPath = BaseDexClassLoader.class.getDeclaredMethod("addDexPath", String.class);
            addDexPath.setAccessible(true);
            addDexPath.invoke(a, dexPath);
            Class<?> targetClass = getClassLoader().loadClass("com.imgui.ImGuiView");
            targetClass.getConstructor(Activity.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

         Log.i("MainActivity",dexPath);
        try {
            ImClassLoader.DexUtils.addDexPath(getClassLoader(),new File(dexPath),new File(optimizedDir));
            Class<?> targetClass = getClassLoader().loadClass("com.imgui.ImGuiView");
            targetClass.getConstructor(Activity.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        DexClassLoader dexClassLoader = new ImClassLoader(dexPath,optimizedDir,libraryPath, getClassLoader());
        try {
            Class<?> targetClass = dexClassLoader.loadClass("com.imgui.ImGuiView");
            targetClass.getConstructor(Activity.class).newInstance(this);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/


        //Log.i("MainActivity",getApplicationInfo().nativeLibraryDir);

        //ImGuiView.createImGuiViewClass();
        //ImGuiView drawView = new ImGuiView( this);
        //binding.getRoot().addView(drawView);
        //ViewGroup contentView = ((ViewGroup)this.findViewById(android.R.id.content));
        //contentView.addView(drawView);

    }

    /**
     * A native method that is implemented by the 'imgui' native library,
     * which is packaged with this application.
     */

}