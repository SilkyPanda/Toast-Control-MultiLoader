package com.silkypanda.toastcontrol;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

final class ConfigFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private long lastModified = -1;

    ConfigFile(Path path) {
        this.path = path;
    }

    void load() {
        Data data = new Data();

        if (Files.exists(this.path)) {
            try (Reader reader = Files.newBufferedReader(this.path)) {
                Data read = GSON.fromJson(reader, Data.class);
                if (read != null) {
                    data = read;
                }
            }
            catch (IOException | JsonParseException e) {
                ToastControl.LOGGER.error("Could not read {}, keeping the current settings", this.path, e);
                return;
            }
        }

        data.apply();
        this.save(data);
    }

    boolean reloadIfChanged() {
        try {
            if (Files.exists(this.path) && Files.getLastModifiedTime(this.path).toMillis() != this.lastModified) {
                this.load();
                return true;
            }
        }
        catch (IOException e) {
            // try again next time
        }
        return false;
    }

    private void save(Data data) {
        try {
            Files.createDirectories(this.path.getParent());
            Files.writeString(this.path, GSON.toJson(data));
            this.lastModified = Files.getLastModifiedTime(this.path).toMillis();
        }
        catch (IOException e) {
            ToastControl.LOGGER.error("Could not write {}", this.path, e);
        }
    }

    private static class Data {

        @SerializedName("blocked_toasts")
        Blocked blocked = new Blocked();

        @SerializedName("visual_options")
        Visual visual = new Visual();

        Debug debug = new Debug();

        void apply() {
            if (this.blocked == null) this.blocked = new Blocked();
            if (this.visual == null) this.visual = new Visual();
            if (this.debug == null) this.debug = new Debug();

            ToastConfig.advancements = this.blocked.advancements;
            ToastConfig.recipes = this.blocked.recipes;
            ToastConfig.system = this.blocked.system;
            ToastConfig.tutorial = this.blocked.tutorial;
            ToastConfig.globalVanilla = this.blocked.globalVanilla;
            ToastConfig.globalModded = this.blocked.globalModded;
            ToastConfig.global = this.blocked.global;
            ToastConfig.blockedClasses = this.blocked.blockedClasses == null ? new ArrayList<>() : this.blocked.blockedClasses;

            ToastConfig.forceTime = Math.clamp(this.visual.forceTime, 0, 4000);
            ToastConfig.translucent = this.visual.translucent;
            ToastConfig.transparent = this.visual.transparent;
            ToastConfig.toastCount = Math.clamp(this.visual.toastCount, 1, 7);
            ToastConfig.offsetX = Math.clamp(this.visual.offsetX, -8192, 8192);
            ToastConfig.offsetY = Math.clamp(this.visual.offsetY, -8192, 8192);
            ToastConfig.noSlide = this.visual.noSlide;
            ToastConfig.startLeft = this.visual.startLeft;
            ToastConfig.topDown = this.visual.topDown;

            ToastConfig.printClasses = this.debug.printClasses;
        }

    }

    private static class Blocked {
        boolean advancements = false;
        boolean recipes = false;
        boolean system = false;
        boolean tutorial = true;
        @SerializedName("global_vanilla")
        boolean globalVanilla = false;
        @SerializedName("global_modded")
        boolean globalModded = false;
        boolean global = false;
        @SerializedName("blocked_classes")
        List<String> blockedClasses = new ArrayList<>();
    }

    private static class Visual {
        @SerializedName("force_time")
        int forceTime = 0;
        boolean translucent = true;
        boolean transparent = false;
        @SerializedName("toast_count")
        int toastCount = 3;
        @SerializedName("x_offset")
        int offsetX = 0;
        @SerializedName("y_offset")
        int offsetY = 0;
        @SerializedName("no_slide")
        boolean noSlide = false;
        @SerializedName("start_left")
        boolean startLeft = false;
        @SerializedName("top_down")
        boolean topDown = false;
    }

    private static class Debug {
        @SerializedName("print_classes")
        boolean printClasses = false;
    }

}
