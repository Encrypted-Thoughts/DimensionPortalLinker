package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.DimensionInfo;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Entity {
    @Shadow public abstract ServerLevel level();

    public ServerPlayerEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void portallinker_modifyPlayerSpawn(boolean alive, TeleportTransition.PostTeleportTransition postDimensionTransition, CallbackInfoReturnable<TeleportTransition> cir)
    {
        var info = WorldHelper.getDimensionInfo(level().dimension().identifier().toString());
        if (info != null && info.OverridePlayerSpawn)
            overrideSpawn(info, postDimensionTransition, cir);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    public void portallinker_modifyFailedPlayerSpawn(boolean alive, TeleportTransition.PostTeleportTransition postDimensionTransition, CallbackInfoReturnable<TeleportTransition> cir)
    {
        var info = WorldHelper.getDimensionInfo(level().dimension().identifier().toString());
        if (info != null && info.OverrideWorldSpawn)
            overrideSpawn(info, postDimensionTransition, cir);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    public void portallinker_modifyWorldSpawn(boolean alive, TeleportTransition.PostTeleportTransition postDimensionTransition, CallbackInfoReturnable<TeleportTransition> cir)
    {
        var info = WorldHelper.getDimensionInfo(level().dimension().identifier().toString());
        if (info != null && info.OverrideWorldSpawn)
            overrideSpawn(info, postDimensionTransition, cir);
    }

    @Unique
    private void overrideSpawn(DimensionInfo info, TeleportTransition.PostTeleportTransition postDimensionTransition, CallbackInfoReturnable<TeleportTransition> cir) {
        var server = level().getServer();
        if (info.SpawnDimension != null) {
            var spawnPoint = info.getSpawnPoint();
            var world = WorldHelper.getWorldByName(server, info.SpawnDimension);
            if (spawnPoint != null && world != null)
                cir.setReturnValue(new TeleportTransition(world, spawnPoint.getBottomCenter(), Vec3.ZERO, 0.0F, 0.0F, postDimensionTransition));
        }
    }
}
