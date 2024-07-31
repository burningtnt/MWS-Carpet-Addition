package net.burningtnt.mca.impl.pcaSyncProtocol.impl;

import net.burningtnt.mca.impl.pcaSyncProtocol.PCAProtocol;
import net.burningtnt.mca.impl.pcaSyncProtocol.AbstractPCASynchronizeHandle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public final class EntityPCASynchronizeHandle extends AbstractPCASynchronizeHandle<Entity> {
    public EntityPCASynchronizeHandle() {
        super(PCAProtocol.ENTITY_START, PCAProtocol.ENTITY_CANCEL, PCAProtocol.ENTITY_DATA);
    }

    @Override
    public Entity[] locateTargets(ServerPlayerEntity player, PacketByteBuf buf) {
        Entity entity = player.getServerWorld().getEntityById(buf.readInt());
        if (entity == null) {
            return super.locateTargets(player, buf);
        }
        return new Entity[]{entity};
    }

    @Override
    public boolean encodeTarget(PacketByteBuf buf, Entity entity) {
        World world = entity.getWorld();
        if (world == null || world.isClient) {
            return false;
        }

        buf.writeIdentifier(world.getRegistryKey().getValue());
        buf.writeInt(entity.getId());
        buf.writeNbt(entity.writeNbt(new NbtCompound()));
        return true;
    }
}
