package com.breakinblocks.painterjs;

import com.breakinblocks.painterjs.kjs.Painter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = PainterAddon.MODID)
public class PainterEvents {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Painter.sync(event.getEntity());
    }
}