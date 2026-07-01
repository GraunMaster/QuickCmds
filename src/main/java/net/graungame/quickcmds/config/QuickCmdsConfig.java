package net.graungame.quickcmds.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuickCmdsConfig {

    public static final int MAX_SLOTS = 12;
    public static final float SCALE_MIN = 0.4f;
    public static final float SCALE_MAX = 2.0f;
    public static final int ALPHA_MIN = 20;
    public static final int ALPHA_MAX = 255;

    public static final int OUTER_R_MIN = 40;
    public static final int OUTER_R_MAX = 180;
    public static final int INNER_R_MIN = 10;
    public static final int INNER_R_MAX = 80;
    public static final int DEAD_R_MIN  = 5;
    public static final int DEAD_R_MAX  = 60;

    public static final float DELAY_MIN = 0f;
    public static final float DELAY_MAX = 10f;
    public static final float ICON_SCALE_MIN = 0.3f;
    public static final float ICON_SCALE_MAX = 2.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("quickcmds.json");

    private static List<QuickCommand> commands = new ArrayList<>();
    private static float menuScale   = 0.7f;
    private static float iconScale   = 1.0f;
    private static int outerRadius = 107;
    private static int innerRadius = 28;
    private static int deadRadius  = 28;

    public static int getOuterRadius() { return outerRadius; }
    public static int getInnerRadius() { return innerRadius; }
    public static int getDeadRadius()  { return deadRadius; }

    public static void setOuterRadius(int v) { outerRadius = clamp(v, OUTER_R_MIN, OUTER_R_MAX); }
    public static void setInnerRadius(int v) { innerRadius = clamp(v, INNER_R_MIN, Math.min(INNER_R_MAX, outerRadius - 10)); }
    public static void setDeadRadius(int v)  { deadRadius  = clamp(v, DEAD_R_MIN,  Math.min(DEAD_R_MAX,  outerRadius - 5)); }

    private static int segColorR = 0, segColorG = 0, segColorB = 0;
    private static int segAlpha    = 128;
    private static int segAlphaHov = 160;
    private static int txtColorR = 170, txtColorG = 170, txtColorB = 170;
    private static int txtHovR = 255, txtHovG = 255, txtHovB = 255;

    private static Map<String,Integer> iconUsage = new LinkedHashMap<>();

    public static void recordIconUsage(String iconId) {
        if (iconId == null || iconId.isBlank()) return;
        iconUsage.merge(iconId, 1, Integer::sum);
    }

    public static List<String> getMostUsedIcons(int n) {
        return iconUsage.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String,Integer>>comparingInt(e -> -e.getValue()))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    public static List<QuickCommand> getCommands() { return commands; }

    public static float getMenuScale()  { return menuScale; }
    public static void  setMenuScale(float v)  { menuScale  = Math.max(SCALE_MIN, Math.min(SCALE_MAX, v)); }
    public static float getIconScale()  { return iconScale; }
    public static void  setIconScale(float v)  { iconScale  = Math.max(ICON_SCALE_MIN, Math.min(ICON_SCALE_MAX, v)); }

    public static int getSegColorR() { return segColorR; }
    public static int getSegColorG() { return segColorG; }
    public static int getSegColorB() { return segColorB; }
    public static void setSegColor(int r, int g, int b) {
        segColorR = clamp(r,0,255); segColorG = clamp(g,0,255); segColorB = clamp(b,0,255);
    }
    public static int getSegColor(boolean hovered) {
        int a = hovered ? segAlphaHov : segAlpha;
        return (a << 24) | (segColorR << 16) | (segColorG << 8) | segColorB;
    }
    public static int getSegAlpha()    { return segAlpha; }
    public static int getSegAlphaHov() { return segAlphaHov; }
    public static void setSegAlpha(int v)    { segAlpha    = clamp(v, ALPHA_MIN, ALPHA_MAX); }
    public static void setSegAlphaHov(int v) { segAlphaHov = clamp(v, ALPHA_MIN, ALPHA_MAX); }

    public static int getTextColorR()  { return txtColorR; }
    public static int getTextColorG()  { return txtColorG; }
    public static int getTextColorB()  { return txtColorB; }
    public static int getTextHovR()    { return txtHovR; }
    public static int getTextHovG()    { return txtHovG; }
    public static int getTextHovB()    { return txtHovB; }
    public static void setTextColor(int r, int g, int b)    { txtColorR=clamp(r,0,255); txtColorG=clamp(g,0,255); txtColorB=clamp(b,0,255); }
    public static void setTextHovColor(int r, int g, int b) { txtHovR=clamp(r,0,255); txtHovG=clamp(g,0,255); txtHovB=clamp(b,0,255); }
    public static int getTextColor(boolean hovered) {
        if (hovered) return 0xFF000000 | (txtHovR<<16) | (txtHovG<<8) | txtHovB;
        return 0xFF000000 | (txtColorR<<16) | (txtColorG<<8) | txtColorB;
    }

    private static class SaveData {
        float menuScale   = 0.7f;
        float iconScale  = 1.0f;
        int   outerRadius = 107, innerRadius = 28, deadRadius = 28;
        int   segColorR=0, segColorG=0, segColorB=0;
        int   segAlpha=128, segAlphaHov=160;
        int   txtColorR=170, txtColorG=170, txtColorB=170;
        int   txtHovR=255, txtHovG=255, txtHovB=255;
        List<QuickCommand> commands = new ArrayList<>();
        Map<String,Integer> iconUsage = new LinkedHashMap<>();
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            if (!copyDefaultConfig(file)) { commands = new ArrayList<>(); return; }
        }
        try (Reader r = new FileReader(file)) {
            JsonElement el = JsonParser.parseReader(r);
            if (el.isJsonObject()) {
                SaveData data = GSON.fromJson(el, SaveData.class);
                commands     = data.commands != null ? data.commands : new ArrayList<>();
                menuScale    = data.menuScale;
                iconScale    = Math.max(ICON_SCALE_MIN, Math.min(ICON_SCALE_MAX, data.iconScale));
                outerRadius  = clamp(data.outerRadius, OUTER_R_MIN, OUTER_R_MAX);
                innerRadius  = clamp(data.innerRadius, INNER_R_MIN, INNER_R_MAX);
                deadRadius   = clamp(data.deadRadius,  DEAD_R_MIN,  DEAD_R_MAX);
                segColorR    = clamp(data.segColorR, 0, 255);
                segColorG    = clamp(data.segColorG, 0, 255);
                segColorB    = clamp(data.segColorB, 0, 255);
                segAlpha     = clamp(data.segAlpha,    ALPHA_MIN, ALPHA_MAX);
                segAlphaHov  = clamp(data.segAlphaHov, ALPHA_MIN, ALPHA_MAX);
                txtColorR=clamp(data.txtColorR,0,255); txtColorG=clamp(data.txtColorG,0,255); txtColorB=clamp(data.txtColorB,0,255);
                txtHovR=clamp(data.txtHovR,0,255);     txtHovG=clamp(data.txtHovG,0,255);     txtHovB=clamp(data.txtHovB,0,255);
                iconUsage = data.iconUsage != null ? data.iconUsage : new LinkedHashMap<>();
                
                for (QuickCommand cmd : commands) {
                    cmd.delay = Math.max(DELAY_MIN, Math.min(DELAY_MAX, cmd.delay));
                }
            } else if (el.isJsonArray()) {
                Type type = new TypeToken<List<QuickCommand>>(){}.getType();
                commands = GSON.fromJson(el, type);
                if (commands == null) commands = new ArrayList<>();
            }
            if (commands.size() > MAX_SLOTS)
                commands = commands.subList(0, MAX_SLOTS);
        } catch (Exception e) {
            commands = new ArrayList<>();
            System.err.println("[QuickCmds] Failed to load config: " + e.getMessage());
        }
    }

    private static boolean copyDefaultConfig(File target) {
        try (InputStream in = QuickCmdsConfig.class.getResourceAsStream("/assets/quickcmds/quickcmds.json")) {
            if (in == null) return false;
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (OutputStream out = new FileOutputStream(target)) {
                in.transferTo(out);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[QuickCmds] Failed to copy default config: " + e.getMessage());
            return false;
        }
    }

    public static void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            SaveData data = new SaveData();
            data.commands=commands; data.menuScale=menuScale; data.iconScale=iconScale;
            data.outerRadius=outerRadius; data.innerRadius=innerRadius; data.deadRadius=deadRadius;
            data.segColorR=segColorR; data.segColorG=segColorG; data.segColorB=segColorB;
            data.segAlpha=segAlpha; data.segAlphaHov=segAlphaHov;
            data.txtColorR=txtColorR; data.txtColorG=txtColorG; data.txtColorB=txtColorB;
            data.txtHovR=txtHovR; data.txtHovG=txtHovG; data.txtHovB=txtHovB;
            data.iconUsage=iconUsage;
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.err.println("[QuickCmds] Failed to save config: " + e.getMessage());
        }
    }

    public static boolean canAdd() { return commands.size() < MAX_SLOTS; }
    public static void add(QuickCommand cmd)            { if (canAdd()) commands.add(cmd); }
    public static void remove(int index)                { if (index >= 0 && index < commands.size()) commands.remove(index); }
    public static void set(int index, QuickCommand cmd) { if (index >= 0 && index < commands.size()) commands.set(index, cmd); }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}

