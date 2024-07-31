package net.burningtnt.mca.mixin.legacyCarpetTickCommand;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import net.burningtnt.mca.carpet.MWSCarpetSettings;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TickCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @ModifyArg(
            method = "method_54688",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/TickCommand;executeFreeze(Lnet/minecraft/server/command/ServerCommandSource;Z)I"
            ),
            index = 1
    )
    private static boolean modifyOperationType(boolean frozen, @Local(ordinal = 0, argsOnly = true) CommandContext<ServerCommandSource> context) {
        return MWSCarpetSettings.legacyCarpetTickCommand ? !context.getSource().getServer().getTickManager().isFrozen() : frozen;
    }
}
