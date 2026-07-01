package net.graungame.quickcmds.mixin;

import net.graungame.quickcmds.gui.QuickPanelHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class GameRendererMixin {

    @Shadow private double x;
    @Shadow private double y;

    private double qc_prevX = 0;
    private double qc_prevY = 0;
    private boolean qc_firstPos = true;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void quickcmds_onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action != 1) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!QuickPanelHud.panelOpen) return;
        if (mc.currentScreen != null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        double scale = mc.getWindow().getScaleFactor();
        int mx = (int)(this.x / scale);
        int my = (int)(this.y / scale);

        boolean consumed = false;
        if (button == 0) {
            consumed = QuickPanelHud.handleClick(mx, my, sw, sh);
        } else if (button == 1) {
            
            QuickPanelHud.panelOpen = false;
            consumed = true;
        }

        if (consumed) ci.cancel();
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void quickcmds_onCursorPos(long window, double newX, double newY, CallbackInfo ci) {
        if (!QuickPanelHud.panelOpen) {
            
            qc_prevX = newX;
            qc_prevY = newY;
            qc_firstPos = false;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) {
            qc_prevX = newX;
            qc_prevY = newY;
            return;
        }

        ci.cancel();

        if (qc_firstPos) {
            qc_prevX = newX;
            qc_prevY = newY;
            qc_firstPos = false;
            return;
        }

        double guiScale = mc.getWindow().getScaleFactor();
        double dx = (newX - qc_prevX) / guiScale;
        double dy = (newY - qc_prevY) / guiScale;

        qc_prevX = newX;
        qc_prevY = newY;

        QuickPanelHud.addMouseDelta(dx, dy);
    }
}

