package com.silkypanda.toastcontrol;

import java.util.ArrayList;
import java.util.List;

public final class ToastConfig {

    public static boolean advancements = false;
    public static boolean recipes = false;
    public static boolean system = false;
    public static boolean tutorial = true;
    public static boolean globalVanilla = false;
    public static boolean globalModded = false;
    public static boolean global = false;
    public static List<String> blockedClasses = new ArrayList<>();

    public static int forceTime = 0;
    public static boolean translucent = true;
    public static boolean transparent = false;
    public static int toastCount = 3;
    public static int offsetX = 0;
    public static int offsetY = 0;
    public static boolean noSlide = false;
    public static boolean startLeft = false;
    public static boolean topDown = false;

    public static boolean printClasses = false;

    private ToastConfig() {}

}
