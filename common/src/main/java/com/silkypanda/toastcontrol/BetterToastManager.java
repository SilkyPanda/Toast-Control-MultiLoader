package com.

silkypanda.toastcontrol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class BetterToastManager extends ToastManager {

    private final Deque<BetterToastInstance<?>> topDownList = new ArrayDeque<>();

    public BetterToastManager(Minecraft minecraft, Options options) {
        super(minecraft, options);
    }

    @Override
    public void addToast(Toast toast) {
        if (ToastControl.isBlocked(toast)) {
            return;
        }
        super.addToast(toast);
    }

    @Override
    public void update() {
        MutableBoolean soundPlayed = new MutableBoolean(false);
        this.visibleToasts.removeIf(toast -> {
            Toast.Visibility prev = toast.visibility;
            toast.update();
            if (toast.visibility != prev && soundPlayed.isFalse()) {
                soundPlayed.setTrue();
                toast.visibility.playSound(this.getMinecraft().getSoundManager());
            }

            if (toast.hasFinishedRendering()) {
                this.occupiedSlots.clear(toast.firstSlotIndex, toast.firstSlotIndex + toast.occupiedSlotCount);
                this.topDownList.remove(toast);
                return true;
            }
            return false;
        });

        if (!this.queued.isEmpty() && this.freeSlotCount() > 0) {
            this.queued.removeIf(toast -> {
                int count = toast.occcupiedSlotCount();
                int idx = this.findFreeSlotsIndex(count);
                if (idx == -1) {
                    return false;
                }

                BetterToastInstance<?> inst = new BetterToastInstance<>(toast, idx, count);
                this.visibleToasts.add(inst);
                this.occupiedSlots.set(idx, idx + count);

                // replay the slide so the existing toasts shift down
                if (ToastConfig.topDown) {
                    this.topDownList.forEach(t -> t.animationStartTime = -1L);
                }
                this.topDownList.addFirst(inst);

                SoundEvent sound = toast.getSoundEvent();
                if (sound != null && this.playedToastSounds.add(sound)) {
                    this.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, 1.0F));
                }
                return true;
            });
        }

        this.playedToastSounds.clear();
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.update();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics) {
        if (!this.getMinecraft().options.hideGui) {
            int screenWidth = graphics.guiWidth();
            if (!this.visibleToasts.isEmpty()) {
                graphics.nextStratum();
            }

            for (ToastManager.ToastInstance<?> toast : this.visibleToasts) {
                toast.extractRenderState(graphics, screenWidth);
            }

            if (this.getMinecraft().options.musicToast().get().renderToast()
                && this.nowPlayingToast != null
                && (this.getMinecraft().screen == null || !(this.getMinecraft().screen instanceof PauseScreen))) {
                this.nowPlayingToast.extractRenderState(graphics, screenWidth);
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.topDownList.clear();
    }

    @Override
    public int findFreeSlotsIndex(int requiredCount) {
        if (this.freeSlotCount() >= requiredCount) {
            int consecutive = 0;
            int max = ToastConfig.toastCount;
            for (int i = 0; i < max; i++) {
                if (this.occupiedSlots.get(i)) {
                    consecutive = 0;
                }
                else if (++consecutive == requiredCount) {
                    return i + 1 - consecutive;
                }
            }
        }
        return -1;
    }

    @Override
    public int freeSlotCount() {
        return ToastConfig.toastCount - this.occupiedSlots.cardinality();
    }

    public class BetterToastInstance<T extends Toast> extends ToastManager.ToastInstance<T> {

        protected int forcedShowTime = 0;

        protected BetterToastInstance(T toast, int firstSlotIndex, int occupiedSlotCount) {
            super(toast, firstSlotIndex, occupiedSlotCount);
        }

        @Override
        public void update() {
            this.forcedShowTime++;
            long now = Util.getMillis();
            if (this.animationStartTime == -1L) {
                this.animationStartTime = now;
                this.visibility = Toast.Visibility.SHOW;
            }

            if (this.visibility == Toast.Visibility.SHOW && now - this.animationStartTime <= 600L) {
                this.becameFullyVisibleAt = now;
            }

            this.fullyVisibleFor = now - this.becameFullyVisibleAt;
            this.calculateVisiblePortion(now);
            this.getToast().update(BetterToastManager.this, this.fullyVisibleFor);

            Toast.Visibility wanted = this.getToast().getWantedVisibility();
            // keep the toast up until the forced time has passed
            if (this.forcedShowTime <= ToastConfig.forceTime) {
                wanted = Toast.Visibility.SHOW;
            }

            if (wanted != this.visibility) {
                this.animationStartTime = now - (long) ((1.0F - this.visiblePortion) * 600.0F);
                this.visibility = wanted;
            }

            boolean wasFinished = this.hasFinishedRendering;
            this.hasFinishedRendering = this.visibility == Toast.Visibility.HIDE && now - this.animationStartTime > 600L;
            if (this.hasFinishedRendering && !wasFinished) {
                this.getToast().onFinishedRendering();
            }
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int screenWidth) {
            if (this.hasFinishedRendering) {
                return;
            }

            T toast = this.getToast();
            int width = toast.width();
            int height = toast.height();

            // vanilla only updates visiblePortion once per tick, so work it out every frame
            float portion;
            if (ToastConfig.noSlide) {
                portion = 1.0F;
            }
            else {
                float f = Mth.clamp((float) (Util.getMillis() - this.animationStartTime) / 600.0F, 0.0F, 1.0F);
                f *= f;
                portion = this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
            }

            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();

            if (ToastConfig.topDown) {
                int trueIdx = this.topDownIndex();
                float x = ToastConfig.startLeft ? 0 : screenWidth - width;
                pose.translate(x, (trueIdx - 1) * height + height * portion);
            }
            else if (ToastConfig.startLeft) {
                pose.translate(-width + width * portion, this.firstSlotIndex * height);
            }
            else {
                pose.translate(screenWidth - width * portion, this.firstSlotIndex * height);
            }

            pose.translate(ToastConfig.offsetX, ToastConfig.offsetY);
            toast.extractRenderState(graphics, BetterToastManager.this.getMinecraft().font, this.fullyVisibleFor);
            pose.popMatrix();
        }

        private int topDownIndex() {
            int trueIdx = 0;
            Iterator<BetterToastInstance<?>> it = BetterToastManager.this.topDownList.iterator();
            while (it.hasNext()) {
                if (it.next() == this) {
                    break;
                }
                trueIdx++;
            }
            return trueIdx;
        }
    }

}
