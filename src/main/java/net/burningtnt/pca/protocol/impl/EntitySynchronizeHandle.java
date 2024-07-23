package net.burningtnt.pca.protocol.impl;

import net.burningtnt.pca.protocol.Protocol;
import net.burningtnt.pca.protocol.SynchronizeHandle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public final class EntitySynchronizeHandle extends SynchronizeHandle<Entity> {
    public EntitySynchronizeHandle() {
        super(Protocol.ENTITY_START, Protocol.ENTITY_CANCEL, Protocol.ENTITY_DATA);
    }

    @Override
    public Entity[] locateTargets(ServerPlayerEntity player, PacketByteBuf buf) {
        Entity entity = player.getServerWorld().getEntityById(buf.readInt());
        if (entity == null) {
            return findNothing();
        }
        return new Entity[]{entity};
    }

    @Override
    public boolean encodeTarget(PacketByteBuf buf, Entity entity) {
        World world = entity.getWorld();
        if (world == null) {
            return false;
        }

        buf.writeIdentifier(world.getRegistryKey().getValue());
        buf.writeInt(entity.getId());
        buf.writeNbt(entity.writeNbt(new NbtCompound()));
        return true;
    }
}
