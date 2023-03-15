package net.encryptedthoughts.portallinker.mixin;

import com.mojang.authlib.GameProfile;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow private boolean inTeleportationState;
    @Shadow private Vec3d enteredNetherPos;
    @Shadow private int syncedExperience;
    @Shadow private float syncedHealth;
    @Shadow private int syncedFoodLevel;

    @Shadow protected abstract void createEndSpawnPlatform(ServerWorld world, BlockPos centerPos);
    @Shadow protected abstract void worldChanged(ServerWorld world);

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "getTeleportTarget", at = @At("HEAD"), cancellable = true)
    public void getTeleportTargetOverride(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        var teleportTarget = getDimensionTeleportTarget(destination);
        if (teleportTarget != null)
            cir.setReturnValue(teleportTarget);
    }

    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    public void moveToWorldOverride(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        var entity = (ServerPlayerEntity) (Object) this;
        var currentInfo = WorldHelper.getDimensionInfo(entity.world.getRegistryKey().getValue().toString());

        if (currentInfo != null && world instanceof ServerWorld) {

            var dimensionTargetType = currentInfo.NetherPortalDestinationDimension;
            if (currentInfo.Type.equals("minecraft:the_end") || destination.getDimensionKey().getValue().toString().equals("minecraft:the_end"))
                dimensionTargetType = currentInfo.EndPortalDestinationDimension;

            var actualDestination = WorldHelper.getWorldByName(world.getServer(), dimensionTargetType);
            var destinationInfo = WorldHelper.getDimensionInfo(actualDestination.getRegistryKey().getValue().toString());
            if (destinationInfo == null) cir.setReturnValue(null);

            inTeleportationState = true;
            var serverWorld = (ServerWorld) getWorld();
            if (currentInfo.Type.equals("minecraft:the_end") && destinationInfo.Type.equals("minecraft:overworld")) {
                detach();
                serverWorld.removePlayer(entity, RemovalReason.CHANGED_DIMENSION);
                if (!entity.notInAnyWorld) {
                    entity.notInAnyWorld = true;
                    entity.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, 0.0F));
                }
                cir.setReturnValue(entity);
            } else {
                var worldProperties = actualDestination.getLevelProperties();
                entity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(
                        actualDestination.getDimensionKey(),
                        actualDestination.getRegistryKey(),
                        BiomeAccess.hashSeed(actualDestination.getSeed()),
                        entity.interactionManager.getGameMode(),
                        entity.interactionManager.getPreviousGameMode(),
                        actualDestination.isDebugWorld(),
                        actualDestination.isFlat(),
                        (byte)3,
                        getLastDeathPos()));
                entity.networkHandler.sendPacket(new DifficultyS2CPacket(
                        worldProperties.getDifficulty(),
                        worldProperties.isDifficultyLocked()));
                var playerManager = entity.server.getPlayerManager();
                playerManager.sendCommandTree(entity);
                serverWorld.removePlayer(entity, RemovalReason.CHANGED_DIMENSION);
                unsetRemoved();
                TeleportTarget teleportTarget = getTeleportTarget(actualDestination);
                if (teleportTarget != null) {
                    serverWorld.getProfiler().push("moving");
                    if (currentInfo.Type.equals("minecraft:overworld") && destinationInfo.Type.equals("minecraft:the_nether"))
                        enteredNetherPos = getPos();
                    else if (destinationInfo.Type.equals("minecraft:the_end"))
                        createEndSpawnPlatform(actualDestination, BlockPos.ofFloored(teleportTarget.position));

                    serverWorld.getProfiler().pop();
                    serverWorld.getProfiler().push("placing");
                    entity.setWorld(actualDestination);
                    entity.networkHandler.requestTeleport(teleportTarget.position.x, teleportTarget.position.y, teleportTarget.position.z, teleportTarget.yaw, teleportTarget.pitch);
                    entity.networkHandler.syncWithPlayerPosition();
                    actualDestination.onPlayerChangeDimension(entity);
                    serverWorld.getProfiler().pop();
                    worldChanged(serverWorld);
                    entity.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(getAbilities()));
                    playerManager.sendWorldInfo(entity, actualDestination);
                    playerManager.sendPlayerStatus(entity);

                    for (var statusEffectInstance : getStatusEffects())
                        entity.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(getId(), statusEffectInstance));

                    entity.networkHandler.sendPacket(new WorldEventS2CPacket(1032, BlockPos.ORIGIN, 0, false));
                    syncedExperience = -1;
                    syncedHealth = -1.0F;
                    syncedFoodLevel = -1;
                }

                cir.setReturnValue(entity);
            }
        }

        cir.setReturnValue(null);
    }

    private TeleportTarget getDimensionTeleportTarget(ServerWorld destination) {
        var currentInfo = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (currentInfo != null) {
            var teleportTarget = super.getTeleportTarget(destination);
            if (teleportTarget != null && destination.getRegistryKey().getValue().toString().equals(currentInfo.EndPortalDestinationDimension)) {
                var vec3d = teleportTarget.position.add(0.0, -1.0, 0.0);
                return new TeleportTarget(vec3d, Vec3d.ZERO, 90.0F, 0.0F);
            } else
                return teleportTarget;
        }
        return null;
    }
}
