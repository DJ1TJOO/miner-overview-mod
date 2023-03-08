package nl.thomasbrants.mineroverview;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import nl.thomasbrants.mineroverview.config.ModConfig;
import nl.thomasbrants.mineroverview.hud.GameMinerHud;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinerOverviewMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("miner_overview");
	private static GameMinerHud overviewHud;

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		LOGGER.info("Loaded!");

		LOGGER.info("Registering config");
		ConfigHolder<ModConfig>
			configHolder = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		ModConfig config = configHolder.getConfig();

		LOGGER.info("Registering keybindings");
		KeyBinding hudToggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.miner_overview.hud_toggle",
			GLFW.GLFW_KEY_M,
			"category.miner_overview.keybindings"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			if (hudToggleKeyBinding.wasPressed()) {
				config.toggleHud = !config.toggleHud;
				configHolder.save();
			}
		});
	}

	public static GameMinerHud getOverviewHud() {
		return overviewHud;
	}

	public static void setOverviewHud(GameMinerHud overviewHud) {
		MinerOverviewMod.overviewHud = overviewHud;
	}
}
