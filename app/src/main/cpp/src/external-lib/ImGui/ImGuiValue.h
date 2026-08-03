//
// Created by Administrator on 2026/8/2.
//

#ifndef IMGUI_IMGUIVALUE_H
#define IMGUI_IMGUIVALUE_H
#include "XMLDynamicBind.h"
#include "XMLEventHandler.h"
#include "XMLReader.h"
#include "XMLRenderer.h"
#include "XMLTree.h"
#include <jni.h>

class ImGuiValue {
    jfieldID fieldID;
    jobject value;
public:
    ImGuiValue(JNIEnv *env, jobject value);
    ~ImGuiValue();
    ImXML::XMLDynamicBind bind{};

    void updataVM(JavaVM *vm);
    void updata(JNIEnv *env);
};


#endif //IMGUI_IMGUIVALUE_H
