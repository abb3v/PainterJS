package com.breakinblocks.painterjs.client;

import com.breakinblocks.painterjs.PainterAddon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = PainterAddon.MODID, value = Dist.CLIENT)
public class PainterClientEvents {

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        ClientPainter.renderScreen(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(),
                event.getPartialTick());
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientPainter.clear();
    }
}
