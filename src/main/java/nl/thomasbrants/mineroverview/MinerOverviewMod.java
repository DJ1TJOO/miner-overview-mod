package nl.thomasbrants.mineroverview;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import nl.thomasbrants.mineroverview.config.ModConfig;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinerOverviewMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("miner_overview");

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		LOGGER.info("Loaded!");

		LOGGER.info("Registering config");
		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);

		LOGGER.info("Registering keybindings");
		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.miner_overview.hud_toggle",
			GLFW.GLFW_KEY_M,
			"category.miner_overview.keybindings"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			if (keyBinding.wasPressed()) {
				ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
				config.hudToggle = !config.hudToggle;

				LOGGER.info("Toggled hud to " + config.hudToggle);

				AutoConfig.getConfigHolder(ModConfig.class).save();
			}
		});
	}
}
