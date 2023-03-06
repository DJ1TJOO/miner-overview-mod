package nl.thomasbrants.mineroverview.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "miner_overview")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean hudToggle = true;
}
