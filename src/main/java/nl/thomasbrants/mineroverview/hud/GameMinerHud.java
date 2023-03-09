package nl.thomasbrants.mineroverview.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import nl.thomasbrants.mineroverview.config.ModConfig;
import nl.thomasbrants.mineroverview.helpers.Colors;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Overview hud.
 */
public class GameMinerHud {
    private static final ConfigHolder<ModConfig> configHolder = AutoConfig.getConfigHolder(ModConfig.class);
    private static final ModConfig config = configHolder.getConfig();
    // TODO: fix code complexity
    private final MinecraftClient client;
    // TODO: reset on world switch
    // TODO: fix light distance out side of light level range
    // TODO: render green on block sides where to place
    private final Map<BlockPos, Integer> lightValueCache;
    private final List<BlockPos> lightValueCacheRevalidation;

    /**
     * Init overview hud for a given minecraft client.
     *
     * @param client The minecraft client.
     */
    public GameMinerHud(MinecraftClient client) {
        this.client = client;

        this.lightValueCache = new HashMap<>();
        this.lightValueCacheRevalidation = new ArrayList<>();
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

        renderItemStats(guiX, guiY);

        RenderSystem.disableBlend();
    }

    private void renderItemStats(int minX, int minY) {
        if (client.player == null || !config.itemOverview.toggleItemOverview) return;

        List<ItemStack> items = new ArrayList<>();
        if (config.itemOverview.toggleMainHandItemOverview) {
            items.add(client.player.getInventory().getMainHandStack());
        }
        if (config.itemOverview.toggleOffHandItemOverview) {
            items.add(client.player.getInventory().offHand.get(0));
        }

        if (config.itemOverview.toggleArmorItemOverview) {
            items.add(null);

            for (int i = client.player.getInventory().armor.size() - 1; i >= 0; --i ) {
                items.add(client.player.getInventory().armor.get(i));
            }
        }

        if (config.itemOverview.toggleInventoryItemOverview) {
            items.add(null);

            for (int slot : config.renderedSlots) {
                items.add(client.player.getInventory().getStack(slot));
            }
        }

        items = items.stream().filter(x -> x == null || !x.isEmpty()).toList();

        int height = client.getWindow().getScaledHeight();
        int lineHeight = client.textRenderer.fontHeight + 8;

        int maxSpace = height - minY - lineHeight * 2;
        int itemsSpace = maxSpace / lineHeight;

        int originalSize = items.size();
        items = items.stream().limit(itemsSpace).toList();

        int totalHeight = items.size() * lineHeight;
        int y = Math.max(height / 2 - totalHeight / 2, minY + lineHeight);

        for (ItemStack itemStack : items) {
            if (itemStack == null) {
                y += 8;
                continue;
            }

            renderItemStat(itemStack, minX, y);
            y += lineHeight;

            if (client.player.getInventory().armor.contains(itemStack)) y -= 2;
        }

        if (originalSize > items.size()) {
            renderGuiOverlay(client.textRenderer, minX, y + 8, "(%s %s)".formatted(originalSize - items.size(), Text.translatable("text.miner_overview.more").getString()), config.textColor);
        }
    }

    private void renderItemStat(ItemStack stack, int x, int y) {
        if (client.player == null) return;

        client.getItemRenderer().renderInGuiWithOverrides(stack, x, y);
        renderGuiItemCount(client.textRenderer, stack, x, y, config.textColor);

        if (stack.getItem().isDamageable() && config.itemOverview.toggleItemDamage) {
            int currentDurability = stack.getMaxDamage() - stack.getDamage();

            int color = config.textColor;
            if (currentDurability < stack.getMaxDamage()) {
                color = Colors.lightGreen;
            }
            if (currentDurability <= (stack.getMaxDamage() / 1.5)) {
                color = Colors.lightYellow;
            }
            if (currentDurability <= (stack.getMaxDamage() / 2.5)) {
                color = Colors.lightOrange;
            }
            if (currentDurability <= (stack.getMaxDamage()) / 4) {
                color = Colors.lightRed;
            }

            renderGuiOverlay(client.textRenderer, x + 19, y + 6, "%s".formatted(currentDurability), color);
            return;
        }

        int itemCount = client.player.getInventory().count(stack.getItem());
        if (itemCount > stack.getCount() && config.itemOverview.toggleTotalItemCount) {
            String itemCountString = "(%s)".formatted(itemCount);
            renderGuiOverlay(client.textRenderer, x + 19, y + 6 + 3, itemCountString, config.textColor);
        }
    }

    private void renderGuiItemCount(TextRenderer renderer, ItemStack stack, int x, int y, int color) {
        if (stack.isEmpty()) return;
        if (stack.getCount() == 1) return;

        String string = String.valueOf(stack.getCount());
        renderGuiOverlay(renderer, x + 19 - 2 - renderer.getWidth(string), y + 6 + 3, string, color);
    }

    private void renderGuiOverlay(TextRenderer renderer, int x, int y, String label, int color) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0F, 0.0F, client.getItemRenderer().zOffset + 200.0F);
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        renderer.draw(label, (float)(x), (float)(y), color, true, matrixStack.peek().getPositionMatrix(), immediate, false, 0, 15728880);
        immediate.draw();
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
        if (config.lightLevel.toggleLightLevelSpawnProof && getLightItemStack() != null) {
            Integer nextLightSourceDistance = getNextLightSourceDistance(lightLevel, maxLightLevel);

            if (nextLightSourceDistance != null) {
                lightLevelText += nextLightSourceDistance == 0 ? " (   )" : " (%s   )".formatted(nextLightSourceDistance);
            }
        }

        return lightLevelText;
    }

    /**
     * Renders the light level item.
     *
     * @param lightLevelText The light level text to render the item in.
     * @param x The light level text x position.
     * @param y The light level text y position.
     */
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
     * Handle block update in world for light levels.
     *
     * @param pos The position of the updated block.
     * @param oldBlock The old block state.
     * @param newBlock The new block state.
     */
    public void handleBlockUpdate(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
        if (oldBlock.getLuminance() <= 0 && newBlock.getLuminance() <= 0) return;

        if (client.world == null) return;

        int maxLightLevel = client.world.getMaxLightLevel();

        List<BlockPos> keys = lightValueCache.keySet().stream()
            .filter(x -> x.isWithinDistance(pos, maxLightLevel * 3)).toList();

        // Queue for update
        for (BlockPos key : keys) {
            if (lightValueCacheRevalidation.contains(key)) continue;
            lightValueCacheRevalidation.add(key);
        }
    }

    /**
     * Calculates the distance to the next light source to be placed.
     *
     * @param lightLevel The current light level.
     * @param maxLightLevel The maximum light level.
     * @return The distance to the next light source.
     */
    private Integer getNextLightSourceDistance(int lightLevel, int maxLightLevel) {
        // Calculate the distance to next light source placement
        if (client.world == null || client.player == null) return 0;

        int actualLightLevel = lightLevel;

        if (actualLightLevel == 0) {
            BlockPos playerPos = client.player.getBlockPos();

            if (lightValueCache.containsKey(playerPos) && !lightValueCacheRevalidation.contains(playerPos)) {
                Integer cachedLightLevel = lightValueCache.get(playerPos);
                if (cachedLightLevel == null) return null;

                actualLightLevel = cachedLightLevel;
            } else {
                lightValueCacheRevalidation.remove(playerPos);

                // Find the closest light source
                List<Double> lightLevels = calculateLightLevels(playerPos, maxLightLevel);

                // Find lowest value.
                if (!lightLevels.isEmpty()) {
                    lightLevels.sort(Double::compareTo);
                    actualLightLevel = lightLevels.get(lightLevels.size() - 1).intValue();
                    lightValueCache.put(playerPos, actualLightLevel);
                } else {
                    lightValueCache.put(playerPos, null);
                    return null;
                }
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
     * Calculates all the relative light levels for a player position.
     *
     * @param playerPos The player position.
     * @param maxLightLevel The max light level of a light source.
     * @return A list of the relative light levels.
     */
    private List<Double> calculateLightLevels(BlockPos playerPos, int maxLightLevel) {
        List<Double> lightLevels = new ArrayList<>();

        // Find all light levels
        for (int i = -maxLightLevel*3; i < maxLightLevel*3; i++) {
            for (int j = -maxLightLevel*3; j < maxLightLevel*3; j++) {
                for (int k = -maxLightLevel*3; k < maxLightLevel*3; k++) {
                    int x = playerPos.getX() + i;
                    int z = playerPos.getZ() + j;
                    int y = playerPos.getY() + k;

                    BlockPos pos = new BlockPos(x, y, z);

                    Double lightLevel = calculateLightLevel(playerPos, pos);
                    if (lightLevel == null) continue;

                    lightLevels.add(lightLevel);
                }
            }
        }

        return lightLevels;
    }

    /**
     * Calculates the relative light level for a block at a players position.
     *
     * @param playerPos The players position.
     * @param blockPos The block position.
     * @return The relative light level.
     */
    private Double calculateLightLevel(BlockPos playerPos, BlockPos blockPos) {
        if (client.world == null) return null;

        BlockState block = client.world.getBlockState(blockPos);
        int blockLuminance = block.getLuminance();
        if (blockLuminance == 0 || getLightItemStack() == null || !block.getBlock().asItem().equals(getLightItemStack().getItem())) return null;

        int yOffset = Math.abs(playerPos.getY() - blockPos.getY());

        BlockPos posDistance = new BlockPos(blockPos.getX(), playerPos.getY(), blockPos.getZ());
        double distance = Math.sqrt(posDistance.getSquaredDistance(playerPos));

        return blockLuminance - distance - yOffset;
    }

    /**
     * Get the luminance of the current item in the main or offhand.
     *
     * @return Light source luminance.
     */
    private int getLuminance() {
        if (client.player == null) return 0;

        if (!client.player.getInventory().getMainHandStack().isEmpty()) {
            Item lightSource = client.player.getInventory().getMainHandStack().getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return luminance;
            }
        }

        if (!client.player.getInventory().offHand.get(0).isEmpty()) {
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

        if (!client.player.getInventory().getMainHandStack().isEmpty()) {
            Item lightSource = client.player.getInventory().getMainHandStack().getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return client.player.getInventory().getMainHandStack();
            }
        }

        if (!client.player.getInventory().offHand.get(0).isEmpty()) {
            Item lightSource = client.player.getInventory().offHand.get(0).getItem();
            int luminance = Block.getBlockFromItem(lightSource).getStateManager().getDefaultState().getLuminance();

            if (luminance > 0) {
                return client.player.getInventory().offHand.get(0);
            }
        }

        return null;
    }

    public static void handleSlotMouseClick(Slot slot, int slotId, CallbackInfo ci) {
        if (!config.toggleHud || !config.itemOverview.toggleItemOverview || !config.itemOverview.toggleInventoryItemOverview) return;
        if (MinerOverviewMod.getOverviewHud() == null) return;
        if (!MinerOverviewMod.isToggleSlotPressed()) return;

        // Only allow for inventory slots
        if (slot.getIndex() < 9 || slot.getIndex() > 35) return;

        // Only allow if last slot with index
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        List<Slot> slots = player.currentScreenHandler.slots.stream()
            .filter(x -> x.getIndex() == slot.getIndex()).toList();
        Slot lastSlot = slots.get(slots.size() - 1);
        if (lastSlot.id != slotId) return;

        if (config.renderedSlots.contains(slot.getIndex())) {
            config.renderedSlots.remove(slot.getIndex());
        } else {
            config.renderedSlots.add(slot.getIndex());
        }

        configHolder.save();
        ci.cancel();
    }
}
