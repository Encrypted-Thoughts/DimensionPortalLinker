package net.encryptedthoughts.portallinker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndGatewayBlockEntity.class)
public abstract class EndGatewayBlockEntityMixin {
    @ModifyExpressionValue(method = "getOrCreateExitPortalPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> portallinker_checkIfDimensionIsEnd(RegistryKey<World> original) {
        var info = WorldHelper.getDimensionInfo(original.getValue().toString());
        if (info == null || !info.Type.equals("minecraft:the_end")) return original;
        return World.END;
    }
}
