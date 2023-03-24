package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    public Entity redirectEndMove(Entity entity, ServerWorld ignored)
    {
        var world = entity.getWorld();
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null) return entity.moveToWorld(ignored);

        if (info.IsEndPortalEnabled) {
            var server = world.getServer();
            var key = WorldHelper.getWorldRegistryKeyByName(server, info.EndPortalDestinationDimension);
            if (key == null) return null;

            var destination = server.getWorld(key);
            if (info.Type.equals("minecraft:overworld")) {
                ServerWorld.createEndSpawnPlatform(destination);
                var blockPos = ServerWorld.END_SPAWN_POS;
                var target = new TeleportTarget(new Vec3d((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5), entity.getVelocity(), entity.getYaw(), entity.getPitch());
                return FabricDimensions.teleport(entity, destination, target);
            } else {
                var blockPos = getTopBlock(destination, destination.getSpawnPos());
                var target = new TeleportTarget(new Vec3d((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5), entity.getVelocity(), entity.getYaw(), entity.getPitch());
                return FabricDimensions.teleport(entity, destination, target);
            }
        }
        return null;
    }

    private BlockPos getTopBlock(ServerWorld world, BlockPos pos) {
        var mutable = new BlockPos.Mutable(pos.getX(), 256, pos.getZ());
        while (world.getBlockState(mutable).isAir() && mutable.getY() > world.getBottomY())
            mutable.move(Direction.DOWN);
        return mutable.move(Direction.UP);
    }
}
