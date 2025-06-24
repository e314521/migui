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

        try {

            /*Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
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



            Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
            //ActivityThread.getMethod("currentActivityThread","()Landroid/app/ActivityThread;")
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfos = activityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo top = taskInfos.get(0);



            Log.i("123",taskInfos.get(0).topActivity.toString());

            /*ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            @SuppressLint("SoonBlockedPrivateApi") Field mServiceFiled = ClipboardManager.class.getDeclaredField("mService");
            mServiceFiled.setAccessible(true);
            final Object mService = mServiceFiled.get(clipboardManager);
            Class aClass = Class.forName("android.content.IClipboard");
            Object proxyInstance = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{aClass}, (proxy, method, args) -> {
                Log.d("123", "invoke(). method:" + method);
                return method.invoke(mService, args);
            });

            @SuppressLint("SoonBlockedPrivateApi") Field sServiceField = ClipboardManager.class.getDeclaredField("mService");
            sServiceField.setAccessible(true);
            sServiceField.set(clipboardManager, proxyInstance);*/

            //1.主线程ActivityThread内部的mInstrumentation对象，先把他拿出来

            /*Class<?> ActivityThreadClz = Class.forName("android.app.ActivityThread");
            //再拿到sCurrentActivityThread
            Field sCurrentActivityThreadField = ActivityThreadClz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object activityThreadObj = sCurrentActivityThreadField.get(null);//静态变量的属性get不需要参数，传null即可.
            //再去拿它的mInstrumentation
            Field mInstrumentationField = ActivityThreadClz.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation base = (Instrumentation) mInstrumentationField.get(activityThreadObj);// OK,拿到
            ProxyInstrumentation proxyInstrumentation = new ProxyInstrumentation(base);
            mInstrumentationField.set(activityThreadObj, proxyInstrumentation);*/



        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Intent i = new Intent(MainActivity.this, MainActivity2.class);
        //startActivity(i);
        /*try {
            Method onResume = Activity.class.getDeclaredMethod("onResume");
            var onResumeReplacement = Replacement.class.getDeclaredMethod("onResumeReplacement", Hooker.MethodCallback.class);
            Hooker.hook(onResume,onResumeReplacement, new Replacement());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }*/



        //




        try {
            //String libraryPath = getApplicationInfo().nativeLibraryDir;
            //Class<?> System = getClassLoader().loadClass("java.lang.System");
            //Method loadMethod = System.getMethod("load", String.class);
            //loadMethod.invoke(null, libraryPath + "/libimgui.so");
            //Method loadMethod = System.getMethod("loadLibrary", String.class);
            //loadMethod.invoke(null, "imgui");


            //System.load(libraryPath + "/libimgui.so");
            //Class<?> targetClass = getClassLoader().loadClass("com.imgui.ImGuiView");
            //targetClass.getConstructor(Activity.class).newInstance(this);
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