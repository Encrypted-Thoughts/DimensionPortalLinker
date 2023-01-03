package net.encryptedthoughts.portallinker.util;

import net.encryptedthoughts.portallinker.DimensionInfo;
import net.encryptedthoughts.portallinker.PortalLinkerMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class WorldHelper {
    public static ServerWorld getWorldByName(MinecraftServer server, String worldName){
        for (var key : server.getWorldRegistryKeys()) {
            if (key.getValue().toString().equals(worldName))
                return server.getWorld(key);
        }
        return null;
    }

    public static RegistryKey<World> getWorldRegistryKeyByName(MinecraftServer server, String worldName){
        for (var key : server.getWorldRegistryKeys()) {
            if (key.getValue().toString().equals(worldName))
                return key;
        }
        return null;
    }

    public static DimensionInfo getDimensionInfo(String worldName)
    {
        for (var info : PortalLinkerMod.CONFIG.Dimensions)
        {
            if (info.Dimension.equals(worldName))
                return info;
        }

        var info = new DimensionInfo();
        info.Dimension = worldName;
        info.IsNetherPortalEnabled = false;
        info.IsEndPortalEnabled = false;
        return info;
    }
}
