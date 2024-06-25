package net.encryptedthoughts.portallinker;

import net.encryptedthoughts.portallinker.command.PortalLinkerCommand;
import net.encryptedthoughts.portallinker.config.PortalLinkerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalLinkerMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("portal_linker");
	public static PortalLinkerConfig CONFIG = new PortalLinkerConfig();

	@Override
	public void onInitialize() {
		LOGGER.info("Initialized Dimension Portal Linker");

		CommandRegistrationCallback.EVENT.register(PortalLinkerCommand::register);

		if (!CONFIG.ReadFromFile()) {
			ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
				for (var world : server.getWorlds())
				{
					var info = new DimensionInfo();
					info.Dimension = world.getRegistryKey().getValue().toString();
					info.Type = world.getDimensionEntry().getIdAsString();
					if (World.OVERWORLD.equals(world.getRegistryKey())) {
						info.IsEndPortalEnabled = true;
						info.IsNetherPortalEnabled = true;
						info.NetherPortalDestinationDimension = World.NETHER.getValue().toString();
						info.EndPortalDestinationDimension = World.END.getValue().toString();
					} else if (World.NETHER.equals(world.getRegistryKey())) {
						info.IsEndPortalEnabled = false;
						info.IsNetherPortalEnabled = true;
						info.NetherPortalDestinationDimension = World.OVERWORLD.getValue().toString();
					} else if (World.END.equals(world.getRegistryKey())) {
						info.IsEndPortalEnabled = true;
						info.IsNetherPortalEnabled = false;
						info.EndPortalDestinationDimension = World.OVERWORLD.getValue().toString();
					}
					else {
						info.IsEndPortalEnabled = false;
						info.IsNetherPortalEnabled = false;
					}
					CONFIG.Dimensions.add(info);
				}
				CONFIG.SaveToFile();
			});
		}

		EntitySleepEvents.ALLOW_RESETTING_TIME.register(PortalLinkerMod::setAllDimensions);
	}

	private static boolean setAllDimensions(PlayerEntity playerEntity) {
		var server = playerEntity.getServer();
		if (server != null) {
			for (var world : playerEntity.getServer().getWorlds()) {
				world.setTimeOfDay(0);
			}
		}
		return true;
	}
}
