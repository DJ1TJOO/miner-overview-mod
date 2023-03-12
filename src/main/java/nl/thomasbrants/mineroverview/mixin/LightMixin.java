package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import nl.thomasbrants.mineroverview.light.LightLevelStorage;
import nl.thomasbrants.mineroverview.light.LightLevelStorageEntry;
import nl.thomasbrants.mineroverview.light.QueueEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(World.class)
public abstract class LightMixin implements WorldAccess, BlockView {
    @Shadow @Final private RegistryKey<DimensionType> dimension;
    // TODO: find new unknown sources on player move.
    // TODO: make data struct more efficient.
    private final ArrayDeque<QueueEntry> queue = new ArrayDeque<>();

    @Inject(method = "onBlockChanged", at = @At("RETURN"))
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock,
                               CallbackInfo ci) {
        if (!this.dimension.getValue().getPath().contains("overworld")) return;
        updateBlockLight(pos.asLong(), getLuminance(pos));
    }

    private int getLocalLightLevel(long pos) {
        return LightLevelStorage.LIGHT_LEVELS.containsKey(pos) ? LightLevelStorage.LIGHT_LEVELS.get(pos).value : Integer.MIN_VALUE; //getLightingProvider().get(LightType.BLOCK).getLightLevel(pos)
    }

    private void setLocalLightLevel(long pos, int lightLevel, long sourcePos) {
        LightLevelStorage.LIGHT_LEVELS.put(pos, new LightLevelStorageEntry(lightLevel, sourcePos));
    }

    private void updateBlockLight(long pos, int value) {
        MinerOverviewMod.LOGGER.info("Recalculating negative light values");
        long start = System.currentTimeMillis();

        if (value < 0 || value > getMaxLightLevel()) {
            throw new IllegalArgumentException();
        }

        // Reset all with sources in range
        Map<Long, LightLevelStorageEntry> sources = new LinkedHashMap<>();
        for (Map.Entry<Long, LightLevelStorageEntry> lightLevel : LightLevelStorage.LIGHT_LEVELS.entrySet()
            .stream().sorted((a, b) -> b.getValue().value - a.getValue().value).toList()) {
            long lightLevelPos = lightLevel.getKey();
            long sourcePos = lightLevel.getValue().sourcePos;

            if (lightLevelPos == sourcePos && BlockPos.fromLong(lightLevelPos).isWithinDistance(BlockPos.fromLong(pos), getMaxLightLevel()*2)) {
                sources.put(lightLevelPos, lightLevel.getValue());
                continue;
            }

            if (sources.containsKey(sourcePos) && lightLevelPos != sourcePos) {
                LightLevelStorage.LIGHT_LEVELS.remove(lightLevelPos);
            }
        }

        LightLevelStorage.LIGHT_LEVELS.remove(pos);

        // Requeue all sources except for changed
        for (Map.Entry<Long, LightLevelStorageEntry> entry : sources.entrySet()) {
            if (entry.getKey() == pos) continue;
            queue.add(new QueueEntry(entry.getKey(), entry.getValue()));
        }

        if (value > 0) {
            queue.add(new QueueEntry(pos, new LightLevelStorageEntry(value, pos)));
            setLocalLightLevel(pos, value, pos);
        }

        this.propagateIncrease();

        long end = System.currentTimeMillis();
        long time = end - start;
        MinerOverviewMod.LOGGER.info("Recalculated negative light values. It took: " + time + "ms");
    }

    private void propagateIncrease() {
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

                int neighbourStateOpacity = getBlockState(neighbourBlockPos).getOpacity(this, neighbourBlockPos);
                if (neighbourStateOpacity == getMaxLightLevel()) {
                    setLocalLightLevel(neighbourPos, 0, sourcePos);
                    continue;
                }

                int targetLevel = lightValue - Math.max(1, neighbourStateOpacity);
                if (targetLevel < -getMaxLightLevel()) continue;
                if (targetLevel > currentLevel) {
                    setLocalLightLevel(neighbourPos, targetLevel, sourcePos);
                    queue.add(new QueueEntry(neighbourPos, new LightLevelStorageEntry(targetLevel, sourcePos)));
                }
            }
        }
    }
}
