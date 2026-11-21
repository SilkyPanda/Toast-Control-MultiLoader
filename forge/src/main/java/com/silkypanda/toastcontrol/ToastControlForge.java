package com.silkypanda.toastcontrol;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ToastControl.MODID)
public final class ToastControlForge {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ToastControl.MODID, "category"));
    private static final KeyMapping CLEAR = new KeyMapping("key.toastcontrol.clear", InputConstants.Type.KEYBOARD, InputConstants.KEY_J, CATEGORY);

    public ToastControlForge(FMLJavaModLoadingContext context) {
        BusGroup bus = context.getModBusGroup();
        context.registerConfig(ModConfig.Type.CLIENT, ForgeToastConfig.SPEC);

        FMLClientSetupEvent.getBus(bus).addListener(ToastControlForge::clientSetup);
        ModConfigEvent.Loading.getBus(bus).addListener(ToastControlForge::configLoaded);
        ModConfigEvent.Reloading.getBus(bus).addListener(ToastControlForge::configReloaded);

        RegisterKeyMappingsEvent.BUS.addListener(ToastControlForge::registerKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ToastControlForge::clientTick);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        Minecraft.getInstance().execute(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.gui.toastManager = new BetterToastManager(mc, mc.options);
            ToastControl.applyConfig();
        });
    }

    private static void configLoaded(ModConfigEvent.Loading event) {
        if (ToastControl.MODID.equals(event.getConfig().getModId())) {
            ForgeToastConfig.sync();
        }
    }

    private static void configReloaded(ModConfigEvent.Reloading event) {
        if (ToastControl.MODID.equals(event.getConfig().getModId())) {
            ForgeToastConfig.sync();
            Minecraft.getInstance().execute(() -> {
                ToastControl.applyConfig();
                ToastControl.LOGGER.info("Toast control config reloaded.");
            });
        }
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CLEAR);
    }

    private static void clientTick(TickEvent.ClientTickEvent.Post event) {
        while (CLEAR.consumeClick()) {
            Minecraft.getInstance().gui.toastManager.clear();
        }
    }

}
