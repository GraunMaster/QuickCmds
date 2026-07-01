package net.graungame.quickcmds.gui;

import net.graungame.quickcmds.config.QuickCommand;
import net.graungame.quickcmds.config.QuickCmdsConfig;
import net.graungame.quickcmds.keybind.KeybindManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class QuickPanelHud {

    public static boolean panelOpen = false;

    private static final float AA        = 2.0f;
    private static final float GAP       = 0.03f;
    private static final float HOVER_EXPAND = 5f;
    private static final int   COL_DOT   = 0xFFFFFFFF;

    private static final Identifier ICON_COMMAND = Identifier.of("quickcmds", "icon_command");
    private static final Identifier ICON_CHAT    = Identifier.of("quickcmds", "icon_chat");
    private static final Identifier ICON_HOTKEY  = Identifier.of("quickcmds", "icon_hotkey");

    private static int    hoveredSlot = -1;
    private static double joystickDX  = 0;
    private static double joystickDY  = 0;

    private static float[] hoverProgress = new float[0];
    private static long    lastRenderNanos = 0;

    public static void resetJoystick()                    { joystickDX = 0; joystickDY = 0; }
    public static void addMouseDelta(double dx, double dy){ if (!panelOpen) return; joystickDX += dx; joystickDY += dy; }

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !panelOpen) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        TextRenderer tr = mc.textRenderer;
        int cx = sw / 2, cy = sh / 2;

        float s      = QuickCmdsConfig.getMenuScale();
        float outerR = QuickCmdsConfig.getOuterRadius() * s;
        float innerR = QuickCmdsConfig.getInnerRadius() * s;
        float deadR  = QuickCmdsConfig.getDeadRadius()  * s;

        List<QuickCommand> cmds = QuickCmdsConfig.getCommands();
        int count = cmds.size();

        double joyLen = Math.sqrt(joystickDX * joystickDX + joystickDY * joystickDY);
        double clampedDX = joystickDX, clampedDY = joystickDY;
        if (joyLen > outerR) {
            double inv = outerR / joyLen;
            clampedDX = joystickDX * inv;
            clampedDY = joystickDY * inv;
            joystickDX = clampedDX;
            joystickDY = clampedDY;
        }

        hoveredSlot = -1;
        if (joyLen > deadR && count > 0) {
            double angle = Math.toDegrees(Math.atan2(clampedDY, clampedDX)) + 90;
            if (angle < 0) angle += 360;
            hoveredSlot = (int)(angle / (360.0 / count)) % count;
        }

        if (count == 0) {
            renderCursor(ctx, cx, cy, joyLen, clampedDX, clampedDY, outerR);
            return;
        }

        if (hoverProgress.length != count) hoverProgress = new float[count];
        long now = System.nanoTime();
        float dt = lastRenderNanos == 0 ? 0f : (now - lastRenderNanos) / 1_000_000_000f;
        lastRenderNanos = now;
        float step = dt > 0 ? Math.min(dt * 8f, 1f) : 0.25f;
        for (int i = 0; i < count; i++) {
            float target = (hoveredSlot == i) ? 1f : 0f;
            hoverProgress[i] += (target - hoverProgress[i]) * step;
        }

        float segAngle = (float)(2 * Math.PI / count);
        float[] segStart = new float[count];
        float[] segEnd   = new float[count];
        for (int i = 0; i < count; i++) {
            segStart[i] = (float)(-Math.PI / 2) + i * segAngle + GAP;
            segEnd[i]   = segStart[i] + segAngle - GAP * 2;
        }

        renderRing(ctx, cx, cy, outerR, innerR, count, segStart, segEnd, s);

        renderSlotIcons(ctx, tr, cx, cy, cmds, count, outerR, innerR, segStart, segEnd, segAngle, s);

        if (hoveredSlot >= 0 && hoveredSlot < count) {
            renderCenterLabel(ctx, tr, cx, cy, cmds.get(hoveredSlot), innerR);
        }

        renderCursor(ctx, cx, cy, joyLen, clampedDX, clampedDY, outerR);
    }

    private static void renderSlotIcons(DrawContext ctx, TextRenderer tr, int cx, int cy,
                                         List<QuickCommand> cmds, int count,
                                         float outerR, float innerR,
                                         float[] segStart, float[] segEnd, float segAngle,
                                         float scale) {
        
        float midR      = (innerR + outerR) / 2f;
        float chordLen  = 2f * midR * (float)Math.sin(segAngle / 2f);
        float ringDepth = outerR - innerR;
        int iconSize    = Math.max(8, Math.min(64, Math.round(Math.min(chordLen, ringDepth) * 0.72f * QuickCmdsConfig.getIconScale())));

        for (int i = 0; i < count; i++) {
            float expand = HOVER_EXPAND * scale * hoverProgress[i];
            float iMidR  = (innerR + outerR + expand) / 2f;
            float mid    = (segStart[i] + segEnd[i]) / 2f;

            int sx = (int)(cx + Math.cos(mid) * iMidR);
            int sy = (int)(cy + Math.sin(mid) * iMidR);

            QuickCommand cmd = cmds.get(i);
            String icon = cmd.icon != null ? cmd.icon : "";

            if (!icon.isBlank()) {
                renderIconAt(ctx, icon, sx - iconSize / 2, sy - iconSize / 2, iconSize);
            } else {
                
                Identifier fallbackIcon = cmd.isCommand() ? ICON_COMMAND
                        : cmd.isHotkey() ? ICON_HOTKEY : ICON_CHAT;
                try {
                    ctx.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                            fallbackIcon, sx - iconSize / 2, sy - iconSize / 2, iconSize, iconSize);
                } catch (Exception ignored) {}
            }
        }
    }

    private static void renderCenterLabel(DrawContext ctx, TextRenderer tr,
                                           int cx, int cy,
                                           QuickCommand cmd, float innerR) {
        String label = (cmd.label != null && !cmd.label.isBlank()) ? cmd.label : cmd.getFirstAction();
        if (label == null || label.isBlank()) return;

        int maxW = (int)(innerR * 1.6f);
        while (label.length() > 1 && tr.getWidth(label) > maxW)
            label = label.substring(0, label.length() - 1);

        int lw = tr.getWidth(label);
        int lx = cx - lw / 2;
        int ly = cy - 4;

        ctx.fill(lx - 3, ly - 2, lx + lw + 3, ly + 10, 0xAA000000);
        ctx.drawTextWithShadow(tr, Text.literal(label), lx, ly,
                QuickCmdsConfig.getTextColor(true));
    }

    private static void renderIconAt(DrawContext ctx, String icon, int x, int y, int size) {
        if (icon.startsWith("item:")) {
            try {
                Item item = Registries.ITEM.get(Identifier.of(icon.substring(5)));
                
                float scale = size / 16f;
                int cx = x + size / 2;
                int cy = y + size / 2;
                ctx.getMatrices().pushMatrix();
                ctx.getMatrices().translate(cx, cy);
                ctx.getMatrices().scale(scale, scale);
                ctx.drawItem(new ItemStack(item), -8, -8);
                ctx.getMatrices().popMatrix();
            } catch (Exception ignored) {}
        } else if (icon.startsWith("sprite:")) {
            try {
                ctx.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                        Identifier.of(icon.substring(7)), x, y, size, size);
            } catch (Exception ignored) {}
        }
    }

    private static void renderRing(DrawContext ctx, int cx, int cy,
                                    float outerR, float innerR,
                                    int count, float[] segStart, float[] segEnd,
                                    float scale) {
        float maxExpand   = HOVER_EXPAND * scale;
        float renderOuter = outerR + maxExpand + AA;
        int   orCeil      = (int)Math.ceil(renderOuter);

        normStart = new float[count];
        normEnd   = new float[count];
        float TWO_PI = (float)(2 * Math.PI);
        for (int i = 0; i < count; i++) {
            float s = segStart[i] % TWO_PI;
            float e = segEnd[i]   % TWO_PI;
            if (s < 0) s += TWO_PI;
            if (e < 0) e += TWO_PI;
            normStart[i] = s;
            normEnd[i]   = (e < s) ? e + TWO_PI : e;
        }

        float outerR2      = renderOuter * renderOuter;
        float innerRminAA2 = (innerR - AA) * (innerR - AA);

        for (int dy = -orCeil; dy <= orCeil; dy++) {
            float dyF  = dy + 0.5f;
            float dyF2 = dyF * dyF;
            if (dyF2 > outerR2) continue;

            int xHi = (int)Math.ceil(Math.sqrt(outerR2 - dyF2));
            int py  = cy + dy;

            int runSeg   = -1;
            int runStart = 0;

            for (int ax = -xHi; ax <= xHi + 1; ax++) {
                int curSeg = -1;
                if (ax <= xHi) {
                    float xc    = ax + 0.5f;
                    float dist2 = xc * xc + dyF2;
                    if (dist2 >= innerRminAA2) {
                        int seg = segmentOf(xc, dyF, count);
                        if (seg >= 0) {
                            float soAA = outerR + maxExpand * hoverProgress[seg] + AA;
                            if (dist2 <= soAA * soAA) curSeg = seg;
                        }
                    }
                }
                if (curSeg == runSeg) {
                    
                } else {
                    
                    if (runSeg >= 0) {
                        ctx.fill(cx + runStart, py, cx + ax, py + 1,
                                QuickCmdsConfig.getSegColor(hoveredSlot == runSeg));
                    }
                    runSeg   = curSeg;
                    runStart = ax;
                }
            }
        }
    }

    private static int segmentOf(float px, float py, int count) {
        double angle = Math.atan2(py, px);
        if (angle < 0) angle += 2 * Math.PI;
        float a = (float)angle;
        float TWO_PI = (float)(2 * Math.PI);
        for (int i = 0; i < count; i++) {
            float s = normStart[i], e = normEnd[i];
            if (e > TWO_PI) {
                
                if (a >= s || a <= e - TWO_PI) return i;
            } else {
                if (a >= s && a <= e) return i;
            }
        }
        return -1;
    }

    private static float[] normStart;
    private static float[] normEnd;

    private static void renderCursor(DrawContext ctx, int cx, int cy,
                                      double joyLen, double clampedDX, double clampedDY,
                                      float outerR) {
        int dotX, dotY;
        if (joyLen < 1) { dotX = cx; dotY = cy; }
        else {
            float clamp = (float)Math.min(joyLen, outerR - 4);
            dotX = (int)(cx + clampedDX / joyLen * clamp);
            dotY = (int)(cy + clampedDY / joyLen * clamp);
        }
        ctx.fill(dotX - 1, dotY - 3, dotX + 2, dotY + 4, COL_DOT);
        ctx.fill(dotX - 3, dotY - 1, dotX + 4, dotY + 2, COL_DOT);
        ctx.fill(dotX - 2, dotY - 2, dotX + 3, dotY + 3, COL_DOT);
    }

    public static boolean handleClick(int mx, int my, int sw, int sh) {
        if (!panelOpen) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        List<QuickCommand> cmds = QuickCmdsConfig.getCommands();
        if (hoveredSlot >= 0 && hoveredSlot < cmds.size())
            KeybindManager.executeCommand(mc, cmds.get(hoveredSlot));
        panelOpen = false;
        return true;
    }

    public static void executeSelectedAndClose(MinecraftClient client) {
        panelOpen = false;
        List<QuickCommand> cmds = QuickCmdsConfig.getCommands();
        if (hoveredSlot >= 0 && hoveredSlot < cmds.size())
            KeybindManager.executeCommand(client, cmds.get(hoveredSlot));
        hoveredSlot = -1;
    }
}

