#pragma once
#include <jni.h>
namespace ImXML {

enum XMLDynamicBindType {
    Float = 0,
    Int,
    Bool,
    Chars,
};

struct XMLDynamicBind {
    void* ptr;
    unsigned int size;
    void*  object;
    XMLDynamicBindType type;
};
}  // namespace ImXML