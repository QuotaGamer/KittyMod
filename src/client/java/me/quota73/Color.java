package me.quota73;

import net.minecraft.text.Style;

public interface Color {
    public final int BLACK        = 0xFF000000; // 0
    public final int DARK_BLUE    = 0xFF0000AA; // 1
    public final int DARK_GREEN   = 0xFF00AA00; // 2
    public final int DARK_AQUA    = 0xFF00AAAA; // 3
    public final int DARK_RED     = 0xFFAA0000; // 4
    public final int DARK_PURPLE  = 0xFFAA00AA; // 5
    public final int GOLD         = 0xFFAA00AA; // 6
    public final int GRAY         = 0xFFAAAAAA; // 7
    public final int DARK_GRAY    = 0xFF555555; // 8
    public final int BLUE         = 0xFF5555FF; // 9

    public final int GREEN        = 0xFF55FF55; // a
    public final int AQUA         = 0xFF55FFFF; // b
    public final int RED          = 0xFFFF5555; // c
    public final int LIGHT_PURPLE = 0xFFFF55FF; // d
    public final int YELLOW       = 0xFFFFFF55; // e
    public final int WHITE        = 0xFFFFFFFF; // f

    public static Style toStyle(int rgb) {
        return Style.EMPTY.withColor(rgb);
    }
}
