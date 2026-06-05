package net.encryptedthoughts.portallinker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DimensionInfo {
    public String Dimension;

    public String Type;
    public boolean IsNetherPortalEnabled;
    public String NetherPortalDestinationDimension;

    public boolean IsEndPortalEnabled;
    public String EndPortalDestinationDimension;

    public boolean OverrideWorldSpawn = false;
    public boolean OverridePlayerSpawn = false;
    public String SpawnDimension;
    public String SpawnPoint;

    public BlockPos getSpawnPoint() {
        try {
            var coordinate = SpawnPoint.split(",");
            var x = Integer.parseInt(coordinate[0].trim());
            var y = Integer.parseInt(coordinate[1].trim());
            var z = Integer.parseInt(coordinate[2].trim());
            return new BlockPos(x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    public MutableComponent getText() {
        var text = Component.literal("§aDimension: §r").append(Dimension != null ? Dimension : "").append("§r\n")
                .append("§aType: §r").append(Type != null ? Type : "").append("§r\n")
                .append("§aIsNetherPortalEnabled: §r").append(String.valueOf(IsNetherPortalEnabled)).append("§r\n");
        if (IsNetherPortalEnabled)
            text.append("§aNetherPortalDestinationDimension: §r").append(NetherPortalDestinationDimension != null ? NetherPortalDestinationDimension : "").append("§r\n");
        text.append("§aIsEndPortalEnabled: §r").append(String.valueOf(IsEndPortalEnabled)).append("§r\n");
        if (IsEndPortalEnabled)
            text.append("§aEndPortalDestinationDimension: §r").append(EndPortalDestinationDimension != null ? EndPortalDestinationDimension : "").append("§r\n");
        text.append("§aOverrideWorldSpawn: §r").append(String.valueOf(OverrideWorldSpawn)).append("§r\n")
            .append("§aOverridePlayerSpawn: §r").append(String.valueOf(OverridePlayerSpawn)).append("§r");
        if (OverrideWorldSpawn || OverridePlayerSpawn)
            text.append("\n§aSpawnDimension: §r").append(SpawnDimension != null ? SpawnDimension : "").append("§r\n")
                .append("§aSpawnPoint: §r").append(SpawnPoint != null ? SpawnPoint : "§r");
        return text;
    }
}
