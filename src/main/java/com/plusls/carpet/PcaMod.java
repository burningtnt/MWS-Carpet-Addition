package com.plusls.carpet;

import com.plusls.carpet.network.PcaSyncProtocol;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class PcaMod
{
    public static final String MODID = "pca";
    public static final Logger LOGGER = LogManager.getLogger("PcAMod");
    @Nullable
    public static MinecraftServer server = null;

    public static Identifier id(String id) {
        return new Identifier(MODID, id);
    }

    public static void init(MinecraftServer server) {
        PcaSyncProtocol.init();
        PcaMod.server = server;
    }
}
