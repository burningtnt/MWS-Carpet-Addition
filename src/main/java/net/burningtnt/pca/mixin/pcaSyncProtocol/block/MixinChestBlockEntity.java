package net.burningtnt.pca.mixin.pcaSyncProtocol.block;

import net.burningtnt.pca.PcaMod;
import net.burningtnt.pca.PCASyncProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestBlockEntity.class)
public abstract class MixinChestBlockEntity extends LootableContainerBlockEntity {

    protected MixinChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update ChestBlockEntity: {}", this.pos);
        }
    }
}
