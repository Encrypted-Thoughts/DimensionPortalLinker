package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends Entity {

    @Shadow @Nullable public abstract BlockPos getSpawnPointPosition();

    @Shadow public abstract RegistryKey<World> getSpawnPointDimension();

    @Shadow public abstract float getSpawnAngle();

    @Shadow public abstract boolean isSpawnForced();

    public ServerPlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN"), cancellable = true)
    public void portallinker_modifySpawnPointDimension(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir)
    {
        var info = WorldHelper.getDimensionInfo(getWorld().getRegistryKey().getValue().toString());
        var server = getServer();

        var validPlayerSpawn = false;
        var playerSpawnPoint = this.getSpawnPointPosition();
        if (playerSpawnPoint != null && server != null) {
            var serverWorld = server.getWorld(this.getSpawnPointDimension());
            if (serverWorld != null)
                validPlayerSpawn = isValidRespawnPosition(serverWorld, playerSpawnPoint, this.getSpawnAngle(), this.isSpawnForced());
        }

        if (info != null && (info.OverridePlayerSpawn || (!validPlayerSpawn && info.OverrideWorldSpawn)) && info.SpawnDimension != null && server != null) {
            var spawnPoint = info.getSpawnPoint();
            var world = WorldHelper.getWorldByName(server, info.SpawnDimension);
            if (spawnPoint != null && world != null)
                cir.setReturnValue(new TeleportTarget(world, spawnPoint.toBottomCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
        }
    }

    @Unique
    private static boolean isValidRespawnPosition(ServerWorld world, BlockPos pos, float spawnAngle, boolean spawnForced) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock && (spawnForced || blockState.get(RespawnAnchorBlock.CHARGES) > 0) && RespawnAnchorBlock.isNether(world)) {
            return RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos).isPresent();
        } else if (block instanceof BedBlock && BedBlock.isBedWorking(world)) {
            return BedBlock.findWakeUpPosition(EntityType.PLAYER, world, pos, blockState.get(BedBlock.FACING), spawnAngle).isPresent();
        } else if (!spawnForced) {
            return false;
        } else {
            boolean canMobSpawnInsideLegs = block.canMobSpawnInside(blockState);
            BlockState headBlockState = world.getBlockState(pos.up());
            boolean canMobSpawnInsideHead = headBlockState.getBlock().canMobSpawnInside(headBlockState);
            return canMobSpawnInsideLegs && canMobSpawnInsideHead;
        }
    }
}
