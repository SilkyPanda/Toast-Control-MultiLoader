package com.silkypanda.toastcontrol;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.resources.Identifier;

public class ToastControl {

    public static final String MODID = "toastcontrol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static final List<Class<?>> BLOCKED_CLASSES = new ArrayList<>();

    private static final Field NAMESPACE = identifierField("namespace");
    private static final Field PATH = identifierField("path");

    public static void applyConfig() {
        handleToastReloc();
        handleBlockedClasses();
    }

    static void handleToastReloc() {
        Identifier[] targets = { AdvancementToast.BACKGROUND_SPRITE, RecipeToast.BACKGROUND_SPRITE, SystemToast.BACKGROUND_SPRITE, TutorialToast.BACKGROUND_SPRITE };
        String[] paths = { "advancement", "recipe", "system", "tutorial" };

        for (int i = 0; i < targets.length; i++) {
            if (ToastConfig.transparent) {
                change(targets[i], Identifier.fromNamespaceAndPath(MODID, "toast/transparent"));
            }
            else if (ToastConfig.translucent) {
                change(targets[i], Identifier.fromNamespaceAndPath(MODID, "toast/translucent/" + paths[i]));
            }
            else {
                change(targets[i], Identifier.withDefaultNamespace("toast/" + paths[i]));
            }
        }
    }

    static void handleBlockedClasses() {
        BLOCKED_CLASSES.clear();
        for (String name : ToastConfig.blockedClasses) {
            try {
                BLOCKED_CLASSES.add(Class.forName(name));
            }
            catch (ClassNotFoundException e) {
                LOGGER.error("Invalid class string provided to toast control: {}", name);
            }
        }
    }

    private static Field identifierField(String name) {
        try {
            Field field = Identifier.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Could not access Identifier.{}, toast textures will not change", name, e);
            return null;
        }
    }

    private static void change(Identifier target, Identifier source) {
        if (NAMESPACE == null || PATH == null) {
            return;
        }
        try {
            NAMESPACE.set(target, source.getNamespace());
            PATH.set(target, source.getPath());
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Could not change toast sprite {} to {}", target, source, e);
        }
    }

    static boolean isBlocked(Toast toast) {
        if (ToastConfig.printClasses) {
            LOGGER.info("{}", toast.getClass());
        }

        if (ToastConfig.global || ToastConfig.globalVanilla && isVanillaToast(toast)) {
            return true;
        }

        if (ToastConfig.globalModded && !isVanillaToast(toast)) {
            return true;
        }

        if (BLOCKED_CLASSES.contains(toast.getClass())) {
            return true;
        }

        return toast instanceof AdvancementToast && ToastConfig.advancements
            || toast instanceof RecipeToast && ToastConfig.recipes
            || toast instanceof SystemToast && ToastConfig.system
            || toast instanceof TutorialToast && ToastConfig.tutorial;
    }

    private static boolean isVanillaToast(Toast toast) {
        return toast instanceof AdvancementToast || toast instanceof RecipeToast || toast instanceof SystemToast || toast instanceof TutorialToast;
    }

}
