/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.light;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import nl.thomasbrants.mineroverview.config.ModConfig;
import nl.thomasbrants.mineroverview.hud.OverviewHud;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: make data struct more efficient.
// TODO: make less calls or make more efficient

public class LightLevelManger {
    private static final LightLevelManger INSTANCE = new LightLevelManger();

    public static LightLevelManger getInstance() {
        return INSTANCE;
    }

    private final ModConfig config;
    private final ArrayDeque<QueueEntry> queue;

    public LightLevelManger() {
        ConfigHolder<ModConfig> holder = AutoConfig.getConfigHolder(ModConfig.class);
        this.config = holder.getConfig();
        this.queue = new ArrayDeque<>();

        holder.registerSaveListener((configHolder, config) -> {
            updateHighlights();
            return ActionResult.PASS;
        });
    }

    public void updateHighlights() {
        if (MinecraftClient.getInstance().player == null) return;

        LightHighlightRenderer.getInstance().clearHighlightedBlocks();

        int luminance = OverviewHud.getInstance().getPlayerItemLuminance();
        int blockY = MinecraftClient.getInstance().player.getBlockY() + config.lightLevel.lightLevelHeight;

        if (luminance <= 0) return;

        for (Map.Entry<Long, LightLevelStorageEntry> lightLevel : LightLevelStorage.LIGHT_LEVELS.entrySet()) {
            if (BlockPos.unpackLongY(lightLevel.getKey()) != blockY) continue;
            if (getNextLightSourceDistance(luminance, lightLevel.getValue().value - Math.abs(config.lightLevel.lightLevelHeight)) != 0) continue;

            LightHighlightRenderer.getInstance().addHighlightedBlock(lightLevel.getKey());
        }
    }

    private int getLocalLightLevel(long pos) {
        return LightLevelStorage.LIGHT_LEVELS.containsKey(pos) ? LightLevelStorage.LIGHT_LEVELS.get(pos).value : Integer.MIN_VALUE; //getLightingProvider().get(LightType.BLOCK).getLightLevel(pos)
    }

    private void setLocalLightLevel(long pos, int lightLevel, long sourcePos) {
        LightLevelStorage.LIGHT_LEVELS.put(pos, new LightLevelStorageEntry(lightLevel, sourcePos));
        LightHighlightRenderer.getInstance().removeHighlightedBlock(pos);

        if (MinecraftClient.getInstance().player == null) return;
        if (BlockPos.unpackLongY(pos) != MinecraftClient.getInstance().player.getBlockY() + config.lightLevel.lightLevelHeight) return;
        if (OverviewHud.getInstance().getPlayerItemLuminance() <= 0 || getNextLightSourceDistance(OverviewHud.getInstance().getPlayerItemLuminance(), lightLevel - Math.abs(config.lightLevel.lightLevelHeight)) != 0) return;

        LightHighlightRenderer.getInstance().addHighlightedBlock(pos);
    }

    public void updateBlockLight(BlockView world, long pos, int value) {
        if (value < 0 || value > world.getMaxLightLevel()) {
            throw new IllegalArgumentException();
        }

        // Only update when using light level spawn proof
        // TODO: This should be removed, as it doesn't detect new light sources, but is currently necessary to prevent crashes
        if (!config.lightLevel.toggleLightLevelSpawnProof || OverviewHud.getInstance().getPlayerItemLuminance() == 0) return;

        MinerOverviewMod.LOGGER.info("Updating light sources");
        long start = System.currentTimeMillis();

        if (value == LightLevelStorage.LIGHT_LEVELS.getOrDefault(pos, new LightLevelStorageEntry(-1, 0)).value) {
            return;
        }

        // Reset all with sources in range
        resetForSources(world, pos);

        // Requeue changed if it gives light
        if (value > 0) {
            queue.add(new QueueEntry(pos, new LightLevelStorageEntry(value, pos)));
            setLocalLightLevel(pos, value, pos);
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        MinerOverviewMod.LOGGER.info("Updated light sources. It took: " + time + "ms");

        propagateIncrease(world);
    }

    private void resetForSources(BlockView world, long pos) {
        List<Map.Entry<Long, LightLevelStorageEntry>> lightLevelsSorted = LightLevelStorage.LIGHT_LEVELS.entrySet()
            .stream().sorted((a, b) -> b.getValue().value - a.getValue().value).toList();
        Map<Long, LightLevelStorageEntry> sources = new LinkedHashMap<>();
        for (Map.Entry<Long, LightLevelStorageEntry> lightLevel : lightLevelsSorted) {
            long lightLevelPos = lightLevel.getKey();
            long sourcePos = lightLevel.getValue().sourcePos;

            if (lightLevelPos == sourcePos && BlockPos.fromLong(lightLevelPos).isWithinDistance(BlockPos.fromLong(
                pos), world.getMaxLightLevel()*2)) {
                sources.put(lightLevelPos, lightLevel.getValue());
                if (lightLevelPos != pos) {
                    // Requeue all sources except for changed
                    queue.add(new QueueEntry(lightLevelPos, lightLevel.getValue()));
                }
            } else if (sources.containsKey(sourcePos) && lightLevelPos != sourcePos) {
                LightLevelStorage.LIGHT_LEVELS.remove(lightLevelPos);
                LightHighlightRenderer.getInstance().removeHighlightedBlock(lightLevelPos);
            }
        }

        // Remove changed light level
        LightLevelStorage.LIGHT_LEVELS.remove(pos);
        LightHighlightRenderer.getInstance().removeHighlightedBlock(pos);
    }

    private void propagateIncrease(BlockView world) {
        MinerOverviewMod.LOGGER.info("Recalculating negative light values");
        long start = System.currentTimeMillis();

        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            long pos = entry.pos;
            long sourcePos = entry.value.sourcePos;
            int lightValue = entry.value.value;

            for (Direction direction : Direction.stream().toList()) {
                BlockPos neighbourBlockPos = BlockPos.fromLong(pos).offset(direction);
                long neighbourPos = neighbourBlockPos.asLong();

                int currentLevel = getLocalLightLevel(neighbourPos);
                if (currentLevel >= (lightValue - 1)) {
                    continue;
                }

                int neighbourStateOpacity = world.getBlockState(neighbourBlockPos).getOpacity(world, neighbourBlockPos);
                if (neighbourStateOpacity == world.getMaxLightLevel()) {
                    setLocalLightLevel(neighbourPos, 0, sourcePos);
                    continue;
                }

                int targetLevel = lightValue - Math.max(1, neighbourStateOpacity);
                if (targetLevel < -world.getMaxLightLevel()) continue;
                if (targetLevel > currentLevel) {
                    setLocalLightLevel(neighbourPos, targetLevel, sourcePos);
                    queue.add(new QueueEntry(neighbourPos, new LightLevelStorageEntry(targetLevel, sourcePos)));
                }
            }
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        MinerOverviewMod.LOGGER.info("Recalculated negative light values. It took: " + time + "ms");
    }

    /**
     * Calculates the distance to the next light source to be placed.
     *
     * @param sourceLightLevel The light level of the source.
     * @param currentLightLevel The current light level.
     * @return The distance to the next light source.
     */
    public int getNextLightSourceDistance(int sourceLightLevel, int currentLightLevel) {
        // Calculate the distance to next light source placement

        // Emitter ground | actualLightLevel | minLightLevel
        // 13 1 6
        // 13 + 1 = 13 -> 14 / 2 = 7 -> round(7) = 7 -> 7 - 6 = 1 -> 1 * 2 = 2 -> 2 + 1 = 3
        // 13 0 6
        // 13 + 0 = 13 -> 13 / 2 = 6.5 -> round(6.5) = 7 -> 7 - 6 = 1 -> 1 * 2 = 2
        // 13 -1 6
        // 13 - 1 = 12 -> 12 / 2 = 6 -> round(6) = 6 -> 6 - 6 = 0 -> 0 * 2 = 0 -> 0 + 1 = 1
        // 13 -2 6
        // 13 - 2 = 11 -> 11 / 2 = 5.5 -> round(5.5) = 6 -> 6 - 6 = 0 -> 0 * 2 = 0
        // 13 -3 6
        // 13 - 3 = 10 -> 10 / 2 = 5 -> round(5) = 5 -> 5 - 6 = -1 -> -1 * 2 = -2 -> -2 + 1 = -1

        int emitterGroundLightLevel = sourceLightLevel - Math.abs(config.lightLevel.lightLevelHeight);

        float minLightLevelWhenPlaced = (emitterGroundLightLevel + currentLightLevel) / 2f;
        boolean isRounded = minLightLevelWhenPlaced % 1 == 0;

        int currentDistance = Math.round(minLightLevelWhenPlaced) - config.lightLevel.minLightLevelSpawnProof;

        return currentDistance * 2 + (isRounded ? 1 : 0);
    }
}
