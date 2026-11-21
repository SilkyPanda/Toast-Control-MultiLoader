package com.silkypanda.toastcontrol;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ToastControl.MODID, dist = Dist.CLIENT)
public class ToastControlNeoForge {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(ToastControl.MODID, "category"));
    public static final KeyMapping CLEAR = new KeyMapping("key.toastcontrol.clear", InputConstants.KEY_J, CATEGORY);

    public ToastControlNeoForge(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.SPEC);
        bus.register(this);
        NeoForge.EVENT_BUS.register(Events.class);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent e) {
        NeoForgeConfig.sync();
        Minecraft mc = Minecraft.getInstance();
        mc.toastManager = new BetterToastManager(mc, mc.options);
        ToastControl.applyConfig();
    }

    @SubscribeEvent
    public void keyReg(RegisterKeyMappingsEvent e) {
        e.registerCategory(CATEGORY);
        e.register(CLEAR);
    }

    @SubscribeEvent
    public void configLoaded(ModConfigEvent.Loading e) {
        if (ToastControl.MODID.equals(e.getConfig().getModId())) {
            NeoForgeConfig.sync();
        }
    }

    @SubscribeEvent
    public void configReloaded(ModConfigEvent.Reloading e) {
        if (ToastControl.MODID.equals(e.getConfig().getModId())) {
            NeoForgeConfig.sync();
            Minecraft.getInstance().submit(() -> {
                ToastControl.applyConfig();
                ToastControl.LOGGER.info("Toast control config reloaded.");
            });
        }
    }

    public static class Events {

        @SubscribeEvent
        public static void keys(InputEvent.Key e) {
            if (CLEAR.isDown()) {
                Minecraft.getInstance().getToastManager().clear();
            }
        }

    }

}
