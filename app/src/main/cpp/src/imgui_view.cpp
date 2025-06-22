//
// Created by Administrator on 2025/6/19.
//

#include "imgui_view.h"
#include "xhook/xhook.h"

static bool RunInitImgui;
#define HOOK(ret, func, ...)                                                   \
  ret (*orig##func)(__VA_ARGS__);                                              \
  ret my##func(__VA_ARGS__)
HOOK(void, Input, void *thiz, void *ex_ab, void *ex_ac) {
    origInput(thiz, ex_ab, ex_ac);
    ImGui_ImplAndroid_HandleInputEvent((AInputEvent *)thiz);
    return;
}

imgui_view::imgui_view() {
    LOGE("imgui_view");
    mEglDisplay = EGL_NO_DISPLAY;
    mEglSurface = EGL_NO_SURFACE;
    mEglConfig  = nullptr;
    mEglContext = EGL_NO_CONTEXT;
    xhook_register(".*\\.so$",
                   "_ZN7android13InputConsumer21initializeMotionEventEPNS_11MotionEventEPKNS_12InputMessageE",
                   (void *)myInput, (void **)&origInput);
    xhook_refresh(1);
}

void imgui_view::onSurfaceCreate(JNIEnv *env, jobject surface, int SurfaceWidth, int SurfaceHigh){
    LOGE("onSurfaceCreate");

    this->SurfaceWin       = ANativeWindow_fromSurface(env, surface);
    this->surfaceWidth     = SurfaceWidth;
    this->surfaceHigh      = SurfaceHigh;
    this->surfaceWidthHalf = this->surfaceWidth / 2;
    this->surfaceHighHalf  = this->surfaceHigh / 2;
    SurfaceThread = new std::thread([this] { EglThread(); });
    SurfaceThread->detach();
    LOGE("onSurfaceCreate_end");
}
void imgui_view::onSurfaceChange(int SurfaceWidth, int SurfaceHigh){
    this->surfaceWidth     = SurfaceWidth;
    this->surfaceHigh      = SurfaceHigh;
    this->surfaceWidthHalf = this->surfaceWidth / 2;
    this->surfaceHighHalf  = this->surfaceHigh / 2;
    this->isChage          = true;
    LOGE("onSurfaceChange");
}
void imgui_view::onSurfaceDestroy(){
    LOGE("onSurfaceDestroy");
    this->isDestroy = true;
    std::unique_lock<std::mutex> ulo(Threadlk);
    cond.wait(ulo, [this] { return !this->ThreadIo; });
    delete SurfaceThread;
    SurfaceThread = nullptr;

    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplAndroid_Shutdown();
    //ImGui::DestroyContext();
    if (mEglDisplay != EGL_NO_DISPLAY)
    {
        eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);

        if (mEglContext != EGL_NO_CONTEXT)
            eglDestroyContext(mEglDisplay, mEglContext);

        if (mEglSurface != EGL_NO_SURFACE)
            eglDestroySurface(mEglDisplay, mEglSurface);

        eglTerminate(mEglDisplay);
    }

    mEglDisplay = EGL_NO_DISPLAY;
    mEglSurface = EGL_NO_SURFACE;
    mEglContext = EGL_NO_CONTEXT;
    ANativeWindow_release(SurfaceWin);
    LOGE("onSurfaceDestroy end");
}

int imgui_view::initEgl() {
    //1、
    mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (mEglDisplay == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay error=%u", glGetError());
        return -1;
    }
    LOGE("生成mEglDisplay");
    //2、
    EGLint *version = new EGLint[2];
    if (!eglInitialize(mEglDisplay, &version[0], &version[1])) {
        LOGE("eglInitialize error=%u", glGetError());
        return -1;
    }
    LOGE("eglInitialize成功");
    //3、
    const EGLint attribs[] = {EGL_BUFFER_SIZE, 32, EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8,
                              EGL_BLUE_SIZE, 8, EGL_ALPHA_SIZE, 8, EGL_DEPTH_SIZE, 8, EGL_STENCIL_SIZE, 8, EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL_SURFACE_TYPE, EGL_WINDOW_BIT, EGL_NONE};

    EGLint num_config;
    if (!eglGetConfigs(mEglDisplay, NULL, 1, &num_config)) {
        LOGE("eglGetConfigs  error =%u", glGetError());
        return -1;
    }
    LOGE("num_config=%d", num_config);
    // 4、
    if (!eglChooseConfig(mEglDisplay, attribs, &mEglConfig, 1, &num_config)) {
        LOGE("eglChooseConfig  error=%u", glGetError());
        return -1;
    }
    LOGE("eglChooseConfig成功");
    //5、
    int attrib_list[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    mEglContext = eglCreateContext(mEglDisplay, mEglConfig, EGL_NO_CONTEXT, attrib_list);
    if (mEglContext == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext  error = %u", glGetError());
        return -1;
    }
    // 6、
    mEglSurface = eglCreateWindowSurface(mEglDisplay, mEglConfig, SurfaceWin, NULL);
    if (mEglSurface == EGL_NO_SURFACE) {
        LOGE("eglCreateWindowSurface  error = %u", glGetError());
        return -1;
    }
    LOGE("eglCreateWindowSurface成功");
    //7、
    if (!eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
        LOGE("eglMakeCurrent  error = %u", glGetError());
        return -1;
    }
    LOGE("eglMakeCurrent成功");
    return 1;
}
int imgui_view::initImgui(){
    if (RunInitImgui){
        //如果初始化过，就只执行这段
        //IMGUI_CHECKVERSION();
        //ImGui::CreateContext();
        ImGui_ImplAndroid_Init(SurfaceWin);
        ImGui_ImplOpenGL3_Init("#version 300 es");
        return 1;
    }
    RunInitImgui= true;
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO * io = &ImGui::GetIO();
    io->SetMousePosOff(off_x,off_y);
    io->IniSavingRate = 10.0f;

    if(!ImGui_ImplAndroid_Init(SurfaceWin)){
        LOGE("ImGui_ImplAndroid_Init  error");
        return -1;
    }

    if(!ImGui_ImplOpenGL3_Init("#version 300 es")){
        LOGE("ImGui_ImplOpenGL3_Init  error");
        return -1;
    }

    ImFontConfig font_cfg;

    font_cfg.SizePixels = 22.0f;

    io->Fonts->AddFontDefault(&font_cfg);
    io->MouseDoubleClickTime = 0.0001f;
    g = ImGui::GetCurrentContext();
    ImGuiStyle * style =&ImGui::GetStyle();
    style->ScaleAllSizes(4.0f);//缩放尺寸
    style->FramePadding=ImVec2(10.0f,20.0f);
    return 1;
}

void imgui_view::EglThread(){
    LOGE("imgui线程开始");
    if(this->initEgl() != 1){
        LOGE("Egl初始化失败");
        return;
    }
    LOGE("Egl初始化完成");
    if(this->initImgui() != 1){
        LOGE("imgui初始化失败!");
    }
    LOGE("imgui初始化完成");
    ThreadIo = true;

    while (true) {
        if (this->isChage) {
            glViewport(0, 0, this->surfaceWidth, this->surfaceHigh);
            this->isChage = false;
        }
        if (this->isDestroy) {
            this->isDestroy = false;
            ThreadIo = false;
            cond.notify_all();
            return;
        }
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (!ActivityState)continue;
        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplAndroid_NewFrame();
        ImGui::NewFrame();
        ImVec2 center = ImGui::GetMainViewport()->GetCenter();
        ImGui::SetNextWindowPos(center, ImGuiCond_Appearing, ImVec2(0.5f, 0.5f));
        if (show_demo_window) {
            ImGui::ShowDemoWindow(&show_demo_window);
        }


        ImGui::SetNextWindowSize(ImVec2(250 * 3, 100 * 3), 0);
        ImGui::Begin("Hello, world!");
        ImGui::Text("This is some useful text.");
        ImGui::Text("Application average %.3f ms/frame (%.1f FPS)",
                    1000.0f / ImGui::GetIO().Framerate, ImGui::GetIO().Framerate);
        ImGui::Checkbox("Demo Window", &show_demo_window);

        ImGui::End();


        ImGui::Render();
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
        if(!eglSwapBuffers(mEglDisplay, mEglSurface)){
            LOGE("eglSwapBuffers  error = %u", glGetError());
        }
    }



};
