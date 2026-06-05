package net.encryptedthoughts.portallinker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TheEndGatewayBlockEntity.class)
public abstract class EndGatewayBlockEntityMixin {
    @ModifyExpressionValue(method = "getPortalPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> portallinker_checkIfDimensionIsEnd(ResourceKey<Level> original) {
        var info = WorldHelper.getDimensionInfo(original.identifier().toString());
        if (info == null || !info.Type.equals("minecraft:the_end")) return original;
        return Level.END;
    }
}
