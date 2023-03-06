package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import nl.thomasbrants.mineroverview.hud.GameMinerHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	private GameMinerHud hudMiner;

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(MinecraftClient client, ItemRenderer render, CallbackInfo ci) {
		// Start Mixin
		MinerOverviewMod.LOGGER.info("Init Miner Hud");
		this.hudMiner = new GameMinerHud(client);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void onDraw(MatrixStack matrixStack, float esp, CallbackInfo ci) {
		if (!this.client.options.debugEnabled) {
			// Render hud
			this.hudMiner.draw(matrixStack);
		}
	}
}
