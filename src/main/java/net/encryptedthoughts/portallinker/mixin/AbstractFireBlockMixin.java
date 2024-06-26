package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public abstract class AbstractFireBlockMixin {
    @Inject(at = @At("HEAD"), method = "isOverworldOrNether", cancellable = true)
    private static void portallinker_isOverworldOrNether(World world, CallbackInfoReturnable<Boolean> cir) {
        var worldName = world.getRegistryKey().getValue().toString();
        var dimensionInfo = WorldHelper.getDimensionInfo(worldName);
        if (dimensionInfo != null)
            cir.setReturnValue(dimensionInfo.IsNetherPortalEnabled);
    }
}
