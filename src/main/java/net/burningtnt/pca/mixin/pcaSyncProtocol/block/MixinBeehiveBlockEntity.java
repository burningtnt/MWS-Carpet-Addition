package net.burningtnt.pca.mixin.pcaSyncProtocol.block;

import net.burningtnt.pca.PcaMod;
import net.burningtnt.pca.PCASyncProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(BeehiveBlockEntity.class)
public abstract class MixinBeehiveBlockEntity extends BlockEntity {

    public MixinBeehiveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "tickBees", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", shift = At.Shift.AFTER))
    private static void postTickBees(World world, BlockPos pos, BlockState state, List<BeehiveBlockEntity.Bee> bees, BlockPos flowerPos, CallbackInfo ci) {
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(Objects.requireNonNull(world.getBlockEntity(pos)))) {
            PcaMod.LOGGER.debug("update BeehiveBlockEntity: {}", pos);
        }
    }

    @Inject(method = "tryReleaseBee", at = @At(value = "RETURN"))
    public void postTryReleaseBee(BlockState state, BeehiveBlockEntity.BeeState beeState, CallbackInfoReturnable<List<Entity>> cir) {
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this) && cir.getReturnValue() != null) {
            PcaMod.LOGGER.debug("update BeehiveBlockEntity: {}", this.pos);
        }
    }

    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    public void postFromTag(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update BeehiveBlockEntity: {}", this.pos);
        }
    }

    @Inject(method = "tryEnterHive(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;discard()V", ordinal = 0))
    public void postEnterHive(Entity entity, CallbackInfo ci) {
        if (PcaMod.pcaSyncProtocol && PCASyncProtocol.syncBlockEntityToClient(this)) {
            PcaMod.LOGGER.debug("update BeehiveBlockEntity: {}", this.pos);
        }
    }
}