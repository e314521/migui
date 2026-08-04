//
// Created by Administrator on 2025/6/19.
//

#ifndef IMGUI_DEMO_IMGUI_VIEW_H
#define IMGUI_DEMO_IMGUI_VIEW_H

#include "logging.h"


#include <thread>
#include <jni.h>
#include <fcntl.h>

#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include "external-lib/ImGui/imgui.h"
#include "external-lib/ImGui/imgui_internal.h"
#include "external-lib/ImGui/imgui_impl_opengl3.h"
#include "external-lib/ImGui/imgui_impl_android.h"
#include "android/native_window_jni.h"
#include "external-lib/ImGui/XMLDynamicBind.h"
#include "external-lib/ImGui/XMLEventHandler.h"
#include "external-lib/ImGui/XMLReader.h"
#include "external-lib/ImGui/XMLRenderer.h"
#include "external-lib/ImGui/XMLTree.h"
#include "external-lib/ImGui/ImGuiValue.h"

using namespace std;
#define NATIVE_API __attribute__((visibility("default"))) __attribute__((optnone))
extern "C" {
NATIVE_API void readXml(const char* text);
NATIVE_API void addDynamicBind(const char* name,void * ptr, unsigned int size, int type);
NATIVE_API void onDynamic(const char* name);
}

#endif //IMGUI_DEMO_IMGUI_VIEW_H
