package nl.thomasbrants.mineroverview.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import nl.thomasbrants.mineroverview.config.ModConfig;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Overview hud.
 */
public class GameMinerHud {
    private final MinecraftClient client;
    private final ModConfig config;

    /**
     * Init overview hud for a given minecraft client.
     *
     * @param client The minecraft client.
     */
    public GameMinerHud(MinecraftClient client) {
        this.client = client;
        this.config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    /**
     * Draw the overview hud.
     *
     * @param matrixStack The matrix stack for rendering.
     */
    public void draw(MatrixStack matrixStack) {
        if (!config.toggleHud) return;

        RenderSystem.enableBlend();

        int guiX = 4;
        int guiY = 4;

        int lineHeight = client.textRenderer.fontHeight + 2;

        for (Map.Entry<String, String> entry : getInfoText().entrySet()) {
            client.textRenderer.drawWithShadow(matrixStack,
                entry.getValue(),
                guiX,
                guiY,
                config.textColor);

            if (entry.getKey().equals("light-level") && entry.getValue().contains(")") && client.player != null) {
                renderLightLevelItem(entry.getValue(), guiX, guiY);
            }

            guiY += lineHeight;
        }

        if (client.player != null) {
            client.getItemRenderer().renderInGuiWithOverrides(client.player.getInventory().getMainHandStack(), guiX, guiY);
        }

        RenderSystem.disableBlend();

        client.getProfiler().pop();
    }

    /**
     * Get all the info texts for the overview.
     *
     * @return The info texts.
     */
    private Map<String, String> getInfoText() {
        Map<String, String> infoText = new LinkedHashMap<>();

        infoText.put("coordinates", getCoordinatesText());
        infoText.put("dimension", getDimensionConversionText());
        infoText.put("fps", getFpsText());
        infoText.put("light-level", getLightLevelText());

        return infoText.entrySet()
            .stream()
            .filter(a -> a.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    /**
     * Get the fps info text.
     *
     * @return The text, null if not visible.
     */
    private String getFpsText() {
        if (!config.toggleFps) return null;

        int fps = MinecraftClient.getInstance().getCurrentFps();
        return "%s %s".formatted(fps, Text.translatable("text.miner_overview.fps").getString());
    }

    /**
     * Get the dimension conversion info text.
     *
     * @return The text, null if not visible.
     */
    private String getDimensionConversionText() {
        if (client.player == null) return null;
        if (!config.coordinates.toggleDimensionConversion) return null;

        String coordsFormat = "%s, %s, %s";

        // Show nether coordinates in the overworld
        if (client.player.getWorld().getRegistryKey().getValue().toString().equals("minecraft:overworld")) {
            return Text.translatable("text.miner_overview.nether").getString() + ": " + coordsFormat.formatted(client.player.getBlockX() / 8, client.player.getBlockY(), client.player.getBlockZ() / 8);
        }

        // Show overworld coordinates in the nether
        if (client.player.getWorld().getRegistryKey().getValue().toString().equals("minecraft:the_nether")) {
            return Text.translatable("text.miner_overview.overworld").getString() + ": " + coordsFormat.formatted(client.player.getBlockX() * 8, client.player.getBlockY(), client.player.getBlockZ() * 8);
        }

        // Show no coordinates in any other world
        return null;
    }

    /**
     * Get the coordinates info text.
     *
     * @return The text, null if not visible.
     */
    private String getCoordinatesText() {
        if (client.player == null) return null;
        if (!config.coordinates.toggleCoordinates) return null;

        String coordinatesText = "%s, %s, %s".formatted(client.player.getBlockX(),
            client.player.getBlockY(),
            client.player.getBlockZ());

        if (config.coordinates.toggleDirection) {
            coordinatesText += " (%s)".formatted(
                Text.translatable("text.miner_overview.direction." +
                    client.player.getHorizontalFacing().asString()).getString());
        }

        return coordinatesText;
    }

    /**
     * Get the light level info text.
     *
     * @return The text, null if not visible.
     */
    private String getLightLevelText() {
        if (client.world == null || client.player == null) return null;

        if (!config.lightLevel.toggleLightLevel) return null;

        // Get light level
        int maxLightLevel = client.world.getMaxLightLevel();
        int lightLevel = client.world.getLightLevel(LightType.BLOCK, client.player.getBlockPos());
        String lightLevelText = Text.translatable("text.miner_overview.lightLevel").getString() + ": %s".formatted(lightLevel);

        // Get next light source distance
        if (config.lightLevel.toggleLightLevelSpawnProof) {
            int nextLightSourceDistance = getNextLightSourceDistance(lightLevel, maxLightLevel);

            if (nextLightSourceDistance != 0) {
                lightLevelText += " (%s   )".formatted(nextLightSourceDistance);
            }
        }

        return lightLevelText;
    }

    private void renderLightLevelItem(String lightLevelText, int x, int y) {
        float scale = 0.7F;
        int xOffset = client.textRenderer.getWidth(lightLevelText) - client.textRenderer.getWidth("   )");

        MatrixStack matrixStack2 = RenderSystem.getModelViewStack();
        matrixStack2.push();
        matrixStack2.translate(x + xOffset, -3F + y, 0F);
        matrixStack2.scale(scale, scale, 1.0F);
        matrixStack2.translate(0.5F, 0F, 0F);
        RenderSystem.applyModelViewMatrix();

        client.getItemRenderer().renderInGuiWithOverrides(getLightItemStack(), 0, 0);

        matrixStack2.pop();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * Calculates the distance to the next light source to be placed.
     *
     * @param lightLevel The current light level.
     * @param maxLightLevel The maximum light level.
     * @return The distance to the next light source.
     */
    private int getNextLightSourceDistance(int lightLevel, int maxLightLevel) {
        // Calculate the distance to next light source placement
        if (client.world == null || client.player == null) return 0;

        // Find the closest light source
        List<Double> lightLevels = new ArrayList<>();

        // TODO: maybe cache / only update when world changed
        int actualLightLevel = lightLevel;
        if (actualLightLevel == 0) {
            // Find all light levels
            for (int i = -maxLightLevel*3; i < maxLightLevel*3; i++) {
                for (int j = -maxLightLevel*3; j < maxLightLevel*3; j++) {
                    for (int k = -maxLightLevel*3; k < maxLightLevel*3; k++) {
                        int x = client.player.getBlockX() + i;
                        int z = client.player.getBlockZ() + j;
                        int y = client.player.getBlockY() + k;

                        BlockPos pos = new BlockPos(x, y, z);

                        BlockState block = client.world.getBlockState(pos);
                        int blockLuminance = block.getLuminance();
                        if (blockLuminance == 0 || getLightItemStack() == null || !block.getBlock().equals(Block.getBlockFromItem(getLightItemStack().getItem()))) continue;

                        BlockPos posDistance = new BlockPos(x, client.player.getBlockY(), z);
                        int yOffset = Math.abs(client.player.getBlockY() - y);

                        double distance = Math.sqrt(posDistance.getSquaredDistance(client.player.getPos()));
                        lightLevels.add(blockLuminance - distance - yOffset);
                    }
                }
            }

            // Find lowest value.
            if (!lightLevels.isEmpty()) {
                lightLevels.sort(Double::compareTo);
                actualLightLevel = lightLevels.get(0).intValue();
            } else {
                return 0;
            }
        }

        int configLightLevel = config.lightLevel.minLightLevelSpawnProof + config.lightLevel.lightLevelHeight;
        int luminance = getLuminance();
        int worldLightLevel = actualLightLevel - 2;

        if (luminance <= 0) {
            return 0;
        }

        return worldLightLevel + luminance - configLightLevel;
    }

    /**
     * Get the luminance of the current item in the main or offhand.
     *
     * @return Light source luminance.
     */
    private int getLuminance() {
        if (client.player == null) return 0;

        if (client.player.getInventory().getMainHandStack() != null) {
            Item lightSource = client.player.getInventory().getMainHandStack().getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return luminance;
            }
        }

        if (client.player.getInventory().offHand.get(0).getItem() != null) {
            Item lightSource = client.player.getInventory().offHand.get(0).getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return luminance;
            }
        }

        return 0;
    }

    /**
     * Get the item stack with luminance, main hand first.
     *
     * @return ItemStack with luminance.
     */
    private ItemStack getLightItemStack() {
        if (client.player == null) return null;

        if (client.player.getInventory().getMainHandStack() != null) {
            Item lightSource = client.player.getInventory().getMainHandStack().getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return client.player.getInventory().getMainHandStack();
            }
        }

        if (client.player.getInventory().offHand.get(0).getItem() != null) {
            Item lightSource = client.player.getInventory().offHand.get(0).getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return client.player.getInventory().offHand.get(0);
            }
        }

        return null;
    }
}
