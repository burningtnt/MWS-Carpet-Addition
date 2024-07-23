package net.burningtnt.pca.mixin.block;

import net.burningtnt.pca.PCAMod;
import net.burningtnt.pca.protocol.Protocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComparatorBlockEntity.class)
public abstract class MixinComparatorBlockEntity extends BlockEntity {
    public MixinComparatorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PCAMod.pcaSyncProtocol && Protocol.H_BE.tickTarget(this)) {
            PCAMod.LOGGER.debug("update ComparatorBlockEntity: {}", this.pos);
        }
    }
}
