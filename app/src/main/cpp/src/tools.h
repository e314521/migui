//
// Created by Administrator on 2026/5/7.
//
#include <jni.h>
#ifndef IMGUI_TOOLS_H
#define IMGUI_TOOLS_H

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
#endif //IMGUI_TOOLS_H
