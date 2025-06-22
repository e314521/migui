//
// Created by Administrator on 2025/6/19.
//

#ifndef IMGUI_DEMO_IMGUI_VIEW_H
#define IMGUI_DEMO_IMGUI_VIEW_H

#include "log.h"


#include <thread>
#include <jni.h>

#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include "external-lib/ImGui/imgui.h"
#include "external-lib/ImGui/imgui_internal.h"
#include "external-lib/ImGui/imgui_impl_opengl3.h"
#include "external-lib/ImGui/imgui_impl_android.h"
#include "android/native_window_jni.h"

class imgui_view {
    std::condition_variable cond;
    std::mutex              Threadlk;
    EGLDisplay              mEglDisplay;
    EGLSurface              mEglSurface;
    EGLConfig               mEglConfig;
    EGLContext              mEglContext;

    EGLNativeWindowType     SurfaceWin;
    std::thread             *SurfaceThread = nullptr;

    int          surfaceWidthHalf   = 0;
    int          surfaceHighHalf    = 0;
    int          StatusBarHeight    = 0;

    bool ThreadIo;

    void EglThread();
    int initEgl();
    int initImgui();
public:
    int             surfaceWidth        = 0;
    int             surfaceHigh         = 0;
    float           off_x               = 0;
    float           off_y               = 0;
    bool            ActivityState       = true;
    ImGuiWindow     *g_window           = nullptr;
    ImGuiContext    *g                  = nullptr;
    bool            isChage             = false;
    bool            isDestroy           = false;
    bool            show_demo_window    = false;
    imgui_view();

    void onSurfaceCreate(JNIEnv *env, jobject surface, int SurfaceWidth, int SurfaceHigh);
    void onSurfaceChange(int SurfaceWidth, int SurfaceHigh);
    void onSurfaceDestroy();
};
#endif //IMGUI_DEMO_IMGUI_VIEW_H
