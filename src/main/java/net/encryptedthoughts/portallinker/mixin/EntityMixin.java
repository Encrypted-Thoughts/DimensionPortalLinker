package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow private World world;
    @Shadow protected BlockPos lastNetherPortalPosition;

    @Shadow public abstract Entity moveToWorld(ServerWorld destination);
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract float getYaw();
    @Shadow public abstract float getPitch();
    @Shadow protected abstract Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder);
    @Shadow protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

    @Redirect(method = "tickPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    public Entity actualDestinationKey(Entity entity, ServerWorld destination) {
        var currentInfo = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (currentInfo != null) {
            var server = world.getServer();
            var key = WorldHelper.getWorldRegistryKeyByName(server, currentInfo.NetherPortalDestinationDimension);
            return (key != null && currentInfo.IsNetherPortalEnabled) ? moveToWorldAlternative(entity, server.getWorld(key), currentInfo.Type.equals("minecraft:overworld")) : null;
        }
        return moveToWorld(destination);
    }

    private Entity moveToWorldAlternative(Entity entity, ServerWorld destination, boolean toNether) {
        var teleportTarget = getTeleportTargetAlternative(destination, toNether);
        return FabricDimensions.teleport(entity, destination, teleportTarget);
    }

    private TeleportTarget getTeleportTargetAlternative(ServerWorld destination, boolean toNether) {
        var worldBorder = destination.getWorldBorder();
        var d = DimensionType.getCoordinateScaleFactor(world.getDimension(), destination.getDimension());
        var blockPos2 = worldBorder.clamp(getX() * d, getY(), getZ() * d);
        return getPortalRect(destination, blockPos2, toNether, worldBorder).map((rect) -> {
            var blockState = world.getBlockState(lastNetherPortalPosition);
            Direction.Axis axis;
            Vec3d vec3d;
            if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                axis = blockState.get(Properties.HORIZONTAL_AXIS);
                BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(lastNetherPortalPosition, axis, 21, Direction.Axis.Y, 21, (pos) -> world.getBlockState(pos) == blockState);
                vec3d = positionInPortal(axis, rectangle);
            } else {
                axis = Direction.Axis.X;
                vec3d = new Vec3d(0.5, 0.0, 0.0);
            }

            return NetherPortal.getNetherTeleportTarget(destination, rect, axis, vec3d, (Entity)(Object)this, getVelocity(), getYaw(), getPitch());
        }).orElse(null);
    }
}