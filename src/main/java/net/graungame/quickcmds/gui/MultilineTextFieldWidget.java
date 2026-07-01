package net.graungame.quickcmds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MultilineTextFieldWidget extends ClickableWidget {

    private final TextRenderer textRenderer;
    private final List<String> lines = new ArrayList<>();
    private int cursorRow = 0;
    private int cursorCol = 0;
    private int maxLength = 4096;
    private Text placeholder = Text.literal("");
    private int lineHeight = 10;
    private boolean focusedField = false;
    private boolean singleLine = false;

    private static final int MAX_VISIBLE_LINES = 6;
    private int scrollOffset = 0;
    private boolean draggingScrollbar = false;
    private int scrollbarDragOffsetY = 0;
    private static final int SCROLLBAR_W = 4;

    public void setSingleLine(boolean singleLine) { this.singleLine = singleLine; }

    public MultilineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text title) {
        super(x, y, width, height, title);
        this.textRenderer = textRenderer;
        lines.add("");
    }

    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public void setPlaceholder(Text placeholder) { this.placeholder = placeholder; }
    public void setLineHeight(int h) { this.lineHeight = h; }

    public String getText() {
        return String.join("\n", lines);
    }

    public void setText(String text) {
        lines.clear();
        if (text == null || text.isEmpty()) {
            lines.add("");
        } else {
            for (String l : text.split("\n", -1)) lines.add(l);
        }
        cursorRow = lines.size() - 1;
        cursorCol = lines.get(cursorRow).length();
        scrollOffset = 0;
        ensureCursorVisible();
    }

    private int totalLength() {
        int n = 0;
        for (String l : lines) n += l.length();
        return n + (lines.size() - 1);
    }

    private int visibleLines() {
        return Math.max(1, Math.min(MAX_VISIBLE_LINES, getHeight() / lineHeight));
    }

    private int maxScroll() {
        return Math.max(0, lines.size() - visibleLines());
    }

    private void clampScroll() {
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll()));
    }

    private void ensureCursorVisible() {
        int vis = visibleLines();
        if (cursorRow < scrollOffset) scrollOffset = cursorRow;
        else if (cursorRow >= scrollOffset + vis) scrollOffset = cursorRow - vis + 1;
        clampScroll();
    }

    private boolean hasScrollbar() {
        return lines.size() > visibleLines();
    }

    public void setFocusedField(boolean f) { this.focusedField = f; this.setFocused(f); }

    @Override
    public boolean isFocused() { return focusedField; }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.focusedField = focused;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        clampScroll();
        boolean empty = lines.size() == 1 && lines.get(0).isEmpty();
        boolean scrollbar = hasScrollbar();
        int textX = getX() + 3;
        int textY = getY() + 2;

        ctx.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());

        if (empty && !focusedField) {
            ctx.drawTextWithShadow(textRenderer, placeholder, textX, textY, 0xFF555566);
            ctx.disableScissor();
            return;
        }

        int vis = visibleLines();
        for (int row = scrollOffset; row < Math.min(lines.size(), scrollOffset + vis); row++) {
            int i = row - scrollOffset;
            int ly = textY + i * lineHeight;
            String line = lines.get(row);
            ctx.drawTextWithShadow(textRenderer, Text.literal(line), textX, ly, 0xFFE0E0E0);

            if (focusedField && row == cursorRow) {
                int caretX = textX + textRenderer.getWidth(line.substring(0, Math.min(cursorCol, line.length())));
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    ctx.fill(caretX, ly - 1, caretX + 1, ly + lineHeight - 2, 0xFFE0E0E0);
                }
            }
        }

        if (scrollbar) {
            int trackX = getX() + getWidth() - SCROLLBAR_W - 1;
            int trackY = getY() + 1;
            int trackH = getHeight() - 2;
            ctx.fill(trackX, trackY, trackX + SCROLLBAR_W, trackY + trackH, 0x33FFFFFF);

            int thumbH = Math.max(8, trackH * vis / lines.size());
            int scrollRange = Math.max(1, maxScroll());
            int thumbY = trackY + (trackH - thumbH) * scrollOffset / scrollRange;
            boolean thumbHover = mouseX >= trackX && mouseX <= trackX + SCROLLBAR_W && mouseY >= thumbY && mouseY <= thumbY + thumbH;
            ctx.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, (thumbHover || draggingScrollbar) ? 0xFFBBBBBB : 0xFF888888);
        }

        ctx.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean hit = isMouseOver(mouseX, mouseY);
        if (!hit) { setFocusedField(false); return false; }
        setFocusedField(true);

        if (hasScrollbar()) {
            int trackX = getX() + getWidth() - SCROLLBAR_W - 1;
            if (mouseX >= trackX) {
                int vis = visibleLines();
                int trackY = getY() + 1;
                int trackH = getHeight() - 2;
                int thumbH = Math.max(8, trackH * vis / lines.size());
                int scrollRange = Math.max(1, maxScroll());
                int thumbY = trackY + (trackH - thumbH) * scrollOffset / scrollRange;
                if (mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                    draggingScrollbar = true;
                    scrollbarDragOffsetY = (int) (mouseY - thumbY);
                } else {
                    
                    double ratio = (mouseY - trackY - thumbH / 2.0) / Math.max(1, trackH - thumbH);
                    scrollOffset = (int) Math.round(ratio * scrollRange);
                    clampScroll();
                    draggingScrollbar = true;
                    scrollbarDragOffsetY = thumbH / 2;
                }
                return true;
            }
        }

        int vis = visibleLines();
        int relY = (int) (mouseY - getY() - 2);
        int row = Math.max(0, Math.min(vis - 1, relY / lineHeight)) + scrollOffset;
        row = Math.max(0, Math.min(lines.size() - 1, row));
        cursorRow = row;
        String line = lines.get(row);
        int relX = (int) (mouseX - getX() - 3);
        cursorCol = textRenderer.trimToWidth(line, Math.max(0, relX)).length();
        ensureCursorVisible();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int vis = visibleLines();
            int trackY = getY() + 1;
            int trackH = getHeight() - 2;
            int thumbH = Math.max(8, trackH * vis / lines.size());
            int scrollRange = Math.max(1, maxScroll());
            double ratio = (mouseY - scrollbarDragOffsetY - trackY) / Math.max(1, trackH - thumbH);
            scrollOffset = (int) Math.round(ratio * scrollRange);
            clampScroll();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar) { draggingScrollbar = false; return true; }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (!hasScrollbar()) return false;
        scrollOffset -= (int) Math.signum(verticalAmount);
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focusedField) return false;
        boolean handled = handleKey(keyCode);
        if (handled) ensureCursorVisible();
        return handled;
    }

    private boolean handleKey(int keyCode) {
        String line = lines.get(cursorRow);

        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (singleLine) return true;
                if (totalLength() >= maxLength) return true;
                String before = line.substring(0, cursorCol);
                String after = line.substring(cursorCol);
                lines.set(cursorRow, before);
                lines.add(cursorRow + 1, after);
                cursorRow++;
                cursorCol = 0;
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursorCol > 0) {
                    lines.set(cursorRow, line.substring(0, cursorCol - 1) + line.substring(cursorCol));
                    cursorCol--;
                } else if (cursorRow > 0) {
                    String prev = lines.get(cursorRow - 1);
                    int newCol = prev.length();
                    lines.set(cursorRow - 1, prev + line);
                    lines.remove(cursorRow);
                    cursorRow--;
                    cursorCol = newCol;
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (cursorCol < line.length()) {
                    lines.set(cursorRow, line.substring(0, cursorCol) + line.substring(cursorCol + 1));
                } else if (cursorRow < lines.size() - 1) {
                    String next = lines.get(cursorRow + 1);
                    lines.set(cursorRow, line + next);
                    lines.remove(cursorRow + 1);
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (cursorCol > 0) cursorCol--;
                else if (cursorRow > 0) { cursorRow--; cursorCol = lines.get(cursorRow).length(); }
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (cursorCol < line.length()) cursorCol++;
                else if (cursorRow < lines.size() - 1) { cursorRow++; cursorCol = 0; }
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if (cursorRow > 0) { cursorRow--; cursorCol = Math.min(cursorCol, lines.get(cursorRow).length()); }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if (cursorRow < lines.size() - 1) { cursorRow++; cursorCol = Math.min(cursorCol, lines.get(cursorRow).length()); }
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> { cursorCol = 0; return true; }
            case GLFW.GLFW_KEY_END -> { cursorCol = line.length(); return true; }
        }
        return false;
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!focusedField) return false;
        if (chr == '\n' || chr == '\r') return true;
        if (totalLength() >= maxLength) return true;
        String line = lines.get(cursorRow);
        lines.set(cursorRow, line.substring(0, cursorCol) + chr + line.substring(cursorCol));
        cursorCol++;
        ensureCursorVisible();
        return true;
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, getText().isEmpty() ? placeholder : Text.literal(getText()));
    }
}

