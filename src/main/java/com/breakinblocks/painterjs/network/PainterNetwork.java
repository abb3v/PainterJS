package com.breakinblocks.painterjs.network;

import com.breakinblocks.painterjs.PainterAddon;
import com.breakinblocks.painterjs.client.ClientPainter;
import com.breakinblocks.painterjs.objects.PaintObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;

public class PainterNetwork {
    public static final ResourceLocation PAINT_CHANNEL = ResourceLocation.fromNamespaceAndPath(PainterAddon.MODID,
            "paint");
    public static final ResourceLocation VARS_CHANNEL = ResourceLocation.fromNamespaceAndPath(PainterAddon.MODID,
            "vars");

    public record PaintDataPayload(Map<String, PaintObject> objects) implements CustomPacketPayload {
        public static final Type<PaintDataPayload> TYPE = new Type<>(PAINT_CHANNEL);

        public static final StreamCodec<RegistryFriendlyByteBuf, PaintDataPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, PaintObject.STREAM_CODEC),
                PaintDataPayload::objects,
                PaintDataPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record VariableSyncPayload(Map<String, Double> vars) implements CustomPacketPayload {
        public static final Type<VariableSyncPayload> TYPE = new Type<>(VARS_CHANNEL);

        public static final StreamCodec<RegistryFriendlyByteBuf, VariableSyncPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
                VariableSyncPayload::vars,
                VariableSyncPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PainterAddon.MODID).versioned("1.0");
        registrar.playToClient(
                PaintDataPayload.TYPE,
                PaintDataPayload.STREAM_CODEC,
                PainterNetwork::handlePaint);
        registrar.playToClient(
                VariableSyncPayload.TYPE,
                VariableSyncPayload.STREAM_CODEC,
                PainterNetwork::handleVars);
    }

    public static void handlePaint(PaintDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPainter.update(payload.objects());
        });
    }

    public static void handleVars(VariableSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            com.breakinblocks.painterjs.kjs.Painter.vars.putAll(payload.vars());
            ClientPainter.onVariablesUpdated();
        });
    }
}
