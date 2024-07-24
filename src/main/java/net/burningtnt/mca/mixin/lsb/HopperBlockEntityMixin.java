package net.burningtnt.mca.mixin.lsb;

import net.burningtnt.mca.lsb.LegacyShulkerBoxStacker;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Redirect(
            method = "setStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;getMaxCount(Lnet/minecraft/item/ItemStack;)I"
            )
    )
    private int onCalculateMaxCount1(HopperBlockEntity instance, ItemStack stack) {
        return LegacyShulkerBoxStacker.getMaxCount(instance, stack);
    }

    @Redirect(
            method = {"isFull"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"
            )
    )
    private int onCalculateMaxCount2(ItemStack instance) {
        return LegacyShulkerBoxStacker.getMaxCount(instance);
    }

    @Redirect(
            method = {
                    "isInventoryFull",
                    "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
                    "canMergeItems"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"
            )
    )
    private static int onCalculateMaxCount3(ItemStack instance) {
        return LegacyShulkerBoxStacker.getMaxCount(instance);
    }
}
