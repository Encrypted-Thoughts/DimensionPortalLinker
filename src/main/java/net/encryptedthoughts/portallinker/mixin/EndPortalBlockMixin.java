package net.encryptedthoughts.portallinker.mixin;

import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/registry/RegistryKey;"))
    public RegistryKey<World> redirectEndCheck(World world)
    {
        var info = WorldHelper.getDimensionInfo(world.getRegistryKey().getValue().toString());
        if (info.Type.equals("minecraft:the_end"))
            return World.END;
        else if (info.Type.equals("minecraft:overworld"))
            return World.OVERWORLD;
        return world.getRegistryKey();
    }
}
