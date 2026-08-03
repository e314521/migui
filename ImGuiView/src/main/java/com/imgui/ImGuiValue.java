package com.imgui;

public class ImGuiValue {




    public enum ValueType {
        Float,
        Int,
        Bool,
        Chars
    }
    public int Int = 0;
    public float Float = 0;
    public boolean Bool = false;
    public String Chars = "";
    public int Type;

    public ImGuiValue(float v) {
        this.Float = v;
        this.Type = ValueType.Float.ordinal();
    }
    public ImGuiValue(int v) {
        this.Int = v;
        this.Type = ValueType.Int.ordinal();

    }
    public ImGuiValue(String v) {
        this.Chars = v;
        this.Type = ValueType.Chars.ordinal();
    }
    public ImGuiValue(boolean v) {
        this.Bool = v;
        this.Type = ValueType.Bool.ordinal();
    }
}
