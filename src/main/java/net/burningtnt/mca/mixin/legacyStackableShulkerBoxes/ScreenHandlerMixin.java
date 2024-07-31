package net.burningtnt.mca.mixin.legacyStackableShulkerBoxes;

import net.burningtnt.mca.impl.legacyStackableShulkerBoxes.LegacyShulkerBoxStacker;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Redirect(
            method = "calculateComparatorOutput(Lnet/minecraft/inventory/Inventory;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/Inventory;getMaxCount(Lnet/minecraft/item/ItemStack;)I"
            )
    )
    private static int onCalculateComparatorOutput(Inventory instance, ItemStack stack) {
       return LegacyShulkerBoxStacker.getMaxCount(instance, stack);
    }
}
