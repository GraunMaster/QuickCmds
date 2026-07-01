package net.graungame.quickcmds;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;
import net.graungame.quickcmds.config.QuickCmdsConfig;
import net.graungame.quickcmds.gui.EditCommandScreen;
import net.graungame.quickcmds.gui.QuickPanelHud;
import net.graungame.quickcmds.keybind.KeybindManager;

public class QuickCmdsClient implements ClientModInitializer {

    private boolean wasPanelKeyDown = false;

    @Override
    public void onInitializeClient() {
        QuickCmdsConfig.load();
        KeybindManager.register();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.of("quickcmds", "quick_panel"),
                (ctx, tickCounter) -> QuickPanelHud.render(ctx, tickCounter.getTickProgress(false))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            
            if (client.player != null && KeybindManager.OPEN_SETTINGS.wasPressed()) {
                client.setScreen(new EditCommandScreen(null, -2));
                return;
            }

            if (client.player == null || client.currentScreen != null) {
                
                if (QuickPanelHud.panelOpen) {
                    QuickPanelHud.panelOpen = false;
                    wasPanelKeyDown = false;
                }
                return;
            }

            boolean panelKeyDown = KeybindManager.isPanelKeyDown(client);

            if (panelKeyDown && !wasPanelKeyDown) {
                
                QuickPanelHud.panelOpen = true;
                QuickPanelHud.resetJoystick();
            } else if (!panelKeyDown && wasPanelKeyDown && QuickPanelHud.panelOpen) {
                
                QuickPanelHud.executeSelectedAndClose(client);
            }

            wasPanelKeyDown = panelKeyDown;

            if (!QuickPanelHud.panelOpen) {
                KeybindManager.checkPerSlotKeys(client);
            }
        });
    }
}

