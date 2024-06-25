package net.encryptedthoughts.portallinker.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {
    @ModifyVariable(method = "createTeleportTarget", at = @At(value = "STORE"), ordinal = 0)
    public RegistryKey<World> portallinker_modifyRegistryKey(RegistryKey<World> original, @Local(ordinal = 0, argsOnly = true) ServerWorld world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null) return original;
        return switch (info.Type) {
            case "minecraft:overworld" -> World.END;
            case "minecraft:the_end" -> World.OVERWORLD;
            default -> original;
        };
    }

    @ModifyVariable(method = "createTeleportTarget", at = @At(value = "STORE"), ordinal = 1)
    public ServerWorld portallinker_modifyServerWorld(ServerWorld original, @Local(ordinal = 0, argsOnly = true) ServerWorld world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null) return original;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.EndPortalDestinationDimension);
        return redirectedWorld == null ? original : redirectedWorld;
    }

    @Inject(method = "createTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getWorldSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"), cancellable = true)
    public void portallinker_modifyEntityWorldSpawn(ServerWorld original, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTarget> cir, @Local(ordinal = 0, argsOnly = true) ServerWorld world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null || !info.OverrideWorldSpawn || info.SpawnDimension == null) return;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.SpawnDimension);
        var spawnPoint = info.getSpawnPoint();
        if (redirectedWorld == null || spawnPoint == null) return;
        cir.setReturnValue(new TeleportTarget(redirectedWorld, spawnPoint.toBottomCenterPos(), entity.getVelocity(), entity.getYaw(), entity.getPitch(), TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET)));
    }

    @Inject(method = "createTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"), cancellable = true)
    public void portallinker_modifyPlayerWorldSpawn(ServerWorld original, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTarget> cir, @Local(ordinal = 0, argsOnly = true) ServerWorld world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null || !info.OverrideWorldSpawn || info.SpawnDimension == null) return;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.SpawnDimension);
        var spawnPoint = info.getSpawnPoint();
        if (redirectedWorld == null || spawnPoint == null) return;
        cir.setReturnValue(new TeleportTarget(redirectedWorld, spawnPoint.toBottomCenterPos(), entity.getVelocity(), entity.getYaw(), entity.getPitch(), TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET)));
    }

    @Inject(method = "onEntityCollision", at = @At(value = "HEAD"), cancellable = true)
    public void portallinker_disablePortal(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info != null && !info.IsEndPortalEnabled)
            ci.cancel();
    }
}
