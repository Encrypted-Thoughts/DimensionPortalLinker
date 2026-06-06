package net.encryptedthoughts.portallinker;

import net.encryptedthoughts.portallinker.command.PortalLinkerCommand;
import net.encryptedthoughts.portallinker.config.PortalLinkerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
				for (var world : server.getAllLevels())
				{
					var info = new DimensionInfo();
					info.Dimension = world.dimension().identifier().toString();
					info.Type = world.dimensionTypeRegistration().getRegisteredName();
					if (Level.OVERWORLD.equals(world.dimension())) {
						info.IsEndPortalEnabled = true;
						info.IsNetherPortalEnabled = true;
						info.NetherPortalDestinationDimension = Level.NETHER.identifier().toString();
						info.EndPortalDestinationDimension = Level.END.identifier().toString();
					} else if (Level.NETHER.equals(world.dimension())) {
						info.IsEndPortalEnabled = false;
						info.IsNetherPortalEnabled = true;
						info.NetherPortalDestinationDimension = Level.OVERWORLD.identifier().toString();
					} else if (Level.END.equals(world.dimension())) {
						info.IsEndPortalEnabled = true;
						info.IsNetherPortalEnabled = false;
						info.EndPortalDestinationDimension = Level.OVERWORLD.identifier().toString();
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

	private static boolean setAllDimensions(Player playerEntity) {
		var server = playerEntity.level().getServer();
		if (server != null) {
			for (var world : playerEntity.level().getServer().getAllLevels()) {
				var clock = world.dimensionTypeRegistration().value().defaultClock();
				clock.ifPresent(worldClockHolder -> world.clockManager().setTotalTicks(worldClockHolder, 0));
			}
		}
		return true;
	}
}
