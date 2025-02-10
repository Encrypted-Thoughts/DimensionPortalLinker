package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends Entity {
    @Shadow @Nullable public abstract BlockPos getSpawnPointPosition();

    public ServerPlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN"), cancellable = true)
    public void portallinker_modifySpawnPointDimension(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir)
    {
        var info = WorldHelper.getDimensionInfo(getWorld().getRegistryKey().getValue().toString());
        var server = getServer();

        var playerSpawnPoint = this.getSpawnPointPosition();
        if (info != null && (info.OverridePlayerSpawn || (playerSpawnPoint == null && info.OverrideWorldSpawn)) && info.SpawnDimension != null && server != null) {
            var spawnPoint = info.getSpawnPoint();
            var world = WorldHelper.getWorldByName(server, info.SpawnDimension);
            if (spawnPoint != null && world != null)
                cir.setReturnValue(new TeleportTarget(world, spawnPoint.toBottomCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
        }
    }
}
