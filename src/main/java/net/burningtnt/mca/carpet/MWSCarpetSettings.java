package net.burningtnt.mca.carpet;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class MWSCarpetSettings {
    private MWSCarpetSettings() {
    }

    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface RuleDescription {
        String value();
    }

    public static final Map<String, String> TRANSLATION_MAP;

    static {
        Map<String, String> map = new ConcurrentHashMap<>();
        for (Field rule : MWSCarpetSettings.class.getFields()) {
            if (rule.getAnnotation(Rule.class) == null) {
                continue;
            }
            RuleDescription desc = rule.getAnnotation(RuleDescription.class);
            if (desc == null) {
                throw new AssertionError("Rule " + rule.getName() + " is not annotated with @RuleDescription.");
            }
            map.put("carpet.rule." + rule.getName() +  ".desc", desc.value());
        }
        TRANSLATION_MAP = Collections.unmodifiableMap(map);
    }

    private static final String CATEGORIES = "mcs";

    @Rule(categories = CATEGORIES)
    @RuleDescription("Use legacy implementation, which would not let hoppers stack shulker boxs and comparators would output like the way in vanilla Minecraft.")
    public static boolean legacyStackableShulkerBoxes = false;

    @Rule(categories = CATEGORIES)
    @RuleDescription("Use PCA Sync Protocol to sync Entities and Block Entities' inventories to the client.")
    public static boolean pcaSyncProtocol = false;

    @Rule(categories = CATEGORIES)
    @RuleDescription("Let /tick freeze unfreeze the game while tick has been freezed.")
    public static boolean legacyCarpetTickCommand = false;

    private static final Map<String, List<Consumer<MinecraftServer>>> listeners = new ConcurrentHashMap<>();

    public static void initialize() {
        CarpetServer.settingsManager.parseSettingsClass(MWSCarpetSettings.class);
        CarpetServer.settingsManager.registerRuleObserver((commandSource, carpetRule, s) -> fireListener(commandSource, carpetRule));
    }

    public static void registerListener(String rule, Consumer<MinecraftServer> listener) {
        listeners.computeIfAbsent(rule, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    private static void fireListener(ServerCommandSource source, CarpetRule<?> carpetRule) {
        List<Consumer<MinecraftServer>> l = listeners.get(carpetRule.name());
        if (l == null) {
            return;
        }

        MinecraftServer server = source.getServer();
        for (Consumer<MinecraftServer> listener : l) {
            listener.accept(server);
        }
    }
}
