package com.silkypanda.toastcontrol;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class NeoForgeConfig {

    public static final ModConfigSpec SPEC;
    private static final NeoForgeConfig INSTANCE;
    static {
        Pair<NeoForgeConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    private final BooleanValue advancements;
    private final BooleanValue recipes;
    private final BooleanValue system;
    private final BooleanValue tutorial;
    private final BooleanValue globalVanilla;
    private final BooleanValue globalModded;
    private final BooleanValue global;
    private final ConfigValue<List<? extends String>> blockedClasses;

    private final IntValue forceTime;
    private final BooleanValue translucent;
    private final BooleanValue transparent;
    private final IntValue toastCount;
    private final IntValue offsetX;
    private final IntValue offsetY;
    private final BooleanValue noSlide;
    private final BooleanValue startLeft;
    private final BooleanValue topDown;

    private final BooleanValue printClasses;

    private NeoForgeConfig(ModConfigSpec.Builder build) {
        build.comment("Client Configuration").push("client").push("blocked_toasts");

        this.advancements = build.comment("If advancement toasts are blocked.").define("advancements", false);
        this.recipes = build.comment("If recipe toasts are blocked.").define("recipes", false);
        this.system = build.comment("If system toasts are blocked.").define("system", false);
        this.tutorial = build.comment("If tutorial toasts are blocked.").define("tutorial", true);
        this.globalVanilla = build.comment("If all vanilla toasts are blocked.").define("global_vanilla", false);
        this.globalModded = build.comment("If all non-vanilla toasts are blocked.").define("global_modded", false);
        this.global = build.comment("If all toasts are blocked.").define("global", false);
        this.blockedClasses = build.comment("Toast Classes that are blocked from being shown.").defineList("blocked_classes", new ArrayList<>(), () -> "some.package.name.ClassName", Predicates.alwaysTrue());

        build.pop().push("visual_options");
        this.forceTime = build.comment("How long a toast must be on the screen for, in ticks.  Use 0 to use the default time.").defineInRange("force_time", 0, 0, 4000);
        this.translucent = build.comment("If toasts are translucent.").define("translucent", true);
        this.transparent = build.comment("If toasts are transparent.  Overrides translucency.").define("transparent", false);
        this.toastCount = build.comment("How many toasts will be displayed on screen at once.").defineInRange("toast_count", 3, 1, 7);
        this.offsetX = build.comment("The X offset for toasts to be drawn at.").defineInRange("x_offset", 0, -8192, 8192);
        this.offsetY = build.comment("The Y offset for toasts to be drawn at.").defineInRange("y_offset", 0, -8192, 8192);
        this.noSlide = build.comment("If toasts automatically pop into the screen without animations.").define("no_slide", false);
        this.startLeft = build.comment("If toasts show on the left of the screen.").define("start_left", false);
        this.topDown = build.comment("If toasts will come in from the top of the screen, rather than the side.").define("top_down", false);

        build.pop().push("debug");
        this.printClasses = build.comment("If toast classes are printed when they are shown.").define("print_classes", false);
        build.pop().pop();
    }

    static void sync() {
        NeoForgeConfig c = INSTANCE;
        ToastConfig.advancements = c.advancements.get();
        ToastConfig.recipes = c.recipes.get();
        ToastConfig.system = c.system.get();
        ToastConfig.tutorial = c.tutorial.get();
        ToastConfig.globalVanilla = c.globalVanilla.get();
        ToastConfig.globalModded = c.globalModded.get();
        ToastConfig.global = c.global.get();
        ToastConfig.blockedClasses = new ArrayList<String>(c.blockedClasses.get());

        ToastConfig.forceTime = c.forceTime.get();
        ToastConfig.translucent = c.translucent.get();
        ToastConfig.transparent = c.transparent.get();
        ToastConfig.toastCount = c.toastCount.get();
        ToastConfig.offsetX = c.offsetX.get();
        ToastConfig.offsetY = c.offsetY.get();
        ToastConfig.noSlide = c.noSlide.get();
        ToastConfig.startLeft = c.startLeft.get();
        ToastConfig.topDown = c.topDown.get();

        ToastConfig.printClasses = c.printClasses.get();
    }

}
