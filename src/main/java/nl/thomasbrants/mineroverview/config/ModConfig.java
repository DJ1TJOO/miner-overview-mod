package nl.thomasbrants.mineroverview.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import nl.thomasbrants.mineroverview.helpers.Colors;

import java.util.HashSet;
import java.util.Set;

/**
 * Config.
 */
@Config(name = "miner_overview")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean toggleHud = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int textColor = Colors.white;

    @ConfigEntry.Gui.Tooltip
    public boolean toggleFps = true;

    @ConfigEntry.Gui.Tooltip
    public boolean toggleBiome = true;

    @ConfigEntry.Gui.CollapsibleObject
    public Coordinates coordinates = new Coordinates();

    public static class Coordinates {
        @ConfigEntry.Gui.Tooltip
        public boolean toggleCoordinates = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleDirection = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleDimensionConversion = false;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public LightLevel lightLevel = new LightLevel();

    public static class LightLevel {
        @ConfigEntry.Gui.Tooltip
        public boolean toggleLightLevel = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleLightLevelSpawnProof = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 15)
        public int minLightLevelSpawnProof = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = -4, max = 4)
        public int lightLevelHeight = 1;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public ItemOverview itemOverview = new ItemOverview();

    public static class ItemOverview {
        @ConfigEntry.Gui.Tooltip
        public boolean toggleItemOverview = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleMainHandItemOverview = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleOffHandItemOverview = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleArmorItemOverview = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleInventoryItemOverview = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleInventoryItemOverviewSlots = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleItemDamage = true;

        @ConfigEntry.Gui.Tooltip
        public boolean toggleTotalItemCount = true;
    }

    @ConfigEntry.Gui.Excluded
    public Set<Integer> renderedSlots = new HashSet<>();
}
