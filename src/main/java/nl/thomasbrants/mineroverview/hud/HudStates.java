package nl.thomasbrants.mineroverview.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemGroup;

public class HudStates {
    private static final HudStates INSTANCE = new HudStates();
    public static HudStates getInstance() {
        return INSTANCE;
    }

    private KeyBinding toggleSlotKeyBinding;
    private boolean toggleSlotPressed;
    private ItemGroup createInventoryTab;

    public HudStates() {
        toggleSlotKeyBinding = null;
        toggleSlotPressed = false;

        createInventoryTab = null;
    }

    public void handleInput(MinecraftClient client, Screen currentScreen, long window,
                            int key, int action, int scancode) {
        if (toggleSlotKeyBinding == null) return;
        toggleSlotPressed = toggleSlotKeyBinding.wasPressed() || action != 0 && toggleSlotKeyBinding.matchesKey(key, scancode);
    }


    public void setToggleSlotKeyBinding(KeyBinding toggleSlotKeyBinding) {
        this.toggleSlotKeyBinding = toggleSlotKeyBinding;
    }

    public boolean isToggleSlotPressed() {
        return toggleSlotPressed;
    }


    public ItemGroup getCreateInventoryTab() {
        return createInventoryTab;
    }

    public void setCreateInventoryTab(ItemGroup createInventoryTab) {
        this.createInventoryTab = createInventoryTab;
    }
}
