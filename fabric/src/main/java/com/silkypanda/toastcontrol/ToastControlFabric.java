package com.silkypanda.toastcontrol;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ToastControlFabric implements ClientModInitializer {

    private int ticks;

    @Override
    public void onInitializeClient() {
        ConfigFile config = new ConfigFile(FabricLoader.getInstance().getConfigDir().resolve("toastcontrol.json"));
        config.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ToastControl.MODID, "category"));
        KeyMapping clear = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.toastcontrol.clear", InputConstants.Type.KEYSYM, InputConstants.KEY_J, category));

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.gui.toastManager = new BetterToastManager(client, client.options);
            ToastControl.applyConfig();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clear.consumeClick()) {
                client.gui.toastManager.clear();
            }

            if (++this.ticks % 20 == 0 && config.reloadIfChanged()) {
                ToastControl.applyConfig();
                ToastControl.LOGGER.info("Toast control config reloaded.");
            }
        });
    }

}
