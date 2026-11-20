package dev.shadowsoffire.toastcontrol;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.resources.Identifier;

public class ToastControl implements ClientModInitializer {

    public static final String MODID = "toastcontrol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final List<Class<?>> BLOCKED_CLASSES = new ArrayList<>();

    /** How often (in ticks) the config file is checked for changes. */
    private static final int CONFIG_POLL_INTERVAL = 20;

    private static KeyMapping clearKey;
    private static int pollTimer = 0;

    @Override
    public void onInitializeClient() {
        ToastConfig.INSTANCE.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MODID, "category"));
        clearKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toastcontrol.clear", InputConstants.Type.KEYSYM, InputConstants.KEY_J, category));

        // Swap in our toast manager once the client is fully up, then apply the config.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.toastManager = new BetterToastManager(client, client.options);
            applyConfig();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clearKey.consumeClick()) {
                client.getToastManager().clear();
            }

            if (++pollTimer >= CONFIG_POLL_INTERVAL) {
                pollTimer = 0;
                if (ToastConfig.INSTANCE.reloadIfChanged()) {
                    applyConfig();
                    LOGGER.info("Toast control config reloaded.");
                }
            }
        });
    }

    static void applyConfig() {
        handleToastReloc();
        handleBlockedClasses();
    }

    static void handleToastReloc() {
        Identifier[] targets = new Identifier[] { AdvancementToast.BACKGROUND_SPRITE, RecipeToast.BACKGROUND_SPRITE, SystemToast.BACKGROUND_SPRITE, TutorialToast.BACKGROUND_SPRITE };
        String[] paths = new String[] { "advancement", "recipe", "system", "tutorial" };

        if (ToastConfig.isTextureTransparent()) {
            for (Identifier t : targets) {
                change(t, Identifier.fromNamespaceAndPath(MODID, "toast/transparent"));
            }
        }
        else if (ToastConfig.isTextureTranslucent()) {
            for (int i = 0; i < 4; i++) {
                change(targets[i], Identifier.fromNamespaceAndPath(MODID, "toast/translucent/" + paths[i]));
            }
        }
        else {
            for (int i = 0; i < 4; i++) {
                change(targets[i], Identifier.withDefaultNamespace("toast/" + paths[i]));
            }
        }
    }

    static void handleBlockedClasses() {
        BLOCKED_CLASSES.clear();
        for (String s : ToastConfig.INSTANCE.blockedClasses) {
            try {
                Class<?> c = Class.forName(s);
                BLOCKED_CLASSES.add(c);
            }
            catch (ClassNotFoundException e) {
                LOGGER.error("Invalid class string provided to toast control: {}", s);
            }
        }
    }

    private static final Field NAMESPACE_FIELD = identifierField("namespace");
    private static final Field PATH_FIELD = identifierField("path");

    private static Field identifierField(String name) {
        try {
            Field f = Identifier.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Could not access Identifier#{}, toast texture options will not work.", name, e);
            return null;
        }
    }

    /**
     * Rewrites the contents of {@code target} so that it points at the same location as {@code source}.
     * This is how the vanilla toast background sprite constants are redirected to our sprites.
     */
    private static void change(Identifier target, Identifier source) {
        if (NAMESPACE_FIELD == null || PATH_FIELD == null) {
            return;
        }
        try {
            NAMESPACE_FIELD.set(target, source.getNamespace());
            PATH_FIELD.set(target, source.getPath());
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Failed to change toast sprite {} to {}.", target, source, e);
        }
    }

    /**
     * Determines if a toast should be prevented from showing, based on the current configuration.
     */
    static boolean isBlocked(Toast toast) {
        ToastConfig cfg = ToastConfig.INSTANCE;

        if (cfg.printClasses) {
            LOGGER.info("{}", toast.getClass());
        }

        if (cfg.global || cfg.globalVanilla && isVanillaToast(toast)) {
            return true;
        }

        if (cfg.globalModded && !isVanillaToast(toast)) {
            return true;
        }

        if (BLOCKED_CLASSES.contains(toast.getClass())) {
            return true;
        }

        return toast instanceof AdvancementToast && cfg.advancements
            || toast instanceof RecipeToast && cfg.recipes
            || toast instanceof SystemToast && cfg.system
            || toast instanceof TutorialToast && cfg.tutorial;
    }

    private static boolean isVanillaToast(Toast toast) {
        return toast instanceof AdvancementToast || toast instanceof RecipeToast || toast instanceof SystemToast || toast instanceof TutorialToast;
    }

}
