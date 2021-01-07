package com.plusls.carpet.mixin.rule.pcaSyncProtocol.block;

import com.plusls.carpet.PcaMod;
import com.plusls.carpet.network.PcaSyncProtocol;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.Tickable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class MixinShulkerBoxBlockEntity extends LootableContainerBlockEntity implements SidedInventory, Tickable {

    protected MixinShulkerBoxBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PcaSyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update ShulkerBoxBlockEntity: {}", this.pos);
        }
    }
}