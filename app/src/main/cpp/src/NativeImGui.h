//
// Created by Administrator on 2025/6/19.
//

#ifndef IMGUI_DEMO_NATIVEIMGUI_H
#define IMGUI_DEMO_NATIVEIMGUI_H
#include <stdio.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <unistd.h>

#include <jni.h>
#include <string>
#include "imgui_view.h"
#include "dex_data.h"
#include <sys/mman.h>


imgui_view       *mView   = nullptr;

jobject getGlobalContext(JNIEnv* env) {
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(
            activityThread,
            "currentActivityThread",
            "()Landroid/app/ActivityThread;"
    );
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);

    jmethodID getApplication = env->GetMethodID(
            activityThread,
            "getApplication",
            "()Landroid/app/Application;"
    );
    return env->CallObjectMethod(at, getApplication);
}

jstring getFilePath(JNIEnv* env, jobject fileObj) {
    jclass fileClass = env->GetObjectClass(fileObj);
    jmethodID getPathMethod = env->GetMethodID(
            fileClass,
            "getPath",
            "()Ljava/lang/String;"
    );
    return (jstring)env->CallObjectMethod(fileObj, getPathMethod);
}

jstring getCacheDir(JNIEnv* env, jobject context) {
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getCacheDir = env->GetMethodID(
            contextClass,
            "getCacheDir",
            "()Ljava/io/File;"
    );
    jobject CacheDir = (jstring)env->CallObjectMethod(context, getCacheDir);
    return (jstring)getFilePath(env,CacheDir);
}

jstring getNativeLibPath(JNIEnv* env, jobject context) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getAppInfoMethod = env->GetMethodID(
            contextClass,
            "getApplicationInfo",
            "()Landroid/content/pm/ApplicationInfo;"
    );
    jobject appInfo = env->CallObjectMethod(context, getAppInfoMethod);

    jclass appInfoClass = env->FindClass("android/content/pm/ApplicationInfo");
    jfieldID nativeLibDirField = env->GetFieldID(
            appInfoClass,
            "nativeLibraryDir",
            "Ljava/lang/String;"
    );
    return (jstring)env->GetObjectField(appInfo, nativeLibDirField);
}

jobject getClassLoader(JNIEnv* env, jobject context) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getClassLoader = env->GetMethodID(
            contextClass,
            "getClassLoader",
            "()Ljava/lang/ClassLoader;"
    );
    return env->CallObjectMethod(context, getClassLoader);
}

jstring getCurrentSoPath(JNIEnv* env) {
    Dl_info info;
    dladdr((void*)getCurrentSoPath, &info);
    return env->NewStringUTF(info.dli_fname);
}
jstring getCurrentSoDir(JNIEnv* env) {
    Dl_info info;
    char path[PATH_MAX] = {0};
    // 读取/proc/self/maps获取内存映射路径
    dladdr((void*)getCurrentSoPath, &info);
    if(info.dli_fname) {
        strncpy(path, info.dli_fname, PATH_MAX);
    }
    // 提取目录部分
    char* last_slash = strrchr(path, '/');
    if(last_slash) *last_slash = '\0';
    return env->NewStringUTF(path);
}



jobject createDexLoader(JNIEnv* env,
                        jstring dexPath,
                        jstring optimizedDir,
                        jstring libraryPath,
                        jobject parentLoader) {
    jclass loaderClass = env->FindClass("dalvik/system/DexClassLoader");
    jmethodID constructor = env->GetMethodID(
            loaderClass,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V"
    );
    return env->NewObject(loaderClass, constructor,
                          dexPath, optimizedDir, libraryPath, parentLoader);
}

jclass DexLoaderClass(JNIEnv* env, jobject context, jstring name) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID loadClass = env->GetMethodID(
            contextClass,
            "loadClass",
            "(Ljava/lang/String;)Ljava/lang/Class;"
    );
    return (jclass)env->CallObjectMethod(context, loadClass, name);
}

void addDexPath(JNIEnv* env, jobject context, jstring name) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getClassLoader = env->GetMethodID(
            contextClass,
            "addDexPath",
            "(Ljava/lang/String;)V"
    );
    env->CallVoidMethod(context, getClassLoader, name);
}

jobject getThreadClassLoader(JNIEnv* env) {
    // 获取Thread类
    jclass threadClass = env->FindClass("java/lang/Thread");

    // 调用currentThread()
    jmethodID currentThread = env->GetStaticMethodID(
            threadClass,
            "currentThread",
            "()Ljava/lang/Thread;"
    );
    jobject thread = env->CallStaticObjectMethod(threadClass, currentThread);

    // 获取getContextClassLoader()
    jmethodID getLoader = env->GetMethodID(
            threadClass,
            "getContextClassLoader",
            "()Ljava/lang/ClassLoader;"
    );
    return env->CallObjectMethod(thread, getLoader);
}

extern "C"
{
    JNIEXPORT void JNICALL  Java_com_imgui_ImGuiView_jniSurfaceCreate(JNIEnv *env, jclass clazz, jobject surface, jint width, jint high, jfloat x, jfloat y){
        if(mView == nullptr){
            mView = new imgui_view;
        }
        mView->off_x = x;
        mView->off_y = y;
        mView->onSurfaceCreate(env, surface, width, high);
        mView->ActivityState= true;

    }
    JNIEXPORT void JNICALL  Java_com_imgui_ImGuiView_jniSurfaceChanged(JNIEnv *env, jclass clazz, jint width, jint high){
        if (mView != nullptr) {
            mView->onSurfaceChange(width, high);
        }
    }
    JNIEXPORT void JNICALL  Java_com_imgui_ImGuiView_jniSurfaceDestroyed(JNIEnv *env, jclass clazz){
        if (mView != nullptr) {
            mView->ActivityState= false;
            mView->onSurfaceDestroy();
        }
    }
    JNIEXPORT jclass JNICALL  Java_com_imgui_ImGuiView_createImGuiViewClass(JNIEnv *env, jclass clazz, jobject activity){

        return nullptr;
    }
    JNIEXPORT void JNICALL  Java_com_imgui_ImGuiView_createImGui(JNIEnv *env, jclass clazz, jobject activity){

        jclass ImGuiView = DexLoaderClass(env, getThreadClassLoader(env),env->NewStringUTF("com.imgui.ImGuiView"));
        jmethodID constructor = env->GetStaticMethodID(
                ImGuiView,
                "CreateImGuiView",
                "(Landroid/app/Activity;)V"
        );
        env->CallStaticVoidMethod(ImGuiView, constructor, activity);


    }

    static JNINativeMethod methods[] = {
            {"jniSurfaceCreate", "(Landroid/view/Surface;IIFF)V", (void*)Java_com_imgui_ImGuiView_jniSurfaceCreate},
            {"jniSurfaceChanged", "(II)V", (void*)Java_com_imgui_ImGuiView_jniSurfaceChanged},
            {"jniSurfaceDestroyed", "()V", (void*)Java_com_imgui_ImGuiView_jniSurfaceDestroyed}
    };

    jint JNIEXPORT JNI_OnLoad(JavaVM *vm, void *key) {

        LOGE("JNI_OnLoad");
        JNIEnv* env;
        if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
            return JNI_ERR;
        }
        jobject context = getGlobalContext(env);
        jstring CacheDirStr = getCacheDir(env,context);
        const char* path = env->GetStringUTFChars(CacheDirStr, NULL);
        string sCacheDir = path;
        env->ReleaseStringUTFChars(CacheDirStr, path);
        string dexPath =  sCacheDir + "/ImGuiView.dex";
        if(access(dexPath.c_str(), F_OK) == 0) {
            remove(dexPath.c_str());
        }
        int fd = open(dexPath.c_str(), O_CREAT | O_WRONLY, 0444);
        if (fd != -1) {
            write(fd, dex_data, sizeof(dex_data));
            close(fd);
        }



        // 获取系统ClassLoader
        jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");



        jmethodID getSystemLoader = env->GetStaticMethodID(
                classLoaderClass,
                "getSystemClassLoader",
                "()Ljava/lang/ClassLoader;"
        );
        jobject systemLoader = env->CallStaticObjectMethod(
                classLoaderClass,
                getSystemLoader
        );
        jobject ClassLoader = getThreadClassLoader(env);


        addDexPath(env,ClassLoader,env->NewStringUTF(dexPath.c_str()));

        jclass ImGuiView = DexLoaderClass(env, ClassLoader,env->NewStringUTF("com.imgui.ImGuiView"));
        jmethodID constructor = env->GetStaticMethodID(
                ImGuiView,
                "CreateImGuiView",
                "(Landroid/app/Activity;)V"
        );
        if (env->RegisterNatives(ImGuiView, methods,sizeof(methods)/sizeof(methods[0])) < 0) {
            return JNI_ERR;
        }
        env->CallStaticVoidMethod(ImGuiView, constructor, NULL);
        return JNI_VERSION_1_6;
    }
};
#endif //IMGUI_DEMO_NATIVEIMGUI_H
