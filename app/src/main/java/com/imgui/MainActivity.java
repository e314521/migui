package com.imgui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.imgui.databinding.ActivityMainBinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;


@SuppressLint("DiscouragedPrivateApi")
public class MainActivity extends AppCompatActivity {
    private ImGuiValue float0 = new ImGuiValue(2.0F);
    private ImGuiValue float1 = new ImGuiValue(2.0F);
    private ImGuiValue str0 = new ImGuiValue("str01");
    private ImGuiValue str1 = new ImGuiValue("str1");
    private ImGuiValue str2 = new ImGuiValue("str2");
    private ImGuiValue str3 = new ImGuiValue("str3");
    private ImGuiValue color0 = new ImGuiValue(1);
    private ImGuiValue color1 = new ImGuiValue(1);

    private ImGuiValue int0 = new ImGuiValue(1);
    private ImGuiValue bool0 = new ImGuiValue(true);
    static {
        System.loadLibrary("imgui");
    }

    // Used to load the 'imgui' library on application startup.
    private ActivityMainBinding binding;
    static class ProxyInstrumentation extends Instrumentation {
        private final Instrumentation original;

        public ProxyInstrumentation(Instrumentation original) {
            this.original = original;
        }

        @Override
        public void callActivityOnResume(Activity activity) {
            // 3. 拦截onResume逻辑
            Log.d("HOOK", "Before onResume: " + activity.getClass().getSimpleName());
            original.callActivityOnResume(activity);
            Log.d("HOOK", "After onResume: " + activity.getClass().getSimpleName());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new ImGuiView(this);

    }
    public void triggerButtonClick(View but) {
        ImGuiView.addDynamicBind("float0",this.float0);
        ImGuiView.addDynamicBind("float1",this.float1);
        ImGuiView.addDynamicBind("str0",this.str0);
        ImGuiView.addDynamicBind("str1",this.str1);
        ImGuiView.addDynamicBind("str2",this.str2);
        ImGuiView.addDynamicBind("str3",this.str3);
        ImGuiView.addDynamicBind("color0",this.color0);
        ImGuiView.addDynamicBind("color1",this.color1);
        ImGuiView.addDynamicBind("bool0",this.bool0);
        ImGuiView.addDynamicBind("int0",this.int0);
        ImGuiView.readXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<begin name=\"你是谁你是谁\" flags=\"ImGuiWindowFlags_MenuBar | ImGuiWindowFlags_AutoCollapsed | ImGuiWindowFlags_AlwaysAutoResize\">\n" +
                "\t<menubar>\n" +
                "\t\t<menu label=\"File\">\n" +
                "\t\t\t<menuitem label=\"New...\"/>\n" +
                "\t\t\t<menu label=\"Open...\">\n" +
                "\t\t\t\t<menuitem label=\"File1.txt\" />\n" +
                "\t\t\t</menu>\n" +
                "\t\t</menu>\n" +
                "\t\t<menu label=\"Edit\">\n" +
                "\t\t\t<menuitem label=\"Foo\"/>\n" +
                "\t\t\t<menuitem label=\"Bar\"/>\n" +
                "\t\t</menu>\n" +
                "\t</menubar>\n" +
                "\t<table name=\"table0\" columns=\"3\">\n" +
                "\t\t<setupcolumn label=\"Column 0\" width=\"0.33\" flags=\"ImGuiTableColumnFlags_WidthStretch\"/>\n" +
                "\t\t<setupcolumn label=\"Column 1\" width=\"0.33\" flags=\"ImGuiTableColumnFlags_WidthStretch\"/>\n" +
                "\t\t<setupcolumn label=\"Column 2\" width=\"0.33\" flags=\"ImGuiTableColumnFlags_WidthStretch\"/>\n" +
                "\t\t<header/>\n" +
                "\t\t<row>\n" +
                "\t\t\t<column>\n" +
                "\t\t\t\t<text label=\"Text\"/>\n" +
                "\t\t\t\t<button id=\"btn0\" label=\"Hello!\"/>\n" +
                "\t\t\t\t<separatortext label=\"SeparatorText\"/>\n" +
                "\t\t\t\t<sliderfloat label=\"float\" dynamic=\"float0\" min=\"0\" max=\"1\" />\n" +
                "\t\t\t\t<inputtext label=\"string\" dynamic=\"str0\"/>\n" +
                "\t\t\t</column>\n" +
                "\t\t\t<column>\n" +
                "\t\t\t\t<treenode label=\"treeroot\">\n" +
                "\t\t\t\t\t<treenode label=\"tree0\" />\n" +
                "\t\t\t\t\t<treenode label=\"tree1\">\n" +
                "\t\t\t\t\t\t<treenode label=\"tree2\" />\n" +
                "\t\t\t\t\t</treenode>\n" +
                "\t\t\t\t</treenode>\n" +
                "\t\t\t\t<separator/>\n" +
                "\t\t\t\t<group>\n" +
                "\t\t\t\t\t<colorpicker3 dynamic=\"color0\" />\n" +
                "\t\t\t\t</group>\n" +
                "\t\t\t\t<separator/>\n" +
                "\t\t\t\t<group>\n" +
                "\t\t\t\t\t<PopupContextWindow>\n" +
                "\t\t\t\t\t\t<text label=\"context popup\"/>\n" +
                "\t\t\t\t\t</PopupContextWindow>\n" +
                "\t\t\t\t\t<text label=\"right click to open popup\"/>\n" +
                "\t\t\t\t</group>\n" +
                "\t\t\t</column>\n" +
                "\t\t\t<column>\n" +
                "\t\t\t\t<combo label=\"Combo\" preview_value=\"Preview\" >\n" +
                "\t\t\t\t\t<selectable label=\"Option 1\" selected=\"true\" />\n" +
                "\t\t\t\t\t<selectable label=\"Option 2\" selected=\"false\" />\n" +
                "\t\t\t\t\t<selectable label=\"Option 3\" selected=\"false\" />\n" +
                "\t\t\t\t\t<selectable label=\"Option 4\" selected=\"false\" />\n" +
                "\t\t\t\t</combo>\n" +
                "\t\t\t\t<inputfloat label=\"Input float\" dynamic=\"float1\" step=\"0.1\" step_fast=\"0.25\" min=\"0.25\" max=\"1\" format=\"%.4f\"/>\n" +
                "\t\t\t\t<inputint label=\"Input int\" dynamic=\"int0\" step=\"1\" step_fast=\"10\" min=\"1\" max=\"100\"/>\n" +
                "\t\t\t\t<checkbox label=\"Checkbox\" dynamic=\"bool0\"/>\n" +
                "\t\t\t</column>\n" +
                "\t\t</row>\n" +
                "\t</table>\n" +
                "\t<child label=\"Child window\">\n" +
                "\t\t<tabbar id=\"tabbar0\">\n" +
                "\t\t\t<tabitem label=\"Tab 1\">\n" +
                "\t\t\t\t<InputTextMultiline label=\"Sample text\" dynamic=\"str1\" />\n" +
                "\t\t\t</tabitem>\n" +
                "\t\t\t<tabitem label=\"Tab 2\">\n" +
                "\t\t\t\t<InputTextMultiline label=\"Another Sample text\" dynamic=\"str2\" />\n" +
                "\t\t\t</tabitem>\n" +
                "\t\t\t<tabitem label=\"Tab 3\">\n" +
                "\t\t\t\t<InputTextMultiline label=\"Yet Another Sample text\" dynamic=\"str3\" />\n" +
                "\t\t\t</tabitem>\n" +
                "\t\t</tabbar>\n" +
                "\t</child>\n" +
                "</begin>\n");
    }



    /**
     * A native method that is implemented by the 'imgui' native library,
     * which is packaged with this application.
     */

}