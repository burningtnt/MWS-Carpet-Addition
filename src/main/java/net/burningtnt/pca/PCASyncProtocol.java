package net.burningtnt.pca;

import io.netty.buffer.Unpooled;
import net.burningtnt.pca.network.NetworkingHandle;
import net.burningtnt.pca.protocol.ProtocolConstants;
import net.burningtnt.pca.util.CarpetHelper;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class PCASyncProtocol {
    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);

    private static final Map<ServerPlayerEntity, Pair<Identifier, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayerEntity, Pair<Identifier, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<Identifier, BlockPos>, Set<ServerPlayerEntity>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<Identifier, Entity>, Set<ServerPlayerEntity>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<Identifier, Entity> identifierEntityPair = new MutablePair<>();
    private static final MutablePair<Identifier, BlockPos> identifierBlockPosPair = new MutablePair<>();

    @Nullable
    public static volatile MinecraftServer server = null;

    public static void enablePcaSyncProtocol(@NotNull ServerPlayerEntity player) {
        PcaMod.LOGGER.debug("Sending enablePcaSyncProtocol to player: {}", player.getName().getString());

        NetworkingHandle.send(player, ProtocolConstants.ENABLE_PCA_SYNC_PROTOCOL, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void disablePcaSyncProtocol(@NotNull ServerPlayerEntity player) {
        PcaMod.LOGGER.debug("Disabling enablePcaSyncProtocol to {}!", player.getName().getString());

        NetworkingHandle.send(player, ProtocolConstants.DISABLE_PCA_SYNC_PROTOCOL, new PacketByteBuf(Unpooled.buffer()));
    }

    public static void updateEntity(@NotNull ServerPlayerEntity player, @NotNull Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeIdentifier(entity.getEntityWorld().getRegistryKey().getValue());
        buf.writeInt(entity.getId());
        buf.writeNbt(entity.writeNbt(new NbtCompound()));

        NetworkingHandle.send(player, ProtocolConstants.UPDATE_ENTITY, buf);
    }

    public static void updateBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockEntity blockEntity) {
        World world = blockEntity.getWorld();
        if (world == null) {
            return;
        }

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeIdentifier(world.getRegistryKey().getValue());
        buf.writeBlockPos(blockEntity.getPos());
        buf.writeNbt(blockEntity.createComponentlessNbt(world.getRegistryManager()));

        NetworkingHandle.send(player, ProtocolConstants.UPDATE_BLOCK_ENTITY, buf);
    }

    public static void onDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler) {
        if (PcaMod.pcaSyncProtocol) {
            PcaMod.LOGGER.debug("onDisconnect remove: {}", serverPlayNetworkHandler.player.getName().getString());
        }
        PCASyncProtocol.clearPlayerWatchData(serverPlayNetworkHandler.player);
    }

    public static void onJoin(ServerPlayNetworkHandler serverPlayNetworkHandler) {
        if (PcaMod.pcaSyncProtocol) {
            enablePcaSyncProtocol(serverPlayNetworkHandler.player);
        }
    }

    public static void cancelSyncBlockEntityHandler(ServerPlayerEntity player) {
        if (!PcaMod.pcaSyncProtocol) {
            return;
        }
        PcaMod.LOGGER.info("{} cancel watch blockEntity.", player.getName().getString());
        PCASyncProtocol.clearPlayerWatchBlock(player);
    }

    public static void cancelSyncEntityHandler(ServerPlayerEntity player) {
        if (!PcaMod.pcaSyncProtocol) {
            return;
        }
        PcaMod.LOGGER.info("{} cancel watch entity.", player.getName().getString());
        PCASyncProtocol.clearPlayerWatchEntity(player);
    }

    public static void syncBlockEntity(ServerPlayerEntity player, PacketByteBuf buf) {
        if (!PcaMod.pcaSyncProtocol) {
            return;
        }

        BlockPos pos = buf.readBlockPos();
        ServerWorld world = player.getServerWorld();
        BlockState blockState = world.getBlockState(pos);
        clearPlayerWatchData(player);
        PcaMod.LOGGER.info("{} watch blockpos {}: {}", player.getName().getString(), pos, blockState);

        BlockEntity blockEntityAdj = null;
        if (blockState.getBlock() instanceof ChestBlock) {
            if (blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                BlockPos posAdj = pos.offset(ChestBlock.getFacing(blockState));
                blockEntityAdj = world.getWorldChunk(posAdj).getBlockEntity(posAdj);
            }
        } else if (blockState.isOf(Blocks.BARREL) && CarpetHelper.getBoolRuleValue("largeBarrel", false)) {
            Direction directionOpposite = blockState.get(BarrelBlock.FACING).getOpposite();
            BlockPos posAdj = pos.offset(directionOpposite);
            BlockState blockStateAdj = world.getBlockState(posAdj);
            if (blockStateAdj.isOf(Blocks.BARREL) && blockStateAdj.get(BarrelBlock.FACING) == directionOpposite) {
                blockEntityAdj = world.getWorldChunk(posAdj).getBlockEntity(posAdj);
            }
        }

        if (blockEntityAdj != null) {
            updateBlockEntity(player, blockEntityAdj);
        }

        BlockEntity blockEntity = world.getWorldChunk(pos).getBlockEntity(pos);
        if (blockEntity != null) {
            updateBlockEntity(player, blockEntity);
        }

        Pair<Identifier, BlockPos> pair = new ImmutablePair<>(player.getEntityWorld().getRegistryKey().getValue(), pos);
        lock.lock();
        playerWatchBlockPos.put(player, pair);
        if (!blockPosWatchPlayerSet.containsKey(pair)) {
            blockPosWatchPlayerSet.put(pair, new HashSet<>());
        }
        blockPosWatchPlayerSet.get(pair).add(player);
        lock.unlock();
    }

    public static void syncEntityHandler(ServerPlayerEntity player, PacketByteBuf buf) {
        if (!PcaMod.pcaSyncProtocol) {
            return;
        }
        int entityId = buf.readInt();
        ServerWorld world = player.getServerWorld();
        Entity entity = world.getEntityById(entityId);
        if (entity == null) {
            PcaMod.LOGGER.info("Can't find entity {}.", entityId);
        } else {
            clearPlayerWatchData(player);
            PcaMod.LOGGER.info("{} watch entity {}: {}", player.getName().getString(), entityId, entity);
            updateEntity(player, entity);

            Pair<Identifier, Entity> pair = new ImmutablePair<>(entity.getEntityWorld().getRegistryKey().getValue(), entity);
            lock.lock();
            playerWatchEntity.put(player, pair);
            if (!entityWatchPlayerSet.containsKey(pair)) {
                entityWatchPlayerSet.put(pair, new HashSet<>());
            }
            entityWatchPlayerSet.get(pair).add(player);
            lock.unlock();
        }
    }

    private static MutablePair<Identifier, Entity> getIdentifierEntityPair(Identifier identifier, Entity entity) {
        pairLock.lock();
        identifierEntityPair.setLeft(identifier);
        identifierEntityPair.setRight(entity);
        pairLock.unlock();
        return identifierEntityPair;
    }

    private static MutablePair<Identifier, BlockPos> getIdentifierBlockPosPair(Identifier identifier, BlockPos pos) {
        pairLock.lock();
        identifierBlockPosPair.setLeft(identifier);
        identifierBlockPosPair.setRight(pos);
        pairLock.unlock();
        return identifierBlockPosPair;
    }

    // 工具
    private static @Nullable Set<ServerPlayerEntity> getWatchPlayerList(@NotNull Entity entity) {
        return entityWatchPlayerSet.get(getIdentifierEntityPair(entity.getEntityWorld().getRegistryKey().getValue(), entity));
    }

    private static @Nullable Set<ServerPlayerEntity> getWatchPlayerList(@NotNull World world, @NotNull BlockPos blockPos) {
        return blockPosWatchPlayerSet.get(getIdentifierBlockPosPair(world.getRegistryKey().getValue(), blockPos));
    }

    public static boolean syncEntityToClient(@NotNull Entity entity) {
        if (entity.getEntityWorld().isClient()) {
            return false;
        }
        lock.lock();
        Set<ServerPlayerEntity> playerList = getWatchPlayerList(entity);
        boolean ret = false;
        if (playerList != null) {
            for (ServerPlayerEntity player : playerList) {
                updateEntity(player, entity);
                ret = true;
            }
        }
        lock.unlock();
        return ret;
    }

    public static boolean syncBlockEntityToClient(@NotNull BlockEntity blockEntity) {
        boolean ret = false;
        World world = blockEntity.getWorld();
        BlockPos pos = blockEntity.getPos();
        // 在生成世界时可能会产生空指针
        if (world != null) {
            if (world.isClient()) {
                return false;
            }
            BlockState blockState = world.getBlockState(pos);
            lock.lock();
            Set<ServerPlayerEntity> playerList = getWatchPlayerList(world, blockEntity.getPos());

            Set<ServerPlayerEntity> playerListAdj = null;

            if (blockState.getBlock() instanceof ChestBlock) {
                if (blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                    // 如果是一个大箱子需要特殊处理
                    // 上面不用 isOf 是为了考虑到陷阱箱的情况，陷阱箱继承自箱子
                    BlockPos posAdj = pos.offset(ChestBlock.getFacing(blockState));
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            } else if (blockState.isOf(Blocks.BARREL) && CarpetHelper.getBoolRuleValue("largeBarrel", false)) {
                Direction directionOpposite = blockState.get(BarrelBlock.FACING).getOpposite();
                BlockPos posAdj = pos.offset(directionOpposite);
                BlockState blockStateAdj = world.getBlockState(posAdj);
                if (blockStateAdj.isOf(Blocks.BARREL) && blockStateAdj.get(BarrelBlock.FACING) == directionOpposite) {
                    playerListAdj = getWatchPlayerList(world, posAdj);
                }
            }
            if (playerListAdj != null) {
                if (playerList == null) {
                    playerList = playerListAdj;
                } else {
                    playerList.addAll(playerListAdj);
                }
            }

            if (playerList != null) {
                for (ServerPlayerEntity player : playerList) {
                    updateBlockEntity(player, blockEntity);
                    ret = true;
                }
            }
            lock.unlock();
        }
        return ret;
    }

    private static void clearPlayerWatchEntity(ServerPlayerEntity player) {
        lock.lock();
        Pair<Identifier, Entity> pair = playerWatchEntity.get(player);
        if (pair != null) {
            Set<ServerPlayerEntity> playerSet = entityWatchPlayerSet.get(pair);
            playerSet.remove(player);
            if (playerSet.isEmpty()) {
                entityWatchPlayerSet.remove(pair);
            }
            playerWatchEntity.remove(player);
        }
        lock.unlock();
    }

    private static void clearPlayerWatchBlock(ServerPlayerEntity player) {
        lock.lock();
        Pair<Identifier, BlockPos> pair = playerWatchBlockPos.get(player);
        if (pair != null) {
            Set<ServerPlayerEntity> playerSet = blockPosWatchPlayerSet.get(pair);
            playerSet.remove(player);
            if (playerSet.isEmpty()) {
                blockPosWatchPlayerSet.remove(pair);
            }
            playerWatchBlockPos.remove(player);
        }
        lock.unlock();
    }

    public static void disablePcaSyncProtocolGlobal() {
        lock.lock();
        playerWatchBlockPos.clear();
        playerWatchEntity.clear();
        blockPosWatchPlayerSet.clear();
        entityWatchPlayerSet.clear();
        lock.unlock();

        MinecraftServer s = server;
        if (s != null) {
            for (ServerPlayerEntity player : s.getPlayerManager().getPlayerList()) {
                disablePcaSyncProtocol(player);
            }
        }
    }

    public static void enablePcaSyncProtocolGlobal() {
        MinecraftServer s = server;
        if (s == null) {
            return;
        }
        for (ServerPlayerEntity player : s.getPlayerManager().getPlayerList()) {
            enablePcaSyncProtocol(player);
        }
    }

    public static void clearPlayerWatchData(ServerPlayerEntity player) {
        PCASyncProtocol.clearPlayerWatchBlock(player);
        PCASyncProtocol.clearPlayerWatchEntity(player);
    }
}
