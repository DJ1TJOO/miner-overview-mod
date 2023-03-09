package nl.thomasbrants.mineroverview.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import nl.thomasbrants.mineroverview.config.ModConfig;
import nl.thomasbrants.mineroverview.hud.GameMinerHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inventory selected mixin.
 */
@Mixin(value = {HandledScreen.class})
public abstract class InventoryMixin extends DrawableHelper {
    private static final Identifier OVERLAY_SLOT_TEXTURE = new Identifier("miner_overview:textures/gui/overlay_slot.png");

    private final ConfigHolder<ModConfig> configHolder = AutoConfig.getConfigHolder(ModConfig.class);
    private final ModConfig config = configHolder.getConfig();

    @Inject(method = "drawSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;itemRenderer:Lnet/minecraft/client/render/item/ItemRenderer;", ordinal = 0))
    private void drawSlot(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if (!shouldDrawSlot()) return;

        RenderSystem.setShaderTexture(0, OVERLAY_SLOT_TEXTURE);

        Slot lastSlot = GameMinerHud.getItemOverviewSlot(slot.getIndex());
        if (slot == lastSlot && config.renderedSlots.contains(slot.getIndex())) {
            DrawableHelper.drawTexture(matrices, slot.x, slot.y, getZOffset(), 0, 0, 16, 16, 16, 16);
        }
    }

    private boolean shouldDrawSlot() {
        if (!config.toggleHud || !config.itemOverview.toggleItemOverview || !config.itemOverview.toggleInventoryItemOverview || !config.itemOverview.toggleInventoryItemOverviewSlots)
            return false;
        if (MinerOverviewMod.getOverviewHud() == null) return false;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;
        if (player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) {
            return MinerOverviewMod.getCreateInventoryTab().getType()
                .equals(ItemGroup.Type.INVENTORY);
        }

        return true;
    }

    @Inject(method = "onMouseClick*", at = @At("HEAD"), cancellable = true)
    public void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType,
                             CallbackInfo ci) {
        GameMinerHud.handleSlotMouseClick(slot, slotId, ci);
    }
}
