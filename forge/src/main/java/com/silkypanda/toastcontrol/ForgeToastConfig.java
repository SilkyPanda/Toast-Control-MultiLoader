package com.silkypanda.toastcontrol;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

final class ForgeToastConfig {

    static final ForgeConfigSpec SPEC;

    private static final BooleanValue ADVANCEMENTS;
    private static final BooleanValue RECIPES;
    private static final BooleanValue SYSTEM;
    private static final BooleanValue TUTORIAL;
    private static final BooleanValue GLOBAL_VANILLA;
    private static final BooleanValue GLOBAL_MODDED;
    private static final BooleanValue GLOBAL;
    private static final ConfigValue<List<? extends String>> BLOCKED_CLASSES;

    private static final IntValue FORCE_TIME;
    private static final BooleanValue TRANSLUCENT;
    private static final BooleanValue TRANSPARENT;
    private static final IntValue TOAST_COUNT;
    private static final IntValue OFFSET_X;
    private static final IntValue OFFSET_Y;
    private static final BooleanValue NO_SLIDE;
    private static final BooleanValue START_LEFT;
    private static final BooleanValue TOP_DOWN;

    private static final BooleanValue PRINT_CLASSES;

    static {
        ForgeConfigSpec.Builder build = new ForgeConfigSpec.Builder();
        build.comment("Client Configuration").push("client").push("blocked_toasts");

        ADVANCEMENTS = build.comment("If advancement toasts are blocked.").define("advancements", false);
        RECIPES = build.comment("If recipe toasts are blocked.").define("recipes", false);
        SYSTEM = build.comment("If system toasts are blocked.").define("system", false);
        TUTORIAL = build.comment("If tutorial toasts are blocked.").define("tutorial", true);
        GLOBAL_VANILLA = build.comment("If all vanilla toasts are blocked.").define("global_vanilla", false);
        GLOBAL_MODDED = build.comment("If all non-vanilla toasts are blocked.").define("global_modded", false);
        GLOBAL = build.comment("If all toasts are blocked.").define("global", false);
        BLOCKED_CLASSES = build.comment("Toast Classes that are blocked from being shown.").defineListAllowEmpty("blocked_classes", new ArrayList<String>(), o -> o instanceof String);

        build.pop().push("visual_options");
        FORCE_TIME = build.comment("How long a toast must be on the screen for, in ticks.  Use 0 to use the default time.").defineInRange("force_time", 0, 0, 4000);
        TRANSLUCENT = build.comment("If toasts are translucent.").define("translucent", true);
        TRANSPARENT = build.comment("If toasts are transparent.  Overrides translucency.").define("transparent", false);
        TOAST_COUNT = build.comment("How many toasts will be displayed on screen at once.").defineInRange("toast_count", 3, 1, 7);
        OFFSET_X = build.comment("The X offset for toasts to be drawn at.").defineInRange("x_offset", 0, -8192, 8192);
        OFFSET_Y = build.comment("The Y offset for toasts to be drawn at.").defineInRange("y_offset", 0, -8192, 8192);
        NO_SLIDE = build.comment("If toasts automatically pop into the screen without animations.").define("no_slide", false);
        START_LEFT = build.comment("If toasts show on the left of the screen.").define("start_left", false);
        TOP_DOWN = build.comment("If toasts will come in from the top of the screen, rather than the side.").define("top_down", false);

        build.pop().push("debug");
        PRINT_CLASSES = build.comment("If toast classes are printed when they are shown.").define("print_classes", false);
        build.pop().pop();

        SPEC = build.build();
    }

    static void sync() {
        ToastConfig.advancements = ADVANCEMENTS.get();
        ToastConfig.recipes = RECIPES.get();
        ToastConfig.system = SYSTEM.get();
        ToastConfig.tutorial = TUTORIAL.get();
        ToastConfig.globalVanilla = GLOBAL_VANILLA.get();
        ToastConfig.globalModded = GLOBAL_MODDED.get();
        ToastConfig.global = GLOBAL.get();
        ToastConfig.blockedClasses = new ArrayList<String>(BLOCKED_CLASSES.get());

        ToastConfig.forceTime = FORCE_TIME.get();
        ToastConfig.translucent = TRANSLUCENT.get();
        ToastConfig.transparent = TRANSPARENT.get();
        ToastConfig.toastCount = TOAST_COUNT.get();
        ToastConfig.offsetX = OFFSET_X.get();
        ToastConfig.offsetY = OFFSET_Y.get();
        ToastConfig.noSlide = NO_SLIDE.get();
        ToastConfig.startLeft = START_LEFT.get();
        ToastConfig.topDown = TOP_DOWN.get();

        ToastConfig.printClasses = PRINT_CLASSES.get();
    }

    private ForgeToastConfig() {}

}
