#pragma once

#include "imgui.h"
#include "XMLDynamicBind.h"
#include "XMLEventHandler.h"
#include "XMLTree.h"

#include <unordered_map>

namespace ImXML {
class XMLRenderer {
   private:
    std::unordered_map<std::string, XMLDynamicBind> dynamicBinds;

    bool onNodeBegin(XMLNode& node, XMLEventHandler& handler);

    void onNodeEnd(XMLNode& node, XMLEventHandler& handler);

    void traverse(XMLNode& root, XMLEventHandler& handler);



   public:
    XMLRenderer(/* args */);
    ~XMLRenderer();

    void render(XMLTree& tree, XMLEventHandler& handler);
    bool checkDynamicBind(const std::string& name);
    void addDynamicBind(const std::string& name, const XMLDynamicBind& bind);
    XMLDynamicBind getDynamicBind(const XMLNode& node);

};
}  // namespace ImXML