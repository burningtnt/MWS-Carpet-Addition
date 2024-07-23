package net.burningtnt.pca.mixin.block;

import net.burningtnt.pca.PCAMod;
import net.burningtnt.pca.protocol.Protocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
    protected MixinBrewingStandBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PCAMod.pcaSyncProtocol && Protocol.H_BE.tickTarget(this)) {
            PCAMod.LOGGER.debug("update BrewingStandBlockEntity: {}", this.pos);
        }
    }
}