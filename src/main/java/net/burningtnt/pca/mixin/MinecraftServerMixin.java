package net.burningtnt.pca.mixin;

import net.burningtnt.pca.PCASyncProtocol;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onGameInit(CallbackInfo ci)
    {
        PCASyncProtocol.server = (MinecraftServer)(Object) this;
    }
}
