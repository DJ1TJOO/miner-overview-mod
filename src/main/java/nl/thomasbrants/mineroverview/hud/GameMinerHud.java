package nl.thomasbrants.mineroverview.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class GameMinerHud {
    private final MinecraftClient client;

    public GameMinerHud(MinecraftClient client) {
        this.client = client;
    }

    public void draw(MatrixStack matrixStack) {
        RenderSystem.enableBlend();

        client.textRenderer.drawWithShadow(matrixStack, "FF", 0,0, 0x00E0E0E0);

        client.getProfiler().pop();
    }
}
