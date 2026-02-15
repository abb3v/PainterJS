package com.breakinblocks.painterjs.kjs;

import com.breakinblocks.painterjs.math.VariableSet;
import com.breakinblocks.painterjs.network.PainterNetwork;
import com.breakinblocks.painterjs.objects.PaintObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class Painter {
    private static final Gson GSON = new Gson();
    public static final VariableSet vars = new VariableSet();

    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new PainterNetwork.VariableSyncPayload(vars.getAll()));
        }
    }

    public static void syncAll() {
        PacketDistributor.sendToAllPlayers(new PainterNetwork.VariableSyncPayload(vars.getAll()));
    }

    public static void paint(Player player, Map<String, Object> data) {
        if (player == null || data == null)
            return;

        Map<String, PaintObject> paintObjects = new HashMap<>();

        data.forEach((id, rawEntry) -> {
            try {
                JsonElement jsonElement = GSON.toJsonTree(rawEntry);
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    PaintObject obj = PaintObject.fromJson(json);
                    paintObjects.put(id, obj);
                }
            } catch (Exception e) {
                com.mojang.logging.LogUtils.getLogger().error("Failed to parse paint object: " + id, e);
            }
        });

        if (!paintObjects.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new PainterNetwork.PaintDataPayload(paintObjects));
            } else if (player.level().isClientSide()) {
                ClientHandler.paint(paintObjects);
            }
        }
    }
}
