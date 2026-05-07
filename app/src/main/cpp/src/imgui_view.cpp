//
// Created by Administrator on 2025/6/19.
//

#include "imgui_view.h"
#include "tools.h"
#include "dex_data.h"

static bool g_Initialized = false;
extern "C" void beginFrame();
extern "C" void renderFrame();
extern "C" void endFrame();
extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnSurfaceCreated(JNIEnv *env, jclass clazz, jobject surface) {
    if (g_Initialized)
        return;
    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    if (!window)
    {
        LOGE("ANativeWindow_fromSurface failed");
        return;
    }
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO &io = ImGui::GetIO();

    // Setup Dear ImGui style
    ImGui::StyleColorsDark();
    // ImGui::StyleColorsLight();

    // Setup Platform/Renderer backends
    ImGui_ImplAndroid_Init(window);
    ImGui_ImplOpenGL3_Init("#version 300 es");

//    ImFontConfig font_cfg;
//    font_cfg.SizePixels = 22.0f;
//    io.Fonts->AddFontDefault(&font_cfg);

    ImFont* font = io.Fonts->AddFontFromFileTTF(
            "/system/fonts/MiSansVF.ttf",  // Android 常用中文字体路径
            22.0f,
            nullptr,
            io.Fonts->GetGlyphRangesChineseFull()  // 加载全部中文字符
    );

    // Arbitrary scale-up
    // FIXME: Put some effort into DPI awareness
    ImGui::GetStyle().ScaleAllSizes(3.0f);
    io.FontGlobalScale = 1.2f;

    g_Initialized = true;

    LOGD("setup done");
}
extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnSurfaceChanged(JNIEnv *env, jclass clazz, jint width,
                                                                       jint height) {
    LOGD("nativeOnSurfaceChanged");
    if (!g_Initialized)
        return;

    ImGuiIO &io = ImGui::GetIO();
    io.DisplaySize = ImVec2((float)width, (float)height);
}
extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnDrawFrame(JNIEnv *env, jclass clazz)
{
    LOGD("nativeOnDrawFrame");

    if (!g_Initialized)
        return;

    beginFrame();
    renderFrame();
    endFrame();
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_imgui_ImGuiView_handleTouch(JNIEnv *env, jclass clazz, jfloat x, jfloat y, jint action)
{
    LOGD("handleTouch");

    if (!g_Initialized)
        return false;

    ImGuiIO &io = ImGui::GetIO();

    switch (action)
    {
        case 0: // ACTION_DOWN
            io.AddMousePosEvent(x, y);
            io.AddMouseButtonEvent(0, true);
            break;
        case 1: // ACTION_UP
            io.AddMouseButtonEvent(0, false);
            io.AddMousePosEvent(-1, -1);
            break;
        case 2: // ACTION_MOVE
            io.AddMousePosEvent(x, y);
            break;
        default:
            return false;
            break;
    }

    return io.WantCaptureMouse ? true : false;
}
static JNINativeMethod methods[] = {
        {"nativeOnSurfaceCreated", "(Landroid/view/Surface;)V", (void*)Java_com_imgui_ImGuiView_nativeOnSurfaceCreated},
        {"nativeOnSurfaceChanged", "(II)V", (void*)Java_com_imgui_ImGuiView_nativeOnSurfaceChanged},
        {"nativeOnDrawFrame", "()V", (void*)Java_com_imgui_ImGuiView_nativeOnDrawFrame},
        {"handleTouch", "(FFI)Z", (void*)Java_com_imgui_ImGuiView_handleTouch}
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
    jobject ClassLoader = getThreadClassLoader(env);
    addDexPath(env,ClassLoader,env->NewStringUTF(dexPath.c_str()));
    jclass ImGuiView = DexLoaderClass(env, ClassLoader,env->NewStringUTF("com.imgui.ImGuiView"));
    if (env->RegisterNatives(ImGuiView, methods,sizeof(methods)/sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}


void beginFrame()
{
    ImGuiIO &io = ImGui::GetIO();

    LOGD("Start rendering...");
    LOGD("DisplaySize: %f, %f", io.DisplaySize.x, io.DisplaySize.y);

    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame();
    ImGui::NewFrame();
}

void endFrame()
{
    ImGuiIO &io = ImGui::GetIO();

    ImGui::Render();
    glViewport(0, 0, (int)io.DisplaySize.x, (int)io.DisplaySize.y);
    glClear(GL_COLOR_BUFFER_BIT);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}

/*
    * ImGui rendering
     Render frame is meant to be hooked from frida
     So that we can can render ImGui frame from frida
     By default, it will render a simple demo frame
 */
void renderFrame()
{
//    LOGD("renderFrame");
//    static float f = 0.0f;
//    static int counter = 0;
//
//    ImGuiIO &io = ImGui::GetIO();
//
//    ImGui::Begin("Hello, world!"); // Create a window called "Hello, world!" and append into it.
//
//    ImGui::Text(
//            "This is some useful text."); // Display some text (you can use a format strings too)
//
//
//    ImGui::SliderFloat("float", &f, 0.0f,
//                       1.0f); // Edit 1 float using a slider from 0.0f to 1.0f
//
//
//    if (ImGui::Button(
//            "Button")) // Buttons return true when clicked (most widgets return true when edited/activated)
//        counter++;
//    ImGui::SameLine();
//    ImGui::Text("counter = %d", counter);
//
//    ImGui::Text("Application average %.3f ms/frame (%.1f FPS)", 1000.0f / io.Framerate,
//                io.Framerate);
//    ImGui::End();
}
