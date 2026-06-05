package net.encryptedthoughts.portallinker.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    @ModifyVariable(method = "getPortalDestination", at = @At(value = "STORE"), ordinal = 0)
    public ResourceKey<Level> portallinker_modifyRegistryKey(ResourceKey<Level> original, @Local(ordinal = 0, argsOnly = true) ServerLevel world)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info == null) return original;

        return switch (info.Type) {
            case "minecraft:overworld" -> Level.NETHER;
            case "minecraft:the_nether" -> Level.OVERWORLD;
            default -> original;
        };
    }

    @ModifyVariable(method = "getPortalDestination", at = @At(value = "STORE"), ordinal = 1)
    public ServerLevel portallinker_modifyServerWorld(ServerLevel original, @Local(ordinal = 0, argsOnly = true) ServerLevel world)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info == null) return original;

        var redirectedWorld = WorldHelper.getWorldByName(world.getServer(), info.NetherPortalDestinationDimension);
        return redirectedWorld == null ? original : redirectedWorld;
    }

    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    public void portallinker_disablePortal(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl, CallbackInfo ci)
    {
        var info = WorldHelper.getDimensionInfo(world.dimension().identifier().toString());
        if (info != null && !info.IsNetherPortalEnabled)
            ci.cancel();
    }
}
