#include <jni.h>
#include <string>
#include "pch.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_imgui_1demo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}