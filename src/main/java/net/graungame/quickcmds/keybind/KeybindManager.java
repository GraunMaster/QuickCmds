package net.graungame.quickcmds.keybind;

import net.graungame.quickcmds.config.QuickCommand;
import net.graungame.quickcmds.config.QuickCmdsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeybindManager {

    public static KeyBinding OPEN_PANEL;
    public static KeyBinding OPEN_SETTINGS;

    private static final Map<String, Integer> KEY_MAP = new HashMap<>();
    private static final Map<String, int[]>   MOD_MAP = new HashMap<>();

    static {
        for (int i = 0; i < 26; i++)
            KEY_MAP.put(String.valueOf((char)('A' + i)), GLFW.GLFW_KEY_A + i);
        for (int i = 0; i <= 9; i++)
            KEY_MAP.put(String.valueOf(i), GLFW.GLFW_KEY_0 + i);
        for (int i = 1; i <= 12; i++)
            KEY_MAP.put("F" + i, GLFW.GLFW_KEY_F1 + (i - 1));
        for (int i = 0; i <= 9; i++)
            KEY_MAP.put("KP" + i, GLFW.GLFW_KEY_KP_0 + i);
        KEY_MAP.put("SPACE",     GLFW.GLFW_KEY_SPACE);
        KEY_MAP.put("ENTER",     GLFW.GLFW_KEY_ENTER);
        KEY_MAP.put("TAB",       GLFW.GLFW_KEY_TAB);
        KEY_MAP.put("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE);
        KEY_MAP.put("INSERT",    GLFW.GLFW_KEY_INSERT);
        KEY_MAP.put("DELETE",    GLFW.GLFW_KEY_DELETE);
        KEY_MAP.put("HOME",      GLFW.GLFW_KEY_HOME);
        KEY_MAP.put("END",       GLFW.GLFW_KEY_END);
        KEY_MAP.put("PAGEUP",    GLFW.GLFW_KEY_PAGE_UP);
        KEY_MAP.put("PAGEDOWN",  GLFW.GLFW_KEY_PAGE_DOWN);

        MOD_MAP.put("CTRL",  new int[]{ GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL });
        MOD_MAP.put("SHIFT", new int[]{ GLFW.GLFW_KEY_LEFT_SHIFT,   GLFW.GLFW_KEY_RIGHT_SHIFT   });
        MOD_MAP.put("ALT",   new int[]{ GLFW.GLFW_KEY_LEFT_ALT,     GLFW.GLFW_KEY_RIGHT_ALT     });
    }

    public static void register() {
        OPEN_PANEL = new KeyBinding("quickcmds.key.open_panel",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "QuickCmds");
        OPEN_SETTINGS = new KeyBinding("quickcmds.key.open_settings",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "QuickCmds");
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(OPEN_PANEL);
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(OPEN_SETTINGS);
    }

    public static boolean isPanelKeyDown(MinecraftClient client) {
        InputUtil.Key boundKey = InputUtil.fromTranslationKey(OPEN_PANEL.getBoundKeyTranslationKey());
        if (boundKey.getCategory() != InputUtil.Type.KEYSYM) return false;
        return GLFW.glfwGetKey(client.getWindow().getHandle(), boundKey.getCode()) == GLFW.GLFW_PRESS;
    }

    public static int resolveKey(String name) {
        if (name == null || name.isBlank()) return -1;
        Integer code = KEY_MAP.get(name.trim().toUpperCase());
        return code != null ? code : -1;
    }

    public static boolean isKeybindDown(long window, String keybind) {
        if (keybind == null || keybind.isBlank()) return false;
        String[] parts = keybind.toUpperCase().split("\\+");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) return false;
            int[] modCodes = MOD_MAP.get(part);
            if (modCodes != null) {
                boolean either = GLFW.glfwGetKey(window, modCodes[0]) == GLFW.GLFW_PRESS
                              || GLFW.glfwGetKey(window, modCodes[1]) == GLFW.GLFW_PRESS;
                if (!either) return false;
            } else {
                int code = resolveKey(part);
                if (code == -1) return false;
                if (GLFW.glfwGetKey(window, code) != GLFW.GLFW_PRESS) return false;
            }
        }
        return true;
    }

    public static String formatKeybind(String keybind) {
        if (keybind == null || keybind.isBlank()) return "—";
        return keybind.toUpperCase().replace("+", " + ");
    }

    private static final Set<String> heldKeys = new HashSet<>();

    public static void checkPerSlotKeys(MinecraftClient client) {
        if (client.player == null || client.currentScreen != null) return;
        long window = client.getWindow().getHandle();
        for (QuickCommand cmd : QuickCmdsConfig.getCommands()) {
            String kb = cmd.getKeybind();
            if (kb.isBlank()) continue;
            boolean down = isKeybindDown(window, kb);
            if (down && !heldKeys.contains(kb)) {
                heldKeys.add(kb);
                executeCommand(client, cmd);
            } else if (!down) {
                heldKeys.remove(kb);
            }
        }
    }

    public static void executeCommand(MinecraftClient client, QuickCommand cmd) {
        if (client.player == null) return;

        if (cmd.isHotkey()) {
            simulateKey(client, cmd.getKeybind());
            return;
        }

        List<String> actions = cmd.getActions();
        if (actions.isEmpty()) return;

        long delayMs = (long)(cmd.delay * 1000L);

        if (delayMs <= 0 || actions.size() == 1) {
            
            for (String action : actions) {
                sendAction(client, cmd, action);
            }
        } else {
            
            Thread thread = new Thread(() -> {
                for (int i = 0; i < actions.size(); i++) {
                    final String action = actions.get(i);
                    client.execute(() -> sendAction(client, cmd, action));
                    if (i < actions.size() - 1) {
                        try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
                    }
                }
            }, "quickcmds-exec");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static void sendAction(MinecraftClient client, QuickCommand cmd, String action) {
        if (client.player == null || action == null || action.isBlank()) return;
        if (cmd.isCommand()) {
            String exe = action.startsWith("/") ? action.substring(1) : action;
            client.player.networkHandler.sendChatCommand(exe);
        } else {
            client.player.networkHandler.sendChatMessage(action);
        }
    }

    public static void simulateKey(MinecraftClient client, String keybind) {
        if (keybind == null || keybind.isBlank()) return;
        String[] parts = keybind.toUpperCase().split("\\+");
        String mainPart = null;
        for (int i = parts.length - 1; i >= 0; i--) {
            String p = parts[i].trim();
            if (!MOD_MAP.containsKey(p)) { mainPart = p; break; }
        }
        if (mainPart == null) return;
        int glCode = resolveKey(mainPart);
        if (glCode == -1) return;
        int scancode = GLFW.glfwGetKeyScancode(glCode);
        InputUtil.Key inputKey = InputUtil.fromKeyCode(glCode, scancode);
        KeyBinding match = null;
        for (KeyBinding kb : client.options.allKeys) {
            if (kb.matchesKey(glCode, scancode)) { match = kb; break; }
        }
        if (match != null) {
            KeyBinding.setKeyPressed(inputKey, true);
            match.setPressed(true);
            KeyBinding.onKeyPressed(inputKey);
            final KeyBinding finalMatch = match;
            client.execute(() -> { KeyBinding.setKeyPressed(inputKey, false); finalMatch.setPressed(false); });
        } else {
            KeyBinding.setKeyPressed(inputKey, true);
            client.execute(() -> KeyBinding.setKeyPressed(inputKey, false));
        }
    }
}

