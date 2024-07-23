package net.burningtnt.pca.mixin.pcaSyncProtocol.block;

import net.burningtnt.pca.PcaMod;
import net.burningtnt.pca.PCASyncProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BarrelBlockEntity.class)
public abstract class MixinBarrelBlockEntity extends LootableContainerBlockEntity {

    protected MixinBarrelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update BarrelBlockEntity: {}", this.pos);
        }
    }
}