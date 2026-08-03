//
// Created by Administrator on 2026/8/2.
//

#include "ImGuiValue.h"
#include <thread>
constexpr int MAX_SIZE = 1024;
ImGuiValue::ImGuiValue(JNIEnv *env, jobject value) {
    jclass valueClass = env->GetObjectClass(value);
    this->value =  env->NewGlobalRef(value);;
    bind.object = this;
    switch (env->GetIntField(value, env->GetFieldID(valueClass, "Type", "I"))) {
        case ImXML::XMLDynamicBindType::Int:
            fieldID = env->GetFieldID(valueClass, "Int", "I");
            bind.ptr = new int(env->GetIntField(value, fieldID));
            bind.size = sizeof(int);
            bind.type = ImXML::XMLDynamicBindType::Int;
            break;
        case ImXML::XMLDynamicBindType::Float:
            fieldID = env->GetFieldID(valueClass, "Float", "F");
            bind.ptr = new float(env->GetFloatField(value, fieldID));
            bind.size = sizeof(float);
            bind.type = ImXML::XMLDynamicBindType::Float;
            break;
        case ImXML::XMLDynamicBindType::Bool:
            fieldID = env->GetFieldID(valueClass, "Bool", "Z");
            bind.ptr = new bool(env->GetBooleanField(value, fieldID) == JNI_TRUE);
            bind.size = sizeof(bool);
            bind.type = ImXML::XMLDynamicBindType::Bool;
            break;
        case ImXML::XMLDynamicBindType::Chars:
            fieldID = env->GetFieldID(valueClass, "Chars", "Ljava/lang/String;");
            auto jStr = reinterpret_cast<jstring>(env->GetObjectField(value, fieldID));
            jsize javaStrLen = env->GetStringLength(jStr);
            char* textBuffer = new char[MAX_SIZE];
            jsize copyLen = (javaStrLen > MAX_SIZE - 1) ? MAX_SIZE - 1 : javaStrLen;
            env->GetStringUTFRegion(jStr, 0, copyLen, textBuffer);
            textBuffer[copyLen] = '\0';
            bind.ptr = textBuffer;
            bind.size = MAX_SIZE;
            bind.type = ImXML::XMLDynamicBindType::Chars;
            break;
    }
}



ImGuiValue::~ImGuiValue(){
    if(this->bind.ptr != nullptr){
        switch (this->bind.type) {
            case ImXML::XMLDynamicBindType::Float:
                delete static_cast<float*>(this->bind.ptr);
                break;
            case ImXML::XMLDynamicBindType::Int:
                delete static_cast<int*>(this->bind.ptr);
                break;
            case ImXML::XMLDynamicBindType::Bool:
                delete static_cast<bool*>(this->bind.ptr);
                break;
            case ImXML::XMLDynamicBindType::Chars:
                delete static_cast<char*>(this->bind.ptr);
                break;
        }
        this->bind.ptr = nullptr;
    }
}

void ImGuiValue::updata(JNIEnv *env) {
    if(this->bind.ptr != nullptr){
        switch (this->bind.type) {
            case ImXML::XMLDynamicBindType::Float:
                env->SetFloatField(value, fieldID, *static_cast<float*>(this->bind.ptr));
                break;
            case ImXML::XMLDynamicBindType::Int:
                env->SetIntField(value, fieldID, *static_cast<int*>(this->bind.ptr));
                break;

            case ImXML::XMLDynamicBindType::Bool:
                env->SetBooleanField(value, fieldID, (*static_cast<bool*>(this->bind.ptr))?JNI_TRUE:JNI_FALSE);
                break;
            case ImXML::XMLDynamicBindType::Chars:
                env->SetObjectField(value, fieldID, env->NewStringUTF(static_cast<char*>(this->bind.ptr)));
                break;
        }
    }
};
