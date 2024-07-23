package net.burningtnt.pca.protocol.impl;

import net.burningtnt.pca.protocol.Protocol;
import net.burningtnt.pca.protocol.SynchronizeHandle;
import net.burningtnt.pca.utils.CarpetSettings;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class BlockEntitySynchronizeHandle extends SynchronizeHandle<BlockEntity> {
    public BlockEntitySynchronizeHandle() {
        super(Protocol.BE_START, Protocol.BE_CANCEL, Protocol.BE_DATA);
    }

    @Override
    public BlockEntity[] locateTargets(ServerPlayerEntity player, PacketByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ServerWorld world = player.getServerWorld();
        BlockState blockState = world.getBlockState(pos);

        BlockEntity target1 = world.getWorldChunk(pos).getBlockEntity(pos);
        if (target1 == null) {
            return findNothing();
        }

        BlockEntity target2 = null;
        if (blockState.getBlock() instanceof ChestBlock) {
            if (blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                BlockPos pos2 = pos.offset(ChestBlock.getFacing(blockState));
                target2 = world.getWorldChunk(pos2).getBlockEntity(pos2);
            }
        } else if (blockState.isOf(Blocks.BARREL) && CarpetSettings.getBoolRuleValue("largeBarrel", false)) {
            Direction direction = blockState.get(BarrelBlock.FACING).getOpposite();
            BlockPos pos2 = pos.offset(direction);
            BlockState blockState2 = world.getBlockState(pos2);
            if (blockState2.isOf(Blocks.BARREL) && blockState2.get(BarrelBlock.FACING) == direction) {
                target2 = world.getWorldChunk(pos2).getBlockEntity(pos2);
            }
        }

        if (target2 == null) {
            return new BlockEntity[]{target1};
        } else {
            return new BlockEntity[]{target1, target2};
        }
    }

    @Override
    public boolean encodeTarget(PacketByteBuf buf, BlockEntity blockEntity) {
        World world = blockEntity.getWorld();
        if (world == null) {
            return false;
        }

        buf.writeIdentifier(world.getRegistryKey().getValue());
        buf.writeBlockPos(blockEntity.getPos());
        buf.writeNbt(blockEntity.createComponentlessNbt(world.getRegistryManager()));
        return true;
    }
}
