package net.burningtnt.mca.impl.legacyStackableShulkerBoxes;

import net.burningtnt.mca.carpet.MWSCarpetSettings;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public final class LegacyShulkerBoxStacker {
    private LegacyShulkerBoxStacker() {
    }

    public static int getMaxCount(Inventory inventory, ItemStack stack) {
        if (shouldOverride(stack)) {
            return 1;
        }

        return inventory.getMaxCount(stack);
    }

    public static int getMaxCount(ItemStack stack) {
        if (shouldOverride(stack)) {
            return 1;
        }

        return stack.getMaxCount();
    }

    public static boolean shouldOverride(ItemStack stack) {
        return MWSCarpetSettings.legacyStackableShulkerBoxes && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }
}
