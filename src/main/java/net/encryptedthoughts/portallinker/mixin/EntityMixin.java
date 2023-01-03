package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public World world;
    @Shadow
    protected BlockPos lastNetherPortalPosition;

    @Shadow
    protected abstract Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder);

    @Shadow
    protected abstract Vec3d positionInPortal(Direction.Axis axis, BlockLocating.Rectangle rectangle);

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract float getYaw();

    @Shadow
    public abstract float getPitch();

    @Shadow
    protected abstract void removeFromDimension();

    @Shadow
    protected abstract TeleportTarget getTeleportTarget(ServerWorld destination);

    @ModifyVariable(method = "tickPortal", at = @At("STORE"), ordinal = 0)
    public ServerWorld actualDestinationKey(ServerWorld destination) {
        var currentInfo = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (currentInfo != null) {
            var server = destination.getServer();
            var key = WorldHelper.getWorldRegistryKeyByName(server, currentInfo.NetherPortalDestinationDimension);
            if (key != null && currentInfo.IsNetherPortalEnabled)
                return server.getWorld(key);
            else
                return null;
        }
        return destination;
    }

    @Inject(method = "getTeleportTarget", at = @At("HEAD"), cancellable = true)
    public void getTeleportTargetOverride(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        var teleportTarget = getDimensionTeleportTarget(destination);
        if (teleportTarget != null)
            cir.setReturnValue(teleportTarget);
    }

    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    public void moveToWorldOverride(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        var entity = (Entity) (Object) this;
        var currentInfo = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (currentInfo != null &&
                world instanceof ServerWorld &&
                !entity.isRemoved()) {

            var dimensionTargetType = currentInfo.NetherPortalDestinationDimension;
            if (currentInfo.Type.equals("minecraft:the_end") || destination.getDimensionKey().getValue().toString().equals("minecraft:the_end"))
                dimensionTargetType = currentInfo.EndPortalDestinationDimension;

            var actualDestination = WorldHelper.getWorldByName(world.getServer(), dimensionTargetType);
            var destinationInfo = WorldHelper.getDimensionInfo(actualDestination.getRegistryKey().getValue().toString());
            if (destinationInfo == null) cir.setReturnValue(null);

            world.getProfiler().push("changeDimension");
            entity.detach();
            world.getProfiler().push("reposition");
            TeleportTarget teleportTarget = getTeleportTarget(actualDestination);
            if (teleportTarget == null)
                cir.setReturnValue(null);
            else {
                this.world.getProfiler().swap("reloading");
                Entity newEntity = entity.getType().create(actualDestination);
                if (newEntity != null) {
                    newEntity.copyFrom(entity);
                    newEntity.refreshPositionAndAngles(
                            teleportTarget.position.x,
                            teleportTarget.position.y,
                            teleportTarget.position.z,
                            teleportTarget.yaw,
                            newEntity.getPitch());
                    newEntity.setVelocity(teleportTarget.velocity);
                    actualDestination.onDimensionChanged(newEntity);
                    if (destinationInfo.Type.equals("minecraft:the_end"))
                        ServerWorld.createEndSpawnPlatform(actualDestination);
                }

                removeFromDimension();
                world.getProfiler().pop();
                ((ServerWorld)world).resetIdleTimeout();
                actualDestination.resetIdleTimeout();
                world.getProfiler().pop();
                cir.setReturnValue(newEntity);
            }
        }
        cir.setReturnValue(null);
    }

    public TeleportTarget getDimensionTeleportTarget(ServerWorld destination) {
        var destinationInfo = WorldHelper.getDimensionInfo(destination.getRegistryKey().getValue().toString());
        var currentInfo = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());

        if (destinationInfo != null && currentInfo != null) {
            boolean fromEndToOverworld = currentInfo.Type.equals("minecraft:the_end") && destinationInfo.Type.equals("minecraft:overworld");
            boolean toEnd = destinationInfo.Type.equals("minecraft:the_end");
            if (!fromEndToOverworld && !toEnd) {
                boolean toNether = destinationInfo.Type.equals("minecraft:the_nether");
                if (!currentInfo.Type.equals("minecraft:the_nether") && !toNether)
                    return null;
                else {
                    var worldBorder = destination.getWorldBorder();
                    var scaleFactor = DimensionType.getCoordinateScaleFactor(
                            world.getDimension(),
                            destination.getDimension());
                    var portalPosition = worldBorder.clamp(
                            getX() * scaleFactor,
                            getY(),
                            getZ() * scaleFactor);
                    return getPortalRect(destination, portalPosition, toNether, worldBorder).map((rect) -> {
                        var blockState = world.getBlockState(lastNetherPortalPosition);
                        Direction.Axis axis;
                        Vec3d vec3d;
                        if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                            axis = blockState.get(Properties.HORIZONTAL_AXIS);
                            BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(
                                    lastNetherPortalPosition,
                                    axis,
                                    21,
                                    Direction.Axis.Y,
                                    21,
                                    (pos) -> world.getBlockState(pos) == blockState);
                            vec3d = positionInPortal(axis, rectangle);
                        } else {
                            axis = Direction.Axis.X;
                            vec3d = new Vec3d(0.5, 0.0, 0.0);
                        }

                        var entity = (Entity) (Object) this;
                        return NetherPortal.getNetherTeleportTarget(
                                destination,
                                rect,
                                axis,
                                vec3d,
                                entity,
                                getVelocity(),
                                getYaw(),
                                getPitch());
                    }).orElse(null);
                }
            } else {
                BlockPos blockPos;
                if (toEnd) blockPos = ServerWorld.END_SPAWN_POS;
                else
                    blockPos = destination.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, destination.getSpawnPos());

                return new TeleportTarget(
                        new Vec3d(
                                (double) blockPos.getX() + 0.5,
                                blockPos.getY(),
                                (double) blockPos.getZ() + 0.5),
                        getVelocity(),
                        getYaw(),
                        getPitch());
            }
        }
        return null;
    }
}