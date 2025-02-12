package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.DimensionInfo;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends Entity {
    public ServerPlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void portallinker_modifyPlayerSpawn(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir)
    {
        var info = WorldHelper.getDimensionInfo(getWorld().getRegistryKey().getValue().toString());
        if (info != null && info.OverridePlayerSpawn)
            overrideSpawn(info, postDimensionTransition, cir);
    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    public void portallinker_modifyFailedPlayerSpawn(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir)
    {
        var info = WorldHelper.getDimensionInfo(getWorld().getRegistryKey().getValue().toString());
        if (info != null && info.OverrideWorldSpawn)
            overrideSpawn(info, postDimensionTransition, cir);
    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    public void portallinker_modifyWorldSpawn(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir)
    {
        var info = WorldHelper.getDimensionInfo(getWorld().getRegistryKey().getValue().toString());
        if (info != null && info.OverrideWorldSpawn)
            overrideSpawn(info, postDimensionTransition, cir);
    }

    @Unique
    private void overrideSpawn(DimensionInfo info, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        var server = getServer();
        if (info.SpawnDimension != null && server != null) {
            var spawnPoint = info.getSpawnPoint();
            var world = WorldHelper.getWorldByName(server, info.SpawnDimension);
            if (spawnPoint != null && world != null)
                cir.setReturnValue(new TeleportTarget(world, spawnPoint.toBottomCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
        }
    }
}
