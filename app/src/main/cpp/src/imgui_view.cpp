//
// Created by Administrator on 2025/6/19.
//

#include "imgui_view.h"
#include "tools.h"
#include "dex_data.h"



static bool g_Initialized = false;
static jobject g_surface = nullptr;
static JavaVM* g_JavaVM = nullptr;
static ImXML::XMLReader *xml_reader = new ImXML::XMLReader();;
static ImXML::XMLTree *xml_tree = nullptr;
static ImXML::XMLRenderer *xml_renderer = new ImXML::XMLRenderer();

static string xml_path = "imgui.xml";
extern "C" void beginFrame();
extern "C" void renderFrame(JNIEnv *env);
extern "C" void endFrame();
class Handler : public ImXML::XMLEventHandler {
public:
    JNIEnv *env;
    virtual void onNodeBegin(ImXML::XMLNode& node) override {}

    virtual void onNodeEnd(ImXML::XMLNode& node) override {}

    virtual void onEvent(ImXML::XMLNode& node) override {
        if (node.args.contains("dynamic")) {
            onDynamic(node.args.at("dynamic").c_str());
        }



        ImXML::XMLDynamicBind bind = xml_renderer->getDynamicBind(node);
        if (bind.object){
            auto * imGuiValue = static_cast<ImGuiValue*>(bind.object);
            imGuiValue->updata(env);
        }

    }
};

static Handler xml_handler;
void initSurface(JNIEnv *env, jobject surface)
{
    if (g_Initialized)
        return;
//    g_surface = surface;
//    g_NativeWindow= ANativeWindow_fromSurface(env, surface);
//    if (!g_NativeWindow)
//    {
//        LOGE("ANativeWindow_fromSurface failed");
//        return;
//    }
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO &io = ImGui::GetIO();

    // Setup Dear ImGui style
    ImGui::StyleColorsDark();
    // ImGui::StyleColorsLight();

    // Setup Platform/Renderer backends
    //ImGui_ImplAndroid_Init(g_NativeWindow);
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
void destroySurface(){
    if(g_Initialized){
        g_Initialized = false;
        LOGD("destroySurface");
//        ImGui_ImplOpenGL3_Shutdown();
//        ImGui_ImplAndroid_Shutdown();
//        ImGui::DestroyContext();
//        ANativeWindow_release(g_NativeWindow);

    }

}
extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnSurfaceCreated(JNIEnv *env, jclass clazz, jobject surface) {

}
extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnSurfaceChanged(JNIEnv *env, jclass clazz, jobject surface, jint width,
                                                                       jint height) {
    LOGD("nativeOnSurfaceChanged,width:{},height:{}",width,height);

    initSurface(env,surface);



    ImGuiIO &io = ImGui::GetIO();
    io.DisplaySize = ImVec2((float)width, (float)height);
}
extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnDrawFrame(JNIEnv *env, jclass clazz)
{
    //LOGD("nativeOnDrawFrame");

    if (!g_Initialized)
        return;

    beginFrame();
    renderFrame(env);
    endFrame();
}

// 几何碰撞检测
bool IsPosInsideAnyImGuiWindow(float x, float y) {
    ImGuiContext& g = *GImGui;
    ImVec2 touch_pos = ImVec2(x, y);
    for (int i = 0; i < g.Windows.Size; i++) {
        ImGuiWindow* window = g.Windows[i];
        if (window->WasActive && !window->Hidden && !(window->Flags & ImGuiWindowFlags_NoInputs)) {
            if (window->Rect().Contains(touch_pos)) {
                return true;
            }
        }
    }
    return false;
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_imgui_ImGuiView_handleTouch(JNIEnv *env, jclass clazz, jint action, jfloat x, jfloat y)
{

    if (!g_Initialized) return JNI_FALSE;
    // 安全边界处理


    ImGuiIO& io = ImGui::GetIO();
    io.AddMousePosEvent(x, y);
    bool IsPosInside = IsPosInsideAnyImGuiWindow(x, y);
    if(!IsPosInside){
        if(GImGui->NavWindow && action == 0){
            io.AddMouseButtonEvent(0, true);
            io.AddMouseButtonEvent(0, false);
        }
        return JNI_FALSE;
    }
    if (action == 0) { // MotionEvent.ACTION_DOWN
        io.AddMouseButtonEvent(0, true);
    }else if (action == 1 || action == 3) { // ACTION_UP 或 ACTION_CANCEL
        io.AddMouseButtonEvent(0, false);
    }
    return JNI_TRUE;

}


extern "C" JNIEXPORT jboolean JNICALL Java_com_imgui_ImGuiView_readXml(JNIEnv *env, jclass clazz, jstring text)
{
    const char* path = env->GetStringUTFChars(text, NULL);
    readXml(path);
    env->ReleaseStringUTFChars(text, path);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL Java_com_imgui_ImGuiView_nativeOnDestroyed(JNIEnv *env, jclass clazz) {
    // TODO: implement nativeOnDestroyed()
    destroySurface();
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_imgui_ImGuiView_addDynamicBind(JNIEnv *env, jclass clazz, jstring text, jobject value) {
    ImGuiValue * imGuiValue = new ImGuiValue(env, value);
    const char* name = env->GetStringUTFChars(text, NULL);
    string dynamic = std::string(name);
    if(!xml_renderer->checkDynamicBind(dynamic)){
        xml_renderer->addDynamicBind(std::string(name), imGuiValue->bind);
    }
    env->ReleaseStringUTFChars(text, name);
    return JNI_TRUE;
}
static JNINativeMethod methods[] = {
        {"nativeOnSurfaceCreated", "(Landroid/view/Surface;)V", (void*)Java_com_imgui_ImGuiView_nativeOnSurfaceCreated},
        {"nativeOnSurfaceChanged", "(Landroid/view/Surface;II)V", (void*)Java_com_imgui_ImGuiView_nativeOnSurfaceChanged},
        {"nativeOnDrawFrame", "()V", (void*)Java_com_imgui_ImGuiView_nativeOnDrawFrame},
        {"nativeOnDestroyed", "()V", (void*)Java_com_imgui_ImGuiView_nativeOnDestroyed},
        {"handleTouch", "(IFF)Z", (void*)Java_com_imgui_ImGuiView_handleTouch},
        {"readXml", "(Ljava/lang/String;)Z", (void*)Java_com_imgui_ImGuiView_readXml},
        {"addDynamicBind", "(Ljava/lang/String;Lcom/imgui/ImGuiValue;)Z", (void*)Java_com_imgui_ImGuiView_addDynamicBind}
};



jint JNIEXPORT JNI_OnLoad(JavaVM *vm, void *key) {
    LOGD("JNI_OnLoad");
    g_JavaVM = vm;
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
    jstring FilesDir = getFilesDir(env,context);
    path = env->GetStringUTFChars(FilesDir, NULL);
    string sFilesDir = path;
    env->ReleaseStringUTFChars(FilesDir, path);

    xml_path = sFilesDir + "/imgui.xml";
    LOGD("{}",xml_path.c_str());
    int fd = open(dexPath.c_str(), O_CREAT | O_WRONLY, 0444);
    if (fd != -1) {
        write(fd, dex_data, sizeof(dex_data));
        close(fd);
    }
    jobject ClassLoader = getThreadClassLoader(env);
    addDexPath(env,ClassLoader,env->NewStringUTF(dexPath.c_str()));
    jclass ImGuiValue = DexLoaderClass(env, ClassLoader,env->NewStringUTF("com.imgui.ImGuiValue"));
    jclass ImGuiView = DexLoaderClass(env, ClassLoader,env->NewStringUTF("com.imgui.ImGuiView"));
    if (env->RegisterNatives(ImGuiView, methods,sizeof(methods)/sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}


void beginFrame()
{
    ImGuiIO &io = ImGui::GetIO();

    //LOGD("Start rendering...");
    //LOGD("DisplaySize: %f, %f", io.DisplaySize.x, io.DisplaySize.y);

    ImGui_ImplOpenGL3_NewFrame();
    //ImGui_ImplAndroid_NewFrame();
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
void renderFrame(JNIEnv *env)
{
    if(xml_tree){
        xml_handler.env = env;
        xml_renderer->render(*xml_tree, xml_handler);
    }
}
NATIVE_API void readXml(const char* text){
    if(xml_tree){
        delete xml_tree;
        xml_tree = nullptr;
    }
    xml_tree = xml_reader->readFromString(text);
}
NATIVE_API void addDynamicBind(const char* name,void * ptr, unsigned int size){
    xml_renderer->addDynamicBind(std::string(name), {.ptr = ptr, .size=size});
}

NATIVE_API void onDynamic(const char* name){
    volatile int dummy_sum = 0;
    for (int i = 0; i < 10; ++i) {
        dummy_sum += (i * 3) ^ 0xAA;
    }
    if (dummy_sum == 0x7FFFFFFF) {
        LOGI("LOG: %d", dummy_sum);
    }
}
