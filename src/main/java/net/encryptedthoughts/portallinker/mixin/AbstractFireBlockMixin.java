package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public abstract class AbstractFireBlockMixin {
    @Inject(at = @At("HEAD"), method = "inPortalDimension", cancellable = true)
    private static void portallinker_isOverworldOrNether(Level world, CallbackInfoReturnable<Boolean> cir) {
        var worldName = world.dimension().identifier().toString();
        var dimensionInfo = WorldHelper.getDimensionInfo(worldName);
        if (dimensionInfo != null)
            cir.setReturnValue(dimensionInfo.IsNetherPortalEnabled);
    }
}
