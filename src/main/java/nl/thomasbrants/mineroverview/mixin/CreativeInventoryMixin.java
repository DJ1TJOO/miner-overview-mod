/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import nl.thomasbrants.mineroverview.config.ModConfig;
import nl.thomasbrants.mineroverview.hud.HudStates;
import nl.thomasbrants.mineroverview.hud.OverviewHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryMixin {
    private final ConfigHolder<ModConfig> configHolder = AutoConfig.getConfigHolder(ModConfig.class);
    private final ModConfig config = configHolder.getConfig();

    @Shadow
    private static ItemGroup selectedTab;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta,
                             CallbackInfo ci) {
        HudStates.getInstance().setCreateInventoryTab(selectedTab);
    }

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    public void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType,
                             CallbackInfo ci) {
        OverviewHud.getInstance().handleSlotMouseClick(slot, ci);
    }
}
