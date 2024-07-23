package net.burningtnt.pca.mixin.pcaSyncProtocol.block;

import net.burningtnt.pca.PcaMod;
import net.burningtnt.pca.PCASyncProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider {


    protected MixinAbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update AbstractFurnaceBlockEntity: {}", this.pos);
        }
    }
}