package com.breakinblocks.painterjs.kjs;

import com.breakinblocks.painterjs.network.PainterNetwork;
import com.breakinblocks.painterjs.objects.PaintObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class Painter {
    private static final Gson GSON = new Gson();

    public static void paint(ServerPlayer player, Map<String, Object> data) {
        if (player == null || data == null)
            return;

        Map<String, PaintObject> paintObjects = new HashMap<>();

        data.forEach((id, rawEntry) -> {
            try {
                // Convert raw entry (Map or other) to JsonObject
                JsonElement jsonElement = GSON.toJsonTree(rawEntry);
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    PaintObject obj = PaintObject.fromJson(json);
                    paintObjects.put(id, obj);
                }
            } catch (Exception e) {
                // Log error or ignore
                com.mojang.logging.LogUtils.getLogger().error("Failed to parse paint object: " + id, e);
            }
        });

        if (!paintObjects.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new PainterNetwork.PaintDataPayload(paintObjects));
        }
    }
}
