package net.burningtnt.pca.mixin.pcaSyncProtocol.block;

import net.burningtnt.pca.PcaMod;
import net.burningtnt.pca.PCASyncProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DispenserBlockEntity.class)
public abstract class MixinDispenserBlockEntity extends LootableContainerBlockEntity {

    protected MixinDispenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update DispenserBlockEntity: {}", this.pos);
        }
    }
}