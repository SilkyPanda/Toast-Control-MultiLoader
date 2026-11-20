package dev.shadowsoffire.toastcontrol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Client configuration, stored as {@code config/toastcontrol.json}.
 * <p>
 * The layout and option names mirror the old NeoForge TOML file:
 * {@code blocked_toasts}, {@code visual_options} and {@code debug} sections.
 * The file is re-read automatically while the game is running if it changes on disk.
 */
public final class ToastConfig {

    public static final ToastConfig INSTANCE = new ToastConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("toastcontrol.json");

    // blocked_toasts
    /** If advancement toasts are blocked. */
    public boolean advancements = true;
    /** If recipe toasts are blocked. */
    public boolean recipes = true;
    /** If system toasts are blocked. */
    public boolean system = false;
    /** If tutorial toasts are blocked. */
    public boolean tutorial = true;
    /** If all vanilla toasts are blocked. */
    public boolean globalVanilla = false;
    /** If all non-vanilla toasts are blocked. */
    public boolean globalModded = false;
    /** If all toasts are blocked. */
    public boolean global = false;
    /** Fully qualified toast class names that are blocked from being shown. */
    public List<String> blockedClasses = new ArrayList<>();

    // visual_options
    /** How long a toast must be on screen, in ticks. 0 uses the default time. */
    public int forceTime = 0;
    /** If toasts are translucent. */
    public boolean translucent = true;
    /** If toasts are transparent. Overrides translucency. */
    public boolean transparent = false;
    /** How many toasts are displayed on screen at once. */
    public int toastCount = 3;
    public int offsetX = 0;
    public int offsetY = 0;
    /** If toasts pop into the screen without sliding. */
    public boolean noSlide = false;
    /** If toasts show on the left of the screen. */
    public boolean startLeft = false;
    /** If toasts come in from the top of the screen, rather than the side. */
    public boolean topDown = false;

    // debug
    /** If toast classes are printed when they are shown. */
    public boolean printClasses = false;

    private long lastModified = -1L;

    private ToastConfig() {}

    public static boolean isTextureTransparent() {
        return INSTANCE.transparent;
    }

    public static boolean isTextureTranslucent() {
        return INSTANCE.translucent;
    }

    /**
     * Loads the config from disk. If the file is missing it is created with default values.
     * If it is unreadable, the current values are kept and an error is logged.
     */
    public void load() {
        if (Files.exists(FILE)) {
            try {
                JsonElement parsed = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8));
                if (parsed.isJsonObject()) {
                    this.read(parsed.getAsJsonObject());
                }
                else {
                    ToastControl.LOGGER.error("Config file {} is not a JSON object, ignoring it.", FILE);
                }
            }
            catch (IOException | RuntimeException ex) {
                ToastControl.LOGGER.error("Failed to read config file {}, keeping current values.", FILE, ex);
                this.rememberModifiedTime();
                return;
            }
        }
        // Writing back fills in any missing keys and clamps out-of-range values.
        this.save();
    }

    /**
     * Reloads the config if the file changed on disk since it was last read.
     *
     * @return true if the file was reloaded.
     */
    public boolean reloadIfChanged() {
        try {
            if (Files.exists(FILE) && Files.getLastModifiedTime(FILE).toMillis() != this.lastModified) {
                this.load();
                return true;
            }
        }
        catch (IOException ex) {
            // Ignore, we will try again on the next poll.
        }
        return false;
    }

    public void save() {
        JsonObject blocked = new JsonObject();
        blocked.addProperty("advancements", this.advancements);
        blocked.addProperty("recipes", this.recipes);
        blocked.addProperty("system", this.system);
        blocked.addProperty("tutorial", this.tutorial);
        blocked.addProperty("global_vanilla", this.globalVanilla);
        blocked.addProperty("global_modded", this.globalModded);
        blocked.addProperty("global", this.global);
        JsonArray classes = new JsonArray();
        this.blockedClasses.forEach(classes::add);
        blocked.add("blocked_classes", classes);

        JsonObject visual = new JsonObject();
        visual.addProperty("force_time", this.forceTime);
        visual.addProperty("translucent", this.translucent);
        visual.addProperty("transparent", this.transparent);
        visual.addProperty("toast_count", this.toastCount);
        visual.addProperty("x_offset", this.offsetX);
        visual.addProperty("y_offset", this.offsetY);
        visual.addProperty("no_slide", this.noSlide);
        visual.addProperty("start_left", this.startLeft);
        visual.addProperty("top_down", this.topDown);

        JsonObject debug = new JsonObject();
        debug.addProperty("print_classes", this.printClasses);

        JsonObject root = new JsonObject();
        root.add("blocked_toasts", blocked);
        root.add("visual_options", visual);
        root.add("debug", debug);

        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            ToastControl.LOGGER.error("Failed to write config file {}.", FILE, ex);
        }
        this.rememberModifiedTime();
    }

    private void read(JsonObject root) {
        JsonObject blocked = section(root, "blocked_toasts");
        this.advancements = bool(blocked, "advancements", true);
        this.recipes = bool(blocked, "recipes", true);
        this.system = bool(blocked, "system", false);
        this.tutorial = bool(blocked, "tutorial", true);
        this.globalVanilla = bool(blocked, "global_vanilla", false);
        this.globalModded = bool(blocked, "global_modded", false);
        this.global = bool(blocked, "global", false);
        this.blockedClasses = strings(blocked, "blocked_classes");

        JsonObject visual = section(root, "visual_options");
        this.forceTime = integer(visual, "force_time", 0, 0, 4000);
        this.translucent = bool(visual, "translucent", true);
        this.transparent = bool(visual, "transparent", false);
        this.toastCount = integer(visual, "toast_count", 3, 1, 7);
        this.offsetX = integer(visual, "x_offset", 0, -8192, 8192);
        this.offsetY = integer(visual, "y_offset", 0, -8192, 8192);
        this.noSlide = bool(visual, "no_slide", false);
        this.startLeft = bool(visual, "start_left", false);
        this.topDown = bool(visual, "top_down", false);

        JsonObject debug = section(root, "debug");
        this.printClasses = bool(debug, "print_classes", false);
    }

    private void rememberModifiedTime() {
        try {
            this.lastModified = Files.exists(FILE) ? Files.getLastModifiedTime(FILE).toMillis() : -1L;
        }
        catch (IOException ex) {
            this.lastModified = -1L;
        }
    }

    private static JsonObject section(JsonObject parent, String key) {
        JsonElement e = parent.get(key);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : new JsonObject();
    }

    private static boolean bool(JsonObject o, String key, boolean def) {
        JsonElement e = o.get(key);
        if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean()) {
            return e.getAsBoolean();
        }
        return def;
    }

    private static int integer(JsonObject o, String key, int def, int min, int max) {
        JsonElement e = o.get(key);
        if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
            return Math.max(min, Math.min(max, e.getAsInt()));
        }
        return def;
    }

    private static List<String> strings(JsonObject o, String key) {
        List<String> out = new ArrayList<>();
        JsonElement e = o.get(key);
        if (e != null && e.isJsonArray()) {
            for (JsonElement el : e.getAsJsonArray()) {
                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    out.add(el.getAsString());
                }
            }
        }
        return out;
    }

}
