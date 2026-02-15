package com.breakinblocks.painterjs;

import com.breakinblocks.painterjs.client.ClientPainter;
import com.breakinblocks.painterjs.network.PainterNetwork;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(PainterAddon.MODID)
public class PainterAddon {
    public static final String MODID = "painterjs";

    public PainterAddon(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PainterNetwork.register(event);
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                    ResourceLocation.fromNamespaceAndPath(MODID, "paint"),
                    ClientPainter::render);
        }
    }
}
