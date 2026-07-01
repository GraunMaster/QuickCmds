package net.graungame.quickcmds.config;

import java.util.ArrayList;
import java.util.List;

public class QuickCommand {
    public String label;
    public String action;
    public String type;
    public String keybind;

    public List<String> actions;
    
    public float delay = 0f;
    
    public String icon;

    public QuickCommand() {}

    public QuickCommand(String label, String action, String type) {
        this.label  = label;
        this.action = action;
        this.type   = type;
    }

    public boolean isCommand() { return "command".equals(type); }
    public boolean isHotkey()  { return "hotkey".equals(type); }

    public List<String> getActions() {
        List<String> result = new ArrayList<>();
        if (actions != null && !actions.isEmpty()) {
            result.addAll(actions);
        } else if (action != null && !action.isBlank()) {
            result.add(action);
        }
        return result;
    }

    public String getExecutable() {
        List<String> all = getActions();
        if (all.isEmpty()) return "";
        String a = all.get(0);
        if (isCommand() && a.startsWith("/")) return a.substring(1);
        return a;
    }

    public String getKeybind() {
        return keybind != null ? keybind : (isHotkey() ? action : "");
    }

    public String getFirstAction() {
        List<String> all = getActions();
        return all.isEmpty() ? "" : all.get(0);
    }

    public String getActionsText() {
        List<String> all = getActions();
        return String.join("\n", all);
    }
}

