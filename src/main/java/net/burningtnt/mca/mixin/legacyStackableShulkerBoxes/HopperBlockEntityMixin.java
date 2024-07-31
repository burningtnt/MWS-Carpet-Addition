package net.burningtnt.mca.mixin.legacyStackableShulkerBoxes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.burningtnt.mca.impl.legacyStackableShulkerBoxes.LegacyShulkerBoxStacker;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
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

    @Redirect(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"
            )
    )
    private static void redirectSetStackFromItem(Inventory instance, int i, ItemStack stack) {
        if (!LegacyShulkerBoxStacker.shouldOverride(stack)) {
            instance.setStack(i, stack);
        } else {
            instance.setStack(i, stack.copyWithCount(1));
        }
    }

    @ModifyExpressionValue(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/item/ItemStack;EMPTY:Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack onInsertItem(ItemStack rawValue, @Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        if (!LegacyShulkerBoxStacker.shouldOverride(stack)) {
            return rawValue;
        }

        if (stack.getCount() == 1) {
            return ItemStack.EMPTY;
        }
        stack.decrement(1);
        return stack;
    }
}
