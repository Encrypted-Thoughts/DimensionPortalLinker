package net.encryptedthoughts.portallinker.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {
    @ModifyVariable(method = "getPortalDestination", at = @At(value = "STORE"), ordinal = 0)
    public ResourceKey<Level> portallinker_modifyRegistryKey(ResourceKey<Level> original, @Local(ordinal = 0, argsOnly = true) ServerLevel world)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info == null) return original;
        return switch (info.Type) {
            case "minecraft:overworld" -> Level.END;
            case "minecraft:the_end" -> Level.OVERWORLD;
            default -> original;
        };
    }

    @ModifyVariable(method = "getPortalDestination", at = @At(value = "STORE"), ordinal = 1)
    public ServerLevel portallinker_modifyServerWorld(ServerLevel original, @Local(ordinal = 0, argsOnly = true) ServerLevel world)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info == null) return original;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.EndPortalDestinationDimension);
        return redirectedWorld == null ? original : redirectedWorld;
    }

    @Inject(method = "getPortalDestination", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"), cancellable = true)
    public void portallinker_modifyEntityWorldSpawn(ServerLevel original, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTransition> cir, @Local(ordinal = 0, argsOnly = true) ServerLevel world)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info == null || !info.OverrideWorldSpawn || info.SpawnDimension == null) return;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.SpawnDimension);
        var spawnPoint = info.getSpawnPoint();
        if (redirectedWorld == null || spawnPoint == null) return;
        cir.setReturnValue(new TeleportTransition(redirectedWorld, spawnPoint.getBottomCenter(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)));
    }

    @Inject(method = "getPortalDestination", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"), cancellable = true)
    public void portallinker_modifyPlayerWorldSpawn(ServerLevel original, Entity entity, BlockPos pos, CallbackInfoReturnable<TeleportTransition> cir, @Local(ordinal = 0, argsOnly = true) ServerLevel world)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info == null || !info.OverrideWorldSpawn || info.SpawnDimension == null) return;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.SpawnDimension);
        var spawnPoint = info.getSpawnPoint();
        if (redirectedWorld == null || spawnPoint == null) return;
        cir.setReturnValue(new TeleportTransition(redirectedWorld, spawnPoint.getBottomCenter(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)));
    }

    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    public void portallinker_disablePortal(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl, CallbackInfo ci)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info != null && !info.IsEndPortalEnabled)
            ci.cancel();
    }
}
