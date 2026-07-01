package net.graungame.quickcmds.gui;

import net.graungame.quickcmds.config.QuickCmdsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IconPickerScreen extends Screen {

    private List<String> recommendedIcons = new ArrayList<>();

    private static final String[] PRESET_ITEMS = {
        "minecraft:grass_block", "minecraft:stone", "minecraft:dirt",
        "minecraft:diamond", "minecraft:diamond_sword", "minecraft:bow",
        "minecraft:arrow", "minecraft:golden_apple", "minecraft:apple",
        "minecraft:bread", "minecraft:torch", "minecraft:crafting_table",
        "minecraft:furnace", "minecraft:chest", "minecraft:bed",
        "minecraft:oak_log", "minecraft:oak_planks", "minecraft:glass",
        "minecraft:sand", "minecraft:gravel", "minecraft:tnt",
        "minecraft:redstone", "minecraft:lapis_lazuli", "minecraft:emerald",
        "minecraft:gold_ingot", "minecraft:iron_ingot", "minecraft:coal",
        "minecraft:blaze_rod", "minecraft:ender_pearl", "minecraft:nether_star",
        "minecraft:elytra", "minecraft:shield", "minecraft:totem_of_undying",
        "minecraft:trident", "minecraft:crossbow", "minecraft:fishing_rod",
        "minecraft:compass", "minecraft:map", "minecraft:clock",
        "minecraft:book", "minecraft:enchanted_book", "minecraft:name_tag",
        "minecraft:saddle", "minecraft:lead", "minecraft:bone",
        "minecraft:blaze_powder", "minecraft:gunpowder", "minecraft:feather",
        "minecraft:string", "minecraft:slime_ball", "minecraft:snowball",
        "minecraft:bucket", "minecraft:water_bucket", "minecraft:lava_bucket",
        "minecraft:oak_sapling", "minecraft:flower_pot", "minecraft:cake",
        "minecraft:pumpkin", "minecraft:jack_o_lantern", "minecraft:lantern",
        "minecraft:soul_lantern", "minecraft:campfire", "minecraft:soul_campfire",
        "minecraft:end_crystal", "minecraft:dragon_egg",
    };

    private static final int ICON_SIZE    = 16;
    private static final int ICON_PAD     = 4;
    private static final int ICONS_PER_ROW = 8;
    private static final int CELL = ICON_SIZE + ICON_PAD;

    private static final int C_BG      = 0xDD050510;
    private static final int C_SECTION = 0x99000000;
    private static final int C_HOV     = 0xCC1A1A30;
    private static final int C_SEL     = 0xCC2A2A50;
    private static final int C_TEXT    = 0xFFDDDDDD;
    private static final int C_DIM     = 0xFF888898;

    private final Screen parent;
    private final Consumer<String> callback;
    private final String currentIcon;

    private TextFieldWidget searchField;
    private String lastSearch = "";
    private List<String> filteredItems = new ArrayList<>();

    private int scrollOffset = 0;
    private int hoveredIcon  = -1;
    private String hoveredId = null;

    private int panelX, panelY, panelW, panelH;
    private int listY, listH;
    private int builtinRows;

    private boolean scrollDrag = false;
    private double  scrollAnchorY = 0;
    private int     scrollAnchorOff = 0;

    public IconPickerScreen(Screen parent, String currentIcon, Consumer<String> callback) {
        super(Text.literal("QuickCmds — Icon"));
        this.parent      = parent;
        this.currentIcon = currentIcon;
        this.callback    = callback;
    }

    @Override
    protected void init() {
        panelW = Math.min(width - 20, ICONS_PER_ROW * CELL + ICON_PAD * 2 + 20);
        panelH = Math.min(height - 40, 300);
        panelX = (width  - panelW) / 2;
        panelY = (height - panelH) / 2;

        int fieldH = 18;
        int sx = panelX + ICON_PAD;
        int sy = panelY + ICON_PAD;
        int sw = panelW - ICON_PAD * 2;
        searchField = new TextFieldWidget(textRenderer, sx, sy, sw, fieldH, Text.literal(""));
        searchField.setMaxLength(64);
        searchField.setPlaceholder(Text.translatable("quickcmds.icon.search"));
        addDrawableChild(searchField);

        listY = sy + fieldH + ICON_PAD + 14;
        listH = panelY + panelH - listY - 30;
        recommendedIcons = QuickCmdsConfig.getMostUsedIcons(ICONS_PER_ROW);
        builtinRows = recommendedIcons.isEmpty() ? 0 : (int)Math.ceil(recommendedIcons.size() / (double)ICONS_PER_ROW);

        rebuildFilter();
    }

    private void rebuildFilter() {
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        filteredItems.clear();
        
        if (q.isBlank()) {
            for (String id : PRESET_ITEMS) filteredItems.add("item:" + id);
        } else {
            
            for (Item item : Registries.ITEM) {
                String id = Registries.ITEM.getId(item).toString();
                String name = item.getName().getString().toLowerCase();
                if (id.contains(q) || name.contains(q)) {
                    filteredItems.add("item:" + id);
                }
            }
        }
        scrollOffset = 0;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        
        String curSearch = searchField != null ? searchField.getText() : "";
        if (!curSearch.equals(lastSearch)) {
            lastSearch = curSearch;
            rebuildFilter();
        }

        ctx.fill(0, 0, width, height, 0x88000000);

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, C_BG);

        super.render(ctx, mx, my, delta);

        hoveredIcon = -1;
        hoveredId   = null;

        int iconAreaW = ICONS_PER_ROW * CELL;
        int startX    = panelX + (panelW - iconAreaW) / 2;

        int builtinY = listY;
        if (!recommendedIcons.isEmpty()) {
            int sectionY = listY - 13;
            ctx.drawTextWithShadow(textRenderer,
                    Text.translatable("quickcmds.icon.recommended"),
                    panelX + ICON_PAD, sectionY, C_DIM);

            for (int i = 0; i < recommendedIcons.size(); i++) {
                int col = i % ICONS_PER_ROW;
                int row = i / ICONS_PER_ROW;
                int ix = startX + col * CELL;
                int iy = builtinY + row * CELL;
                String iconId = recommendedIcons.get(i);
                boolean sel = iconId.equals(currentIcon);
                boolean hov = mx >= ix && mx < ix + ICON_SIZE && my >= iy && my < iy + ICON_SIZE;
                if (hov) { hoveredIcon = i; hoveredId = iconId; }
                ctx.fill(ix - 1, iy - 1, ix + ICON_SIZE + 1, iy + ICON_SIZE + 1,
                        sel ? C_SEL : (hov ? C_HOV : 0x33FFFFFF));
                renderItemIcon(ctx, iconId, ix, iy);
            }
        }

        int itemSectionY = builtinY + builtinRows * CELL + ICON_PAD;
        ctx.drawTextWithShadow(textRenderer,
                Text.translatable("quickcmds.icon.items"),
                panelX + ICON_PAD, itemSectionY, C_DIM);

        int itemListY  = itemSectionY + 11;
        int visRows    = Math.max(1, (panelY + panelH - 30 - itemListY) / CELL);
        int totalRows  = (int)Math.ceil(filteredItems.size() / (double)ICONS_PER_ROW);
        scrollOffset   = Math.max(0, Math.min(scrollOffset, Math.max(0, totalRows - visRows)));

        int clipTop    = itemListY;
        int clipBottom = itemListY + visRows * CELL;

        for (int vi = 0; vi < visRows; vi++) {
            int rowIdx = vi + scrollOffset;
            for (int col = 0; col < ICONS_PER_ROW; col++) {
                int idx = rowIdx * ICONS_PER_ROW + col;
                if (idx >= filteredItems.size()) break;
                String iconId = filteredItems.get(idx);
                int ix = startX + col * CELL;
                int iy = itemListY + vi * CELL;
                boolean sel = iconId.equals(currentIcon);
                boolean hov = mx >= ix && mx < ix + ICON_SIZE && my >= iy && my < iy + ICON_SIZE
                           && my >= clipTop && my < clipBottom;
                if (hov) { hoveredIcon = recommendedIcons.size() + idx; hoveredId = iconId; }
                ctx.fill(ix - 1, iy - 1, ix + ICON_SIZE + 1, iy + ICON_SIZE + 1,
                        sel ? C_SEL : (hov ? C_HOV : 0x22FFFFFF));
                renderItemIcon(ctx, iconId, ix, iy);
            }
        }

        if (totalRows > visRows) {
            int bx  = panelX + panelW - 7;
            int bh  = visRows * CELL;
            int barH = Math.max(12, bh / Math.max(1, totalRows));
            float pct = (float)scrollOffset / Math.max(1, totalRows - visRows);
            int barY = itemListY + (int)((bh - barH) * pct);
            ctx.fill(bx, itemListY, bx + 4, itemListY + bh, 0x33FFFFFF);
            ctx.fill(bx, barY, bx + 4, barY + barH, 0xAAFFFFFF);
        }

        if (hoveredId != null) {
            String tip = iconLabel(hoveredId);
            ctx.drawTooltip(textRenderer, Text.literal(tip), mx, my);
        }

        int btnY  = panelY + panelH - 26;
        int btnW  = 80;
        int btnX  = panelX + panelW / 2 - btnW / 2;
        boolean btnHov = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + 16;
        ctx.fill(btnX, btnY, btnX + btnW, btnY + 16, btnHov ? 0xCC1A1A28 : 0xCC111118);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("quickcmds.icon.none"), btnX + btnW / 2, btnY + 4, C_TEXT);
    }

    private void renderBuiltinIcon(DrawContext ctx, String iconId, int x, int y) {
        if (!iconId.startsWith("sprite:")) return;
        String path = iconId.substring("sprite:".length());
        try {
            Identifier id = Identifier.of(path);
            ctx.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                    id, x, y, ICON_SIZE, ICON_SIZE);
        } catch (Exception ignored) {}
    }

    private void renderItemIcon(DrawContext ctx, String iconId, int x, int y) {
        if (!iconId.startsWith("item:")) return;
        String itemPath = iconId.substring("item:".length());
        try {
            Item item = Registries.ITEM.get(Identifier.of(itemPath));
            ctx.drawItem(new ItemStack(item), x, y);
        } catch (Exception ignored) {}
    }

    private String iconLabel(String iconId) {
        if (iconId.startsWith("item:")) {
            String itemId = iconId.substring("item:".length());
            try {
                Item item = Registries.ITEM.get(Identifier.of(itemId));
                return item.getName().getString();
            } catch (Exception ignored) {}
        }
        return iconId;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int x = (int)mx, y = (int)my;

        int bx = panelX + panelW - 7;
        if (x >= bx && x < bx + 4 && btn == 0) {
            scrollDrag = true; scrollAnchorY = my; scrollAnchorOff = scrollOffset;
            return true;
        }

        if (hoveredId != null && btn == 0) {
            QuickCmdsConfig.recordIconUsage(hoveredId);
            callback.accept(hoveredId);
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        int btnY = panelY + panelH - 26;
        int btnW = 80;
        int btnX = panelX + panelW / 2 - btnW / 2;
        if (x >= btnX && x < btnX + btnW && y >= btnY && y < btnY + 16 && btn == 0) {
            callback.accept("");
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        if (x < panelX || x > panelX + panelW || y < panelY || y > panelY + panelH) {
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int totalRows = (int)Math.ceil(filteredItems.size() / (double)ICONS_PER_ROW);
        int itemSectionY = listY + builtinRows * CELL + ICON_PAD + 11;
        int visRows = Math.max(1, (panelY + panelH - 30 - itemSectionY) / CELL);
        scrollOffset = Math.max(0, Math.min(scrollOffset + (dy > 0 ? -1 : 1), Math.max(0, totalRows - visRows)));
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (scrollDrag) {
            int totalRows = (int)Math.ceil(filteredItems.size() / (double)ICONS_PER_ROW);
            int itemSectionY = listY + builtinRows * CELL + ICON_PAD + 11;
            int visRows = Math.max(1, (panelY + panelH - 30 - itemSectionY) / CELL);
            int bh = visRows * CELL;
            double delta = my - scrollAnchorY;
            int steps = (int)(delta / bh * Math.max(1, totalRows - visRows));
            scrollOffset = Math.max(0, Math.min(scrollAnchorOff + steps, Math.max(0, totalRows - visRows)));
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        scrollDrag = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int kc, int sc, int mod) {
        if (kc == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(kc, sc, mod);
    }

    @Override
    public boolean shouldPause() { return false; }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,     y,     x+w,   y+1,   color);
        ctx.fill(x,     y+h-1, x+w,   y+h,   color);
        ctx.fill(x,     y,     x+1,   y+h,   color);
        ctx.fill(x+w-1, y,     x+w,   y+h,   color);
    }
}

