package net.encryptedthoughts.portallinker.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    @ModifyVariable(method = "createTeleportTarget", at = @At(value = "STORE"), ordinal = 0)
    public RegistryKey<World> portallinker_modifyRegistryKey(RegistryKey<World> original, @Local(ordinal = 0, argsOnly = true) ServerWorld world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null) return original;

        return switch (info.Type) {
            case "minecraft:overworld" -> World.NETHER;
            case "minecraft:the_nether" -> World.OVERWORLD;
            default -> original;
        };
    }

    @ModifyVariable(method = "createTeleportTarget", at = @At(value = "STORE"), ordinal = 1)
    public ServerWorld portallinker_modifyServerWorld(ServerWorld original, @Local(ordinal = 0, argsOnly = true) ServerWorld world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info == null) return original;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.NetherPortalDestinationDimension);
        return redirectedWorld == null ? original : redirectedWorld;
    }

    @Inject(method = "onEntityCollision", at = @At(value = "HEAD"), cancellable = true)
    public void portallinker_disablePortal(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, CallbackInfo ci)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info != null && !info.IsNetherPortalEnabled)
            ci.cancel();
    }
}
